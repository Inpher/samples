package org.inpher.clientdemo;
import org.inpher.clientapi.InpherClient;
import org.inpher.clientapi.InpherUser;
import org.inpher.clientapi.exceptions.ExistingUserException;

public class DemoRegister {
	private static String username = "inpherawsdemo"; 
	private static String pwd = "mypwd"; 
	
	public static void main(String [] args) throws Exception {
		InpherClient inpherClient = Demo.generateInpherClient(); 

		InpherUser user = new InpherUser(username, pwd);
		
		try {
			System.out.println("Registering user " + user.getUsername() + " ...");
			inpherClient.registerUser(user);
			System.out.println("User " + user.getUsername() + " successfully registered."); 
		} catch(ExistingUserException e) {
			System.out.println("User " + user.getUsername() + " already exists."); 
			System.exit(1); 
		}
	}
}