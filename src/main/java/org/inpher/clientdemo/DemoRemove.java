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

/**
 * @author jetchev
 *
 */
public class DemoRemove {
	@DemoArg
	private static String username = "inpherawsdemo";
	@DemoArg
	private static String pwd = "mypwd"; 
	@DemoArg
	private static String path = "data"; 
	@DemoArg
	private static String solrhttp = "https://54.148.151.19:8983/solr/inpher-frequency"; 
	@DemoArg
	private static String s3BucketName = "inpherbetademo"; 
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
		InpherClient inpherClient = Demo.createInpherClient(solrhttp, s3BucketName);
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
