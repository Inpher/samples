/**
* Copyright 2015 Inpher, Inc.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package application;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.inpher.clientapi.DecryptedSearchResponse;
import org.inpher.clientapi.Element;
import org.inpher.clientapi.ElementVisitResult;
import org.inpher.clientapi.ElementVisitor;
import org.inpher.clientapi.FrontendPath;
import org.inpher.clientapi.InpherClient;
import org.inpher.clientapi.InpherProgressListener;
import org.inpher.clientapi.InpherUser;
import org.inpher.clientapi.MakeDirectoryRequest;
import org.inpher.clientapi.ReadDocumentRequest;
import org.inpher.clientapi.UploadDocumentRequest;
import org.inpher.clientapi.VisitElementTreeRequest;
import org.inpher.clientapi.exceptions.ExistingDirectoryException;
import org.inpher.clientapi.exceptions.ExistingUserException;
import org.inpher.clientapi.exceptions.InpherException;
import org.inpher.clientapi.exceptions.InpherRuntimeException;
import org.inpher.clientapi.exceptions.InvalidCredentialsException;
import org.inpher.clientapi.exceptions.RootDirectoryException;

import com.fasterxml.jackson.databind.ObjectMapper;

import application.transfer.ElementToJs;
import application.transfer.LoginResult;
import application.transfer.RankedSearchResultsToJs;
import application.transfer.RegularStatus;
import application.transfer.UploadProgressEntity;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.DirectoryChooser;
import netscape.javascript.JSObject;

public class JavaBridge {
	private InpherClient inpherClient;
	private List<File> lastFilesDrop;
	private JSObject window;
	
	/**
	 * Constructor
	 * 
	 * @param window
	 */
	public JavaBridge() {
	}
	
	public synchronized void log(String message) {
		System.out.println(message);
	}

	public void registerUser(String username, String password, JSObject callback) {
		asyncRun(new JavascriptAsyncThread(callback) {
			@Override
			public Object doSomething() {
				try {
					inpherClient.registerUser(new InpherUser(username, password));
					return LoginResult.createSuccess(username).toJson();
				} catch (ExistingUserException e) {
					e.printStackTrace();
					return LoginResult.createFailure("Registration Failed! (User already exists)").toJson();
				} catch (InpherRuntimeException | InpherException e) {
					e.printStackTrace();
					return LoginResult.createFailure("Registration Failed! ("+e.getMessage()+")").toJson();
				}
			}
		});
	}

	public void loginUser(String username, String password, final JSObject callback) {
		asyncRun(new JavascriptAsyncThread(callback) {
			@Override
			public Object doSomething() {
				try {
					inpherClient.loginUser(new InpherUser(username, password));
					//try { Thread.sleep(5000); } catch (Throwable t) {}
					return LoginResult.createSuccess(username).toJson();
				} catch (InvalidCredentialsException e) {
					e.printStackTrace();
					return LoginResult.createFailure("Login Failed! (Invalid Credentials)").toJson();
				} catch (InpherRuntimeException | InpherException e) {
					e.printStackTrace();
					return LoginResult.createFailure("Login Failed! "+e.getMessage()).toJson();
				}
			}
		});
	}

	public void listFiles(String frontentURI, JSObject addItem) {
		asyncRun(new KillableThread() {
			public void run() {
				KillableThread thread=this;
				FrontendPath fPath = FrontendPath.parse(frontentURI);
				ElementVisitor<Integer> ev = new ElementVisitor<Integer>() {
					@Override
					public ElementVisitResult visitDocument(Element document, Integer userParam) {
						if (thread.killRequest) return ElementVisitResult.TERMINATE;
						ElementToJs elt = new ElementToJs(document.getFrontendURI(), document.getContentType(),
								document.getElementName(), document.getSize());
						callJSMethodByMainThread(addItem, "add", elt.toJson());
						return ElementVisitResult.CONTINUE;
					}

					@Override
					public ElementVisitResult postVisitDirectory(Element dir, Integer userParam) {
						return ElementVisitResult.CONTINUE;
					}

					@Override
					public ElementVisitResult preVisitDirectory(Element dir, Integer userParam, Object[] paramToPass) {
						if (thread.killRequest) return ElementVisitResult.TERMINATE;
						ElementToJs elt = new ElementToJs(dir.getFrontendURI(), "folder", dir.getElementName(), 0);
						if (userParam!=1)
							callJSMethodByMainThread(addItem, "add", elt.toJson());
						if (userParam <= 0)
							return ElementVisitResult.SKIP_SIBLINGS;
						paramToPass[0] = userParam - 1;
						return ElementVisitResult.CONTINUE;
					}
				};
				VisitElementTreeRequest req = new VisitElementTreeRequest(fPath, ev, 1);
				callJSMethodByMainThread(addItem, "clear");
				inpherClient.visitElementTree(req);
				callJSMethodByMainThread(addItem, "done");
			}
		});
	}

	public void initializeInpherClient() {
        try {
        	inpherClient = InpherClient.getClient();
        } catch (Exception e) {
        	e.printStackTrace();
        }
    	if (inpherClient==null) {
        	Alert a = new Alert(AlertType.ERROR);
        	a.setContentText(
        			"The inpher Client has not been generated\n"
        					+"properly. Please run Setup again");
        	a.showAndWait();
        	Platform.exit();
        }
	}


	
	/**
	 * Everything that we do in this function is the following: for each
	 * sourceURI: 1. if local directory, create exactly the same remote
	 * directory 2. if local file, upload it
	 * 
	 * @param sourceURIs
	 * @param destDirURI
	 * @param processItem
	 */
	public void doUploadElement(String destDirURI, final JSObject processItem) {
		System.out.println("In Upload element...");
		asyncRun(new KillableThread() {
			public void run() {
				try {
					//Sanity check: verify if sources are valid paths
					KillableThread thread=this;
					FrontendPath destFPath=FrontendPath.parse(destDirURI);

					ArrayList<Path> pathsToCopy=new ArrayList<>();
					for (File f:lastFilesDrop) {
						if (f.exists() && f.canRead())
							pathsToCopy.add(f.toPath());
						else
							System.out.println("Cowardly ignore "+f+"which cannot be opened");
					}
					if (pathsToCopy.size()==0) {
						callJSMethodByMainThread(processItem, "done",
								RegularStatus.createError("No files were detected in the drag and drop!").toJson());
						return;
					}
					//check that the target is a directory or can be created as a directory
					if (pathsToCopy.size()>1 || Files.isDirectory(pathsToCopy.get(0))) {
						MakeDirectoryRequest mr = new MakeDirectoryRequest(destFPath);
						try { inpherClient.makeDirectory(mr); }
						catch (ExistingDirectoryException | RootDirectoryException e) {}
						catch (InpherRuntimeException e) {
							//else set the destination to the parent folder
							destFPath=FrontendPath.parse(destFPath.getParentFrontendURI());
						}
					}

					for (Path localPath : pathsToCopy) {
						FrontendPath destDir = destFPath.resolve(localPath.getFileName().toString());
						final String destDirStr = destDir.getFrontendURI();

						try {
							Files.walkFileTree(localPath, new FileVisitor<Path>() {
								@Override
								public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
										throws IOException {
									if (thread.killRequest) return FileVisitResult.TERMINATE;
									// Whenever we enter a local directory, we
									// create the same remote directory
									// before treating the content of the folder
									// (we do nothing if the remote directory
									// already exists)
									String relPath = localPath.relativize(dir).toString();
									FrontendPath newdir = FrontendPath.parse(destDirStr + "/" + relPath);
									callJSMethodByMainThread(processItem, "progress", 
											UploadProgressEntity.create(dir.toString(), newdir.toString(), "make dir").toJson());
									System.err.println("[mkdir] " + newdir);
									try {
										inpherClient.makeDirectory(new MakeDirectoryRequest(newdir));
									} catch (ExistingDirectoryException e) {
									}
									callJSMethodByMainThread(processItem, "progress", 
											UploadProgressEntity.create(dir.toString(), newdir.toString(), "done processing").toJson());
									return FileVisitResult.CONTINUE;
								}

								@Override
								public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
									if (thread.killRequest) return FileVisitResult.TERMINATE;
									// Whenever we encounter a local file, we upload
									// it
									String relPath = localPath.relativize(file).toString();
									FrontendPath newfile = FrontendPath.parse(destDirStr + "/" + relPath);
									File f = file.toFile();
									System.err.println("" + file + " -> " + newfile);
									callJSMethodByMainThread(processItem, "progress", 
											UploadProgressEntity.create(file.toString(), newfile.toString(), "uploading").toJson());
									inpherClient.uploadDocument(new UploadDocumentRequest(f, newfile));
									callJSMethodByMainThread(processItem, "progress", 
											UploadProgressEntity.create(file.toString(), newfile.toString(), "done processing").toJson());
									return FileVisitResult.CONTINUE;
								}

								@Override
								public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
									// Treating errors on the local filesystem
									// (corrupted filesystem,
									// circular symlink refs, ...) is out of scope
									// of this demo
									System.err.println("[IGNORING]" + file);
									callJSMethodByMainThread(processItem, "progress", 
											UploadProgressEntity.create(file.toString(), "?", "failed processing").toJson());
									throw exc;
								}

								@Override
								public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
									return FileVisitResult.CONTINUE;
								}
							});
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				} catch (RuntimeException e) {
					//if there was any uncaught exception, return it to the ui
					callJSMethodByMainThread(processItem, "done",
							RegularStatus.createError("An error has occured: "+e.getMessage()).toJson());
					return;
				}
				//normal end
				callJSMethodByMainThread(processItem, "done",
						RegularStatus.createNormal("Transfer complete!").toJson());
				return;
			}
		});
	}

	public void downloadElement(String sourceURIsJson, final JSObject processItem) {  
		boolean overwriteFiles = true; 
		
		ObjectMapper om = new ObjectMapper(); 
		String[] tsourceURIs = null;
		try {
			tsourceURIs = om.readValue(sourceURIsJson, String[].class);
		} catch (Exception e) {
			e.printStackTrace();
			callJSMethodByThisThread(processItem, "done", 
					RegularStatus.createError(e.getMessage()).toJson());
			return;
		}
		final String[] sourceURIs=tsourceURIs; 
		
		DirectoryChooser dirChooser = new DirectoryChooser();
		dirChooser.setTitle("Choose Target Folder");
        File destPath = dirChooser.showDialog(null);
        if (destPath==null) {
        	callJSMethodByThisThread(processItem, "done",
        			RegularStatus.createNormal().toJson());
        }
		

		asyncRun(new KillableThread() {
			public void run() {
				ElementVisitor<Object> ev = null; 
				KillableThread thread = this;
				// loop over the source URIs and visit the tree
				for (String sourceURI : sourceURIs) {
					final FrontendPath sourcePath = FrontendPath.parse(sourceURI);	
					File destPathFull = new File(destPath, sourcePath.getLastElementName()); 
					// create the ElementVisitor object 
					ev = new ElementVisitor<Object>() {
						@Override
						public ElementVisitResult visitDocument(Element document, Object unused) {
							if (thread.killRequest) return ElementVisitResult.TERMINATE;
							String relPath = sourcePath.relativize(document.getFrontendPath());
							File filePath = new File(destPathFull, relPath);
							System.err.println(document.getFrontendURI()+" -> "+filePath);
							callJSMethodByMainThread(processItem, "progress",
									UploadProgressEntity.create(
											document.getFrontendURI(), 
											filePath.toString(), 
											"downloading").toJson());
							if (filePath.exists()) {
								if (!filePath.isFile()) {
									System.err.println("IGNORING: Destination is not a overwritable file");
									return ElementVisitResult.CONTINUE;
								}
								if (!overwriteFiles) {
									System.err.println("IGNORING: Destination already exists");
									return ElementVisitResult.CONTINUE;
								}
							}
							ReadDocumentRequest req = new ReadDocumentRequest(document.getFrontendPath(), filePath);
							inpherClient.readDocument(req);
							callJSMethodByMainThread(processItem, "progress",
									UploadProgressEntity.create(
											document.getFrontendURI(), 
											filePath.toString(), 
											"downloaded").toJson());
							return ElementVisitResult.CONTINUE;
						}

						@Override
						public ElementVisitResult postVisitDirectory(Element dir, Object userParam) {
							return ElementVisitResult.CONTINUE;
						}

						@Override
						public ElementVisitResult preVisitDirectory(Element dir,
								Object userParam, Object[] paramToPass) {
							if (thread.killRequest) return ElementVisitResult.TERMINATE;
							String relPath = sourcePath.relativize(dir.getFrontendPath());
							File newDir = new File(destPathFull, relPath);
							System.err.println("[mkdir] "+dir.getFrontendURI()+" -> "+newDir);
							callJSMethodByMainThread(processItem, "progress",
									UploadProgressEntity.create(
											dir.getFrontendURI(), 
											newDir.toString(), 
											"[mkdir]").toJson());
							if (!newDir.exists() && !newDir.mkdirs()) {
								throw new RuntimeException("Unable to create " + newDir.getAbsolutePath());
							}
							callJSMethodByMainThread(processItem, "progress",
									UploadProgressEntity.create(
											dir.getFrontendURI(), 
											newDir.toString(), 
											"done").toJson());
							return ElementVisitResult.CONTINUE;
						}
					};
					
					try {
						VisitElementTreeRequest request = new VisitElementTreeRequest(sourcePath, ev, null);
						inpherClient.visitElementTree(request);					
						callJSMethodByMainThread(processItem, "done", 
								RegularStatus.createNormal("done!").toJson());
					} catch (Exception e) {
						callJSMethodByMainThread(processItem, "done", 
								RegularStatus.createError(e.getMessage()).toJson());
						return;
					}
				}
			}
		});
	}

	public void search(String kwds, final JSObject callback) {
		asyncRun(new JavascriptAsyncThread(callback) {
			@Override
			public Object doSomething() {
				try {
					List<String> keywords = new ArrayList<>();
					StringTokenizer stok = new StringTokenizer(kwds, " ;,.:?!()+");
					while (stok.hasMoreTokens())
						keywords.add(stok.nextToken());
					// call the search method 
					DecryptedSearchResponse response = inpherClient.search(keywords);
					return RankedSearchResultsToJs.createSuccess(response.getDocumentIds()).toJson();
				} catch (Exception e) {
					e.printStackTrace();
					return RankedSearchResultsToJs.createError(e.getMessage());
				}
			}
		}); 
	}

	public void openDirectly(String docId, final JSObject callback) {
		asyncRun(new JavascriptAsyncThread(callback) {
			@Override
			public Object doSomething() {
				try {
				FrontendPath elem = FrontendPath.parse(docId);
				Path dest = Files.createTempFile("inpherdownload", elem.getLastElementName());
				dest.toFile().deleteOnExit();
				
				ReadDocumentRequest req = new ReadDocumentRequest(elem, dest.toFile());
				inpherClient.readDocument(req);
				
				Desktop.getDesktop().open(dest.toFile());
				return RegularStatus.createNormal().toJson();
				} catch (Exception e) {
					return RegularStatus.createError(e.getMessage()).toJson();
				}
			}
		}); 
	}

	public void test(JSObject x) {
		x.call("call", null, "bli");
	}

	/**
	 * 
	 * @param objThis
	 * @param methname
	 * @param args
	 */
	private void callJSMethodByMainThread(JSObject objThis, String methname, Object... args) {
		Thread t = new Thread() {
			public void run() {
				objThis.call(methname, args);
			}
		};
		Platform.runLater(t);
	}
    /**
	 * Warning: use only if we are in the main thread!!
	 * @param objThis
	 * @param methname
	 * @param args
	 */
	private void callJSMethodByThisThread(JSObject objThis, String methname, Object... args) {
		objThis.call(methname, args);
	}

	/**
	 * 
	 * @param method
	 * @param args
	 */
	private void callJSFunctionByMainThread(JSObject method, Object... args) {
		final Object[] argss = new Object[args.length + 1];
		argss[0] = null;
		for (int i = 0; i < args.length; i++)
			argss[i + 1] = args[i];
		Thread t = new Thread() {
			public void run() {
				method.call("call", argss);
			}
		};
		Platform.runLater(t);
	}

	/**
	 * 
	 * @param x
	 */
	private void asyncRun(KillableThread x) {
		if (KillableThread.runningThread!=null) {
			KillableThread.runningThread.requestKill();
			try {
				KillableThread.runningThread.join();
			} catch (InterruptedException e) {}
			KillableThread.runningThread=null;
		}
		KillableThread.runningThread=x;
		x.start();
	}
	private void asyncRun(JavascriptAsyncThread x) {
		x.start();
	}

	/**
	 * 
	 */
	private abstract class JavascriptAsyncThread extends Thread {
		JSObject callback;
		Object[] args;

		public JavascriptAsyncThread(JSObject callback, Object... args) {
			this.callback = callback;
			this.args = args;
		}

		public abstract Object doSomething();

		public void run() {
			Object res = doSomething();
			// for some unknown reason, the callback must be issued from
			// the javaFX thread, else some strange errors occur
			callJSFunctionByMainThread(callback, res);
		}
	}
	
	public void setLastFilesDrop(List<File> list) {
		lastFilesDrop=list;
	}
	
	public void setWindow(JSObject window) {
		this.window=window;
	}
}
