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

public class DemoDownload {
	private static String username = "inpherawsdemo"; 
	private static String pwd = "mypwd"; 

	private static String dest = "medicalmixed"; 
	private static String source = "medicalmixed"; 
	private static Boolean overwriteFiles = true; 

	public void downloadElement(FrontendPath path, File destDir) {
	}


	public static void main(String[] args) throws Exception {
		// create the client
		final InpherClient inpherClient = Demo.generateInpherClient();
		inpherClient.loginUser(new InpherUser(username, pwd)); 

		final File destPath = new File(dest);
		final FrontendPath sourcePath = FrontendPath.parse(username + ":/"+ source);

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
