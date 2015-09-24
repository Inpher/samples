package org.inpher.clientdemo;

import org.inpher.clientapi.InpherClient;
import org.inpher.clientapi.InpherUser;
import org.inpher.clientapi.Element;
import org.inpher.clientapi.ElementType;
import org.inpher.clientapi.ElementVisitResult;
import org.inpher.clientapi.ElementVisitor;
import org.inpher.clientapi.FrontendPath;
import org.inpher.clientapi.VisitElementTreeRequest;
import org.inpher.clientapi.exceptions.NonDirectoryException;

public class DemoList {
	private static String username = "inpherawsdemo";
	private static String pwd = "mypwd";
	private static String path = "";
	private static String maxDepth = "1";

	private InpherClient inpherClient;

	public DemoList(InpherClient inpherClient) {
		this.inpherClient = inpherClient;
	}

	// dedicated to betty
	public void printElement(Element e) {
		if (e.getType() == ElementType.DIRECTORY
				|| e.getType() == ElementType.ROOT) {
			System.out.println("[DIR] " + e.getFrontendURI());
		} else {
			System.out.print(e.getFrontendURI() + "\t");
			System.out.print(e.getSize() + "\t");
			System.out.print(e.getContentType() + "\t");
			System.out.println(e.getOwnName());
		}
	}

	public void list(FrontendPath fpath, int maxDepthI) throws Exception {
		try {
			ElementVisitor<Integer> listElems = new ElementVisitor<Integer>() {
				@Override
				public ElementVisitResult visitDocument(Element document,
						Integer depth) {
					printElement(document);
					return ElementVisitResult.CONTINUE;
				}

				@Override
				public ElementVisitResult preVisitDirectory(Element dir,
						Integer depth, Object[] depthToPass) {
					printElement(dir);
					if (depth <= 0)
						return ElementVisitResult.SKIP_SIBLINGS;
					depthToPass[0] = new Integer(depth - 1);
					return ElementVisitResult.CONTINUE;
				}

				@Override
				public ElementVisitResult postVisitDirectory(Element dir,
						Integer depth) {
					return null;
				}
			};
			VisitElementTreeRequest req = new VisitElementTreeRequest(fpath,
					listElems, maxDepthI);
			inpherClient.visitElementTree(req);
		} catch (NonDirectoryException e) {
			System.err
					.println("the specified path ["
							+ fpath
							+ "] is not a directory.\n(this demo only works with directories)");
			System.exit(1);
		}
	}

	public static void main(String[] args) throws Exception {
		InpherClient inpherClient = Demo.generateInpherClient();
		InpherUser user = new InpherUser(username, pwd);
		inpherClient.loginUser(user);
		int maxDepthI = Integer.valueOf(maxDepth);
		DemoList demoList = new DemoList(inpherClient);
		FrontendPath fpath = FrontendPath.parse(username + ":/" + path);
		demoList.list(fpath, maxDepthI);
	}
}