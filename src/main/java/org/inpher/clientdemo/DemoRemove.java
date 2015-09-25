/**
 * 
 */
package org.inpher.clientdemo;

import org.inpher.clientapi.FrontendPath;
import org.inpher.clientapi.InpherClient;
import org.inpher.clientapi.InpherUser;
import org.inpher.clientapi.RemoveElementRequest;

/**
 * A basic demo application for recursive remove. 
 */
public class DemoRemove {
	private static String username = "inpherawsdemo";		// change to your username 
	private static String pwd = "mypwd"; 					// change to your password 
	private static String path = "data"; 					// change to the path 
	private static Boolean recursively = true;				// recursive remove?  
	
	public static void main(String[] args) throws Exception {
		InpherClient inpherClient = DemoConfig.generateInpherClient();
		inpherClient.loginUser(new InpherUser(username, pwd));
		
		FrontendPath fpath = FrontendPath.parse(username + ":/" + path);
		// call the client method for removing elements. 
		inpherClient.removeElement(new RemoveElementRequest(fpath, recursively));
	}
}
