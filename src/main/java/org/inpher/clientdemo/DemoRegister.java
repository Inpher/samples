package org.inpher.clientdemo;
import org.inpher.clientapi.InpherClient;
import org.inpher.clientapi.InpherUser;
import org.inpher.clientapi.exceptions.ExistingUserException;

/**
 * A simple demo application for registering an Inpher user. 
 */
public class DemoRegister {
	private static String username = "inpherawsdemo"; 		// change to your username 
	private static String pwd = "mypwd"; 					// change to your password 
	
	public static void main(String [] args) throws Exception {
		InpherClient inpherClient = DemoConfig.generateInpherClient(); 
		InpherUser user = new InpherUser(username, pwd);
		
		try {
			System.out.println("Registering user " + user.getUsername() + " ...");
			// calling the register method of the Inpher client 
			inpherClient.registerUser(user);
			System.out.println("User " + user.getUsername() + " successfully registered."); 
		} catch(ExistingUserException e) {
			System.out.println("User " + user.getUsername() + " already exists."); 
			System.exit(1); 
		}
	}
}