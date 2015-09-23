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

/**
 * @author jetchev
 *
 */
public class DemoList {
	@DemoArg
	private static String username = "inpherawsdemo"; 
	@DemoArg
	private static String pwd = "mypwd"; 
	@DemoArg
	private static String solrhttp = "https://54.148.151.19:8983/solr/inpher-frequency"; 
	@DemoArg
	private static String s3BucketName = "inpherbetademo"; 
	@DemoArg
	private static String path = ""; 
	@DemoArg
	private static String maxDepth = "1"; 

	private InpherClient inpherClient;
	
	public DemoList(InpherClient inpherClient) {
		this.inpherClient = inpherClient; 
	}
	
	//dedicated to betty
	public void printElement(Element e) {
		if (e.getType()==ElementType.DIRECTORY || e.getType()==ElementType.ROOT) {
			System.out.println("[DIR] "+e.getFrontendURI());
		} else {
			System.out.print(e.getFrontendURI()+"\t");
			System.out.print(e.getSize()+"\t");
			System.out.print(e.getContentType()+"\t");
			System.out.println(e.getOwnName());			
		}
	}
	
	public void list(FrontendPath fpath, int maxDepthI) throws Exception {
		try {
			
			ElementVisitor<Integer> listElems = new ElementVisitor<Integer>() {
				@Override
				public ElementVisitResult visitDocument(Element document, Integer depth) {
					printElement(document);
					return ElementVisitResult.CONTINUE;
				}
				
				@Override
				public ElementVisitResult preVisitDirectory(Element dir, Integer depth,
						Object[] depthToPass) {
					printElement(dir);
					if (depth <= 0) return ElementVisitResult.SKIP_SIBLINGS;
					depthToPass[0]=new Integer(depth-1);
					return ElementVisitResult.CONTINUE;
				}
				
				@Override
				public ElementVisitResult postVisitDirectory(Element dir, Integer depth) {
					return null;
				}
			};
			VisitElementTreeRequest req = new VisitElementTreeRequest(fpath, listElems, maxDepthI);
			inpherClient.visitElementTree(req);
			/*
			ListElementRequest elreq = new ListElementRequest(fpath);
			Element elres = inpherClient.listElement(elreq);
			printElement(elres);
			if (elres.getType()!=ElementType.DIRECTORY && elres.getType()!=ElementType.ROOT)
				System.exit(0);
			System.out.println();
			System.out.println("Listing directory content:");
			
			ListDirectoryRequest req = new ListDirectoryRequest(fpath);			
			Directory res = inpherClient.listDirectory(req);
			for (Element e : res.getChildList()) {
				printElement(e);
			}
			*/
		} catch (NonDirectoryException e) {
			System.err.println("the specified path ["+fpath+"] is not a directory.\n(this demo only works with directories)");
			System.exit(1);
		}
		//catch (NoChildException e) {
		//	System.err.println("the specified path ["+fpath+"] does not exist.");
		//	System.exit(1);
		//}
	}
	
	public static void main(String [] args) throws Exception {
		InpherClient inpherClient = Demo.createInpherClient(solrhttp, s3BucketName); 
		InpherUser user = new InpherUser(username, pwd);  
		inpherClient.loginUser(user);
		int maxDepthI = Integer.valueOf(maxDepth);
		DemoList demoList = new DemoList(inpherClient);
		FrontendPath fpath = FrontendPath.parse(username+":/"+path);
		demoList.list(fpath, maxDepthI);
	}
}