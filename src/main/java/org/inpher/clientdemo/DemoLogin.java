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