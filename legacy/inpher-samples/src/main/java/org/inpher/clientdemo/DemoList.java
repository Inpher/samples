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

import org.inpher.clientapi.Element;
import org.inpher.clientapi.ElementType;
import org.inpher.clientapi.ElementVisitResult;
import org.inpher.clientapi.ElementVisitor;
import org.inpher.clientapi.FrontendPath;
import org.inpher.clientapi.InpherClient;
import org.inpher.clientapi.InpherUser;
import org.inpher.clientapi.VisitElementTreeRequest;

/**
 * A demo application to list the content of a directory. Uses the Inpher element visitor. 
 */
public class DemoList {
	private static String username = "inpherawsdemo";
	private static String pwd = "mypwd";
	private static String path = "/";
	private static Integer maxDepth = 1;

	/**
	 * a simple function to pretty print an element (and its details) on the console
	 */
	public static void prettyPrintElement(Element e) {
		if (e.getType() == ElementType.DIRECTORY
				|| e.getType() == ElementType.ROOT) {
			System.out.println("[DIR] " + e.getFrontendURI());
		} else {
			System.out.print(e.getFrontendURI() + "\t");
			System.out.print(e.getSize() + "\t");
			System.out.print(e.getContentType() + "\t");
			System.out.println(e.getOwnName());
		}
	}

	/**
	 * In this demo, we recursively list the content of the specified remote location [path].
	 * The directory recursion depth is the parameter [maxDepth].	 * 
	 */
	public static void main(String[] args) throws Exception {
		InpherClient inpherClient = InpherClient.getClient();
		inpherClient.loginUser(new InpherUser(username, pwd));
		FrontendPath fpath = FrontendPath.parse(username + ":/" + path);

		// To recursively list the remote location, we will use the ElementVisitor interface
		// from the Inpher API. This interface is very close to the java.nio FileVisitor interface.
		//
		// During the walk of the remote filesystem tree, we need to do the following:
		//  1. when we encounter a document, we pretty-print it
		//  2. when we encounter a directory, we pretty-print its name, and 
		//     and we process its content only if the recursion depth has not
		//     been reached.
		//  To ease our task, we will pass the recursion depth in the optional (Integer)
		//  argument of the ElementVisitor
		ElementVisitor<Integer> elemVisitor = new ElementVisitor<Integer>() {
			@Override
			public ElementVisitResult visitDocument(Element document, Integer depth) {
				//whenever we encounter a document, pretty-print it and continue the walk
				prettyPrintElement(document);
				return ElementVisitResult.CONTINUE;
			}

			@Override
			public ElementVisitResult preVisitDirectory(Element dir,
					Integer depth, Object[] depthToPass) {
				//whenever we encounter a directory, pretty-print it
				//then check the depth to determine if we continue or not
				prettyPrintElement(dir);
				if (depth <= 0) return ElementVisitResult.SKIP_SIBLINGS;
				//if we continue, don't forget to pass the new depth
				depthToPass[0] = new Integer(depth - 1);
				return ElementVisitResult.CONTINUE;
			}

			@Override
			public ElementVisitResult postVisitDirectory(Element dir,
					Integer depth) {
				//nothing needs to be done when we leave a directory
				return ElementVisitResult.CONTINUE;
			}
		};
		inpherClient.visitElementTree(new VisitElementTreeRequest(
				fpath, elemVisitor, maxDepth));
	}
}