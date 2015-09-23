package org.inpher.clientdemo;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.inpher.clientapi.InpherClient;
import org.inpher.clientapi.InpherUser;
import org.inpher.clientapi.DecryptedSearchResponse;
import org.inpher.clientapi.FrontendPath;
import org.inpher.clientapi.RankedSearchResult;
import org.inpher.clientapi.exceptions.InvalidCredentialsException;
import org.inpher.clientapi.exceptions.NonRegisteredUserException;

/**
 * @author jetchev
 *
 */
public class DemoPlainSearch {
	@DemoArg
	private static String username = "inpherawsdemo";
	@DemoArg
	private static String pwd = "mypwd";
	@DemoArg
	private static String solrhttp = "https://54.148.151.19:8983/solr/inpher-plaintext-frequency";
	@DemoArg
	private static String s3BucketName = "inpherbetademo"; 
	@DemoArg
	private static String source = "C:\\Users\\Blagovesta\\Documents\\Mtel";
	@DemoArg
	private static String dest = "demo_folder";
	@DemoArg
	private static String keywords = "enterprise electronics";

	private InpherClient inpherClient;

	public DemoPlainSearch(InpherClient inpherClient) {
		this.inpherClient = inpherClient;
	}

	public void search(List<String> keywordsList) throws Exception {
		// try {
		DecryptedSearchResponse res = inpherClient.plaintextSearch(keywordsList);
		System.out.println("Total number of results: "
				+ res.getDocumentIds().size());
		for (RankedSearchResult e : res.getDocumentIds()) {
			System.out.println(e.getScore() + "\t" + e.getDocId());
		}
		// }
	}

	public static void main(String[] args) throws Exception {
		InpherClient inpherClient = Demo.createInpherClient(solrhttp, s3BucketName, true);
		InpherUser user = new InpherUser(username, pwd);
		inpherClient.loginUser(user);

		// only login (due to the current limitation on the number of buckets on
		// the S3 account)
		try {
			System.out.println("Trying to login ...");
			inpherClient.loginUser(user);
		} catch (NonRegisteredUserException e) {
			System.out.println("User " + user.getUsername()
					+ " does not exists.");
			System.exit(1);
		} catch (InvalidCredentialsException e) {
			System.out.println("Invalid credentials for user "
					+ user.getUsername() + ". Please, try again.");
			System.exit(1);
		}

		// now we do the document upload
		DemoUpload demo = new DemoUpload(inpherClient);
		FrontendPath dataPath = FrontendPath.parse(username + ":/"
				+ dest);
		Path dataDir = Paths.get(source);
		demo.uploadItem(dataDir, dataPath);

		List<String> kwds = new ArrayList<>();
		StringTokenizer stok = new StringTokenizer(keywords, " ;,.:?!()+");
		while (stok.hasMoreTokens())
			kwds.add(stok.nextToken());
		DemoPlainSearch demoSearch = new DemoPlainSearch(inpherClient);
		demoSearch.search(kwds);
	}
}
