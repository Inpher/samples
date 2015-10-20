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