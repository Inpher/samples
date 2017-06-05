/**
* Copyright 2015 Inpher, Inc.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

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
		InpherClient inpherClient = InpherClient.getClient(); 
		InpherUser user = new InpherUser(username, pwd);  
		inpherClient.loginUser(user);

		List<String> kwds = Arrays.asList(keywords);
		// call the search method: return the first 100 results
		DecryptedSearchResponse res = inpherClient.search(kwds,0,100);
		// display the search results 
		for (RankedSearchResult e : res.getDocumentIds()) {
			System.out.format("%6.4f\t%s\n", e.getScore(), e.getDocId());
		}
	}
}