package org.inpher.clientdemo;
import org.inpher.clientapi.InpherClient;
import org.inpher.clientapi.InpherUser;
import org.inpher.clientapi.exceptions.InvalidCredentialsException;
import org.inpher.clientapi.exceptions.NonRegisteredUserException;

/**
 * @author jetchev
 *
 */
public class DemoLogin {
	@DemoArg
	private static String username = ""; 
	@DemoArg
	private static String pwd = ""; 
	@DemoArg
	private static String solrhttp = "https://54.148.151.19:8983/solr/inpher-frequency"; 
	@DemoArg
	private static String s3BucketName = "inpherbetademo"; 
	private InpherClient inpherClient;
	
	public DemoLogin(InpherClient inpherClient) {
		this.inpherClient = inpherClient; 
	}
	
	public void loginUser(InpherUser user) throws InvalidCredentialsException, NonRegisteredUserException {
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
	
	public static void main(String [] args) throws Exception {
		InpherClient inpherClient = Demo.createInpherClient(solrhttp, s3BucketName); 
		
		InpherUser user = new InpherUser(username, pwd);  
		DemoLogin demoLogin = new DemoLogin(inpherClient); 
		demoLogin.loginUser(user);
	}
}