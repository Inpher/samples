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

/**
 * In this demo, we will upload the file or folder named [source] into the remote 
 * location named [dest].
 * If the source is a folder, we perform the upload recursively on the local filesystem. 
 */
public class DemoUpload {
	private static String username = "inpherawsdemo"; 		// change with your username 
	private static String pwd = "mypwd"; 					// change with your password 
	private static String source = "medicalmixed"; 			// change with your local source directory path  
	private static String dest = "medicalbis";				// change with your backend destination path 
	
	public static void main(String [] args) throws Exception {
		// creating the Inpher client  
		final InpherClient inpherClient = InpherClient.getClient(); 
		// login with the specified credentials
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
