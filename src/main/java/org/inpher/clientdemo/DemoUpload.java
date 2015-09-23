package org.inpher.clientdemo;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;

import org.inpher.clientapi.InpherClient;
import org.inpher.clientapi.InpherUser;
import org.inpher.clientapi.FrontendPath;
import org.inpher.clientapi.MakeDirectoryRequest;
import org.inpher.clientapi.UploadDocumentRequest;
import org.inpher.clientapi.exceptions.ExistingDirectoryException;
import org.inpher.clientapi.exceptions.InvalidCredentialsException;
import org.inpher.clientapi.exceptions.NonRegisteredUserException;

public class DemoUpload {
	@DemoArg
	private static String source = "data/demo"; 
	@DemoArg
	private static String username = "inpherawsdemo"; 
	@DemoArg
	private static String pwd = "mypwd"; 
	@DemoArg
	private static String solrhttp = "https://54.148.151.19:8983/solr/inpher-frequency"; 
	@DemoArg
	private static String s3BucketName = "inpherbetademo"; 
	@DemoArg
	private static String dest = "demo";
	private InpherClient inpherClient;
	
	public DemoUpload(InpherClient inpherClient) {
		this.inpherClient = inpherClient;
	}
	
	public void uploadItem(final Path localPath, final FrontendPath destDir) {
		try {
		final String destdirStr = destDir.getFrontendURI();
		
		FileVisitor<Path> fv = new FileVisitor<Path>() {

			@Override
			public FileVisitResult preVisitDirectory(Path dir,
					BasicFileAttributes attrs) throws IOException {
				String relPath = localPath.relativize(dir).toString();
				FrontendPath newdir = FrontendPath.parse(destdirStr+"/"+relPath);
				System.err.println("[mkdir] "+newdir);
				try {
				inpherClient.makeDirectory(new MakeDirectoryRequest(newdir));
				} catch (ExistingDirectoryException e) {}
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(Path file,
					BasicFileAttributes attrs) throws IOException {
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
				System.err.println("[IGNORING]" + file);
				throw exc;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc)
					throws IOException {
				return FileVisitResult.CONTINUE;
			}
		};
		Files.walkFileTree(localPath, fv);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
/*
	public void processFolder(File folder, FrontendPath destPath) {
		if (folder==null || !folder.exists()) {
			System.err.println("This local path: "+folder+" does not exist");
			System.exit(1);
		}
		if (!folder.isDirectory()) {
			System.err.println("This demo can only work with directories");
			System.exit(1);
		}
		// create folder 
		System.out.println("Creating directory " + destPath.toString()); 
		inpherClient.makeDirectory(new MakeDirectoryRequest(destPath));
	    for (final File fileEntry : folder.listFiles()) {
	    	String canPath = null;
			try {
				canPath = fileEntry.getCanonicalPath();
			} catch (IOException e1) {
				e1.printStackTrace();
			} 
	    	if (!fileEntry.getAbsolutePath().equals(canPath)) {
	    		// ignoring symbolic links
	    		continue; 
	    	}
	    	// calculate new dest path 
	    	FrontendPath newDestPath = destPath.getChildPath(fileEntry.getName());
	        if (fileEntry.isDirectory()) { 
	        	// create a folder 
	        	// System.out.println("Creating directory " + newDestPath.toString());
	        	// inpherClient.makeDirectory(new MakeDirectoryRequest(newDestPath)); 
	            processFolder(fileEntry, newDestPath);
	        } else {
	        	if (!fileEntry.isFile() || !fileEntry.canRead()) {
	        		System.err.println("WARNING: Unreadable file: "+fileEntry+" ignored!");
	        		continue;
	        	}
	        	// preprocess and upload document
	        	System.out.println("Uploading file " + fileEntry + " to " + newDestPath.toString()); 
	        	inpherClient.uploadDocument(new UploadDocumentRequest(fileEntry, newDestPath));
	        }
	    }
	}
*/		
	public static void main(String [] args) throws Exception {
		// creating the client  
		InpherClient inpherClient = Demo.createInpherClient(solrhttp, s3BucketName); 
		InpherUser user = new InpherUser(username, pwd); 
		
		// only login (due to the current limitation on the number of buckets on the S3 account)
		try {
			System.out.println("Trying to login ..."); 
			inpherClient.loginUser(user); 
		} catch(NonRegisteredUserException e) {
			System.out.println("User " + user.getUsername() + " does not exists."); 
			System.exit(1);
		} catch(InvalidCredentialsException e) {
			System.out.println("Invalid credentials for user " + user.getUsername() + ". Please, try again."); 
			System.exit(1);
		}
		
		// now we do the document upload 
		DemoUpload demo = new DemoUpload(inpherClient); 
		FrontendPath destDir = FrontendPath.parse(username + ":/" + dest); 
		Path localPath = Paths.get(source); 
		demo.uploadItem(localPath, destDir);
	}
}
