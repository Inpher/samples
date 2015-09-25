package org.inpher.clientdemo;
import org.inpher.clientapi.InpherClient;
import org.inpher.clientapi.InpherUser;
import org.inpher.clientapi.exceptions.InvalidCredentialsException;
import org.inpher.clientapi.exceptions.NonRegisteredUserException;

/**
 * A simple demo application for a login. 
 */
public class DemoLogin {
	private static String username = "inpherawsdemo"; 
	private static String pwd = "mypwd"; 
	
	public static void main(String [] args) throws Exception {
		InpherClient inpherClient = DemoConfig.generateInpherClient(); 
		
		InpherUser user = new InpherUser(username, pwd);  

		try {
			System.out.println("Logging in user " + user.getUsername() + " ...");
			inpherClient.loginUser(user);
			System.out.println("User " + user.getUsername() + " successfully logged."); 
		} catch(NonRegisteredUserException e) {
			System.out.println("User " + user.getUsername() + " does not exists."); 
			System.exit(1);
		} catch(InvalidCredentialsException e) {
			System.out.println("Invalid credentials for user " + user.getUsername() + ". Please, try again."); 
			System.exit(1);
		}
	}
}