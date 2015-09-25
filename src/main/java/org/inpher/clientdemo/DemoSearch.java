package org.inpher.clientdemo;
import java.util.Arrays;
import java.util.List;

import org.inpher.clientapi.DecryptedSearchResponse;
import org.inpher.clientapi.InpherClient;
import org.inpher.clientapi.InpherUser;
import org.inpher.clientapi.RankedSearchResult;

/**
 * @author jetchev
 *
 */
public class DemoSearch {
	private static String username = "inpherawsdemo"; 
	private static String pwd = "mypwd";  
	private static String[] keywords = {"enterprise", "electronics"}; 
	
	public static void main(String [] args) throws Exception {
		InpherClient inpherClient = DemoConfig.generateInpherClient(); 
		InpherUser user = new InpherUser(username, pwd);  
		inpherClient.loginUser(user);

		List<String> kwds = Arrays.asList(keywords);
		
		DecryptedSearchResponse res = inpherClient.search(kwds);
		
		for (RankedSearchResult e : res.getDocumentIds()) {
			System.out.format("%6.1f\t%s\n", e.getScore(), e.getDocId());
		}
	}
}