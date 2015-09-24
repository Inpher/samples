package org.inpher.clientdemo;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;

import org.inpher.clientapi.FrontendPath;
import org.inpher.clientapi.InpherClient;
import org.inpher.clientapi.InpherUser;
import org.inpher.clientapi.MakeDirectoryRequest;
import org.inpher.clientapi.UploadDocumentRequest;
import org.inpher.clientapi.exceptions.ExistingDirectoryException;

public class DemoUpload {
	private static String username = "inpherawsdemo"; 
	private static String pwd = "mypwd"; 
	private static String source = "data/demo"; 
	private static String dest = "/demo";
	
	/**
	 * In this demo, we will upload the file or folder named [source] into the remote 
	 * location named [dest].
	 * If the source is a folder, we perform the upload recursively on the local filesystem 
	 */
	public static void main(String [] args) throws Exception {
		// creating the inpher client  
		final InpherClient inpherClient = Demo.generateInpherClient(); 
		//login with the specified credentials
		inpherClient.loginUser(new InpherUser(username, pwd)); 
		
		
		// Now, we will use the java.nio FileVisitor interface to recursively upload 
		// the source file/folder. This interface allows you to walk across the local filesystem
		// tree and at each file/folder encountered, call a custom callback function.
		//
		// In this demo, all we need to do is the following:
		//  1. whenever we enter into a local directory, we create the same remote directory.
		//  2. whenever we encounter a local file, we upload it.
		
		FrontendPath destDir = FrontendPath.parse(username + ":/" + dest); 
		final Path localPath = Paths.get(source); 

		final String destdirStr = destDir.getFrontendURI();
		
		Files.walkFileTree(localPath,new FileVisitor<Path>() {
			@Override
			public FileVisitResult preVisitDirectory(Path dir,
					BasicFileAttributes attrs) throws IOException {
				// Whenever we enter a local directory, we create the same remote directory
				// before treating the content of the folder
				// (we do nothing if the remote directory already exists)
				String relPath = localPath.relativize(dir).toString();
				FrontendPath newdir = FrontendPath.parse(destdirStr+"/"+relPath);
				System.err.println("[mkdir] "+newdir);
				try { inpherClient.makeDirectory(new MakeDirectoryRequest(newdir));
				} catch (ExistingDirectoryException e) {}
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(Path file,
					BasicFileAttributes attrs) throws IOException {
				// Whenever we encounter a local file, we upload it
				String relPath = localPath.relativize(file).toString();
				FrontendPath newfile = FrontendPath.parse(destdirStr+"/"+relPath);
				File f = file.toFile();
				System.err.println("" + file + " -> "+newfile);
				inpherClient.uploadDocument(new UploadDocumentRequest(f,newfile));
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFileFailed(Path file, IOException exc)
					throws IOException {
				// Treating errors on the local filesystem (corrupted filesystem, 
				// circular symlink refs, ...) is out of scope of this demo
				System.err.println("[IGNORING]" + file);
				throw exc;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc)
					throws IOException {
				// nothing particular needs to be done when we leave a directory
				return FileVisitResult.CONTINUE;
			}
		});	
	}
}
