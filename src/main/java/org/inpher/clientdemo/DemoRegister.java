package org.inpher.clientdemo;
import org.inpher.clientapi.InpherClient;
import org.inpher.clientapi.InpherUser;
import org.inpher.clientapi.exceptions.InpherException;
import org.inpher.clientapi.exceptions.InpherRuntimeException;
import org.inpher.clientapi.exceptions.ExistingUserException;

/**
 * 
 */
public class DemoRegister {
	@DemoArg
	private static String username = "inpherawsdemo"; 
	@DemoArg
	private static String pwd = "mypwd"; 
    @DemoArg
	private static String solrhttp = "https://54.148.151.19:8983/solr/inpher-frequency"; 
	@DemoArg
	private static String s3BucketName = "inpherbetademo"; 
	private InpherClient inpherClient;
	
	public DemoRegister(InpherClient inpherClient) {
		this.inpherClient = inpherClient; 
	}
	
	public void registerUser(InpherUser user) throws InpherException, InpherRuntimeException {
		try {
			System.out.println("Registering user " + user.getUsername() + " ...");
			inpherClient.registerUser(user);
			System.out.println("User " + user.getUsername() + " successfully registered."); 
		} catch(ExistingUserException e) {
			System.out.println("User " + user.getUsername() + " already exists."); 
			System.exit(1); 
		}
	}
	
	public static void main(String [] args) throws Exception {
		InpherClient inpherClient = Demo.createInpherClient(solrhttp, s3BucketName); 
		InpherUser user = new InpherUser(username, pwd);
		DemoRegister demoReg = new DemoRegister(inpherClient); 
		demoReg.registerUser(user);
	}
}