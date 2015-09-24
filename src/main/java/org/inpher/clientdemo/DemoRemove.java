/**
 * 
 */
package org.inpher.clientdemo;

import org.inpher.clientapi.FrontendPath;
import org.inpher.clientapi.InpherClient;
import org.inpher.clientapi.InpherUser;
import org.inpher.clientapi.RemoveElementRequest;

public class DemoRemove {
	private static String username = "inpherawsdemo";
	private static String pwd = "mypwd"; 
	private static String path = "data"; 
	private static Boolean recursively = true;
	
	public static void main(String[] args) throws Exception {
		InpherClient inpherClient = Demo.generateInpherClient();
		inpherClient.loginUser(new InpherUser(username, pwd));
		
		FrontendPath fpath = FrontendPath.parse(username + ":/" + path);
		inpherClient.removeElement(new RemoveElementRequest(fpath, recursively));
	}
}
