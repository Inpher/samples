package org.inpher.clientdemo;
import java.util.Arrays;
import java.util.List;

import org.inpher.clientapi.DecryptedSearchResponse;
import org.inpher.clientapi.InpherClient;
import org.inpher.clientapi.InpherUser;
import org.inpher.clientapi.RankedSearchResult;

/**
 * A basic demo application illustrating keyword search. 
 */
public class DemoSearch {
	private static String username = "inpherawsdemo";					// change to your username  
	private static String pwd = "mypwd";  								// change to your password 
	private static String[] keywords = {"enterprise", "electronics"}; 	// change to your list of keywords 
	
	public static void main(String [] args) throws Exception {
		InpherClient inpherClient = DemoConfig.generateInpherClient(); 
		InpherUser user = new InpherUser(username, pwd);  
		inpherClient.loginUser(user);

		List<String> kwds = Arrays.asList(keywords);
		// call the search method 
		DecryptedSearchResponse res = inpherClient.search(kwds);
		// display the search results 
		for (RankedSearchResult e : res.getDocumentIds()) {
			System.out.format("%6.1f\t%s\n", e.getScore(), e.getDocId());
		}
	}
}