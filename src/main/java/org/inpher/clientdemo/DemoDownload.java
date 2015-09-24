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
 * 
 */

/**
 * @author jetchev
 *
 */
public class DemoDownload {
	private static String username = "inpherawsdemo"; 
	private static String pwd = "mypwd"; 

	private static String dest = "data/output/"; 
	private static String source = "data"; 
	private static String overwrite = "true"; 

	public void downloadElement(FrontendPath path, File destDir) {
	}


	public static void main(String[] args) throws Exception {
		// create the client
		final InpherClient inpherClient = Demo.generateInpherClient();
		inpherClient.loginUser(new InpherUser(username, pwd)); 

		File destDir = new File(dest);
		FrontendPath path = FrontendPath.parse(username + ":/"+ source);

		final boolean overwriteB = Boolean.valueOf(overwrite);
		if (!destDir.exists() || !destDir.isDirectory()) {
			System.err.println("The local folder "+destDir+" does not exist!");
			System.exit(1);
		}
		ElementVisitor<File> ev = new ElementVisitor<File>() {

			@Override
			public ElementVisitResult visitDocument(Element document,
					File dirPath) {
				try {
					File filePath = new File(dirPath,document.getElementName());
					System.err.println(document.getFrontendURI()+" -> "+filePath);
					if (filePath.exists()) {
						if (!filePath.isFile()) {
							System.err.println("IGNORING: Destination is not a overwritable file");
							return ElementVisitResult.CONTINUE;
						}
						if (!overwriteB) {
							System.err.println("IGNORING: Destination already exists");
							return ElementVisitResult.CONTINUE;
						}
					}
					ReadDocumentRequest req = new ReadDocumentRequest(document.getFrontendURI(), filePath);
					inpherClient.readDocument(req);
					return null;
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}
			}

			@Override
			public ElementVisitResult postVisitDirectory(Element dir,
					File userParam) {
				return null;
			}

			@Override
			public ElementVisitResult preVisitDirectory(Element dir,
					File parentPath, Object[] childPath) {
				File newDir = new File(parentPath,dir.getElementName());
				childPath[0]=newDir;
				System.err.println("[mkdir] "+dir.getFrontendURI()+" -> "+newDir);
				if (!newDir.exists() && !newDir.mkdirs()) {
					throw new RuntimeException("Unable to create " + newDir.getAbsolutePath());
				}
				return null;
			}
		};
		VisitElementTreeRequest request = new VisitElementTreeRequest(path, ev, destDir);
		inpherClient.visitElementTree(request);

	}
}
