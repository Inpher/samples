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

import org.inpher.clientapi.Element;
import org.inpher.clientapi.ElementVisitResult;
import org.inpher.clientapi.ElementVisitor;
import org.inpher.clientapi.FrontendPath;
import org.inpher.clientapi.InpherClient;
import org.inpher.clientapi.InpherUser;
import org.inpher.clientapi.ReadDocumentRequest;
import org.inpher.clientapi.VisitElementTreeRequest;

/**
 * A demo application for recursively downloading files stored on the cloud. We will download the file or folder 
 * named [source] from the backend into the local location named [dest].
 * If the source is a folder, we perform the download recursively on the backend.
 */
public class DemoDownload {
	private static String username = "inpherawsdemo"; 	// set your Inpher username 
	private static String pwd = "mypwd"; 				// set your password 

	private static String dest = "medicalmixed"; 		// set your destination directory 
	private static String source = "medicalmixed"; 		// set your source directory 
	private static Boolean overwriteFiles = true; 		// set whether you would like files to be overwritten

	public static void main(String[] args) throws Exception {
		// create the Inpher client
		final InpherClient inpherClient = InpherClient.getClient();
		inpherClient.loginUser(new InpherUser(username, pwd)); 

		final File destPath = new File(dest);
		final FrontendPath sourcePath = FrontendPath.parse(username + ":/"+ source);

		// create the ElementVisitor object 
		ElementVisitor<Object> ev = new ElementVisitor<Object>() {
			@Override
			public ElementVisitResult visitDocument(Element document, Object unused) {
				String relPath = sourcePath.relativize(document.getFrontendPath());
				File filePath = new File(destPath,relPath);
				System.err.println(document.getFrontendURI()+" -> "+filePath);
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
				return ElementVisitResult.CONTINUE;
			}

			@Override
			public ElementVisitResult postVisitDirectory(Element dir, Object userParam) {
				return ElementVisitResult.CONTINUE;
			}

			@Override
			public ElementVisitResult preVisitDirectory(Element dir,
					Object userParam, Object[] paramToPass) {
				String relPath = sourcePath.relativize(dir.getFrontendPath());
				File newDir = new File(destPath, relPath);
				System.err.println("[mkdir] "+dir.getFrontendURI()+" -> "+newDir);
				if (!newDir.exists() && !newDir.mkdirs()) {
					throw new RuntimeException("Unable to create " + newDir.getAbsolutePath());
				}
				return ElementVisitResult.CONTINUE;
			}
		};
		VisitElementTreeRequest request = new VisitElementTreeRequest(sourcePath, ev, null);
		inpherClient.visitElementTree(request);
	}
}
