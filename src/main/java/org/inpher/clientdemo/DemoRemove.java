/**
 * 
 */
package org.inpher.clientdemo;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.inpher.clientapi.InpherClient;
import org.inpher.clientapi.InpherUser;
import org.inpher.clientapi.RemoveElementRequest;
import org.inpher.clientapi.exceptions.InvalidCredentialsException;
import org.inpher.clientapi.exceptions.NonRegisteredUserException;
import org.xml.sax.SAXException;

public class DemoRemove {
	private static String username = "inpherawsdemo";
	private static String pwd = "mypwd"; 
	private static String path = "data"; 
	private InpherClient inpherClient;
	
	DemoRemove(InpherClient inpherClient) {
		this.inpherClient = inpherClient;
	}

	public void removeDirectoryContent(String dirPathStr) throws IOException,
			SAXException, ParserConfigurationException {
		inpherClient.removeElement(new RemoveElementRequest(dirPathStr, true));
	}

	public static void main(String[] args) throws Exception {
		InpherUser user = new InpherUser(username, pwd);
		InpherClient inpherClient = Demo.generateInpherClient();
		// login or registration
		try {
			System.out.println("Trying to login ...");
			inpherClient.loginUser(user);
		} catch (NonRegisteredUserException e) {
			System.out.println("User " + user.getUsername()
					+ " does not exists.");
			System.exit(1);
		} catch (InvalidCredentialsException e) {
			System.out.println("Invalid credentials for user "
					+ user.getUsername() + ". Please, try again.");
			System.exit(1);
		}
		
		DemoRemove demoRemove = new DemoRemove(inpherClient); 
		demoRemove.removeDirectoryContent(username + ":/" + path);
	}
}
