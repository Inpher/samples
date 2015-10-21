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

/**
 * 
 */
package org.inpher.clientdemo;

import org.inpher.clientapi.FrontendPath;
import org.inpher.clientapi.InpherClient;
import org.inpher.clientapi.InpherUser;
import org.inpher.clientapi.RemoveElementRequest;

/**
 * A basic demo application for recursive remove. 
 */
public class DemoRemove {
	private static String username = "inpherawsdemo";		// change to your username 
	private static String pwd = "mypwd"; 					// change to your password 
	private static String path = "data"; 					// change to the path 
	private static Boolean recursively = true;				// recursive remove?  
	
	public static void main(String[] args) throws Exception {
		InpherClient inpherClient = InpherClient.getClient();
		inpherClient.loginUser(new InpherUser(username, pwd));
		
		FrontendPath fpath = FrontendPath.parse(username + ":/" + path);
		// call the client method for removing elements. 
		inpherClient.removeElement(new RemoveElementRequest(fpath, recursively));
	}
}
