package org.inpher.clientdemo;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.inpher.clientapi.InpherClient;
import org.inpher.clientapi.InpherUser;
import org.inpher.clientapi.DecryptedSearchResponse;
import org.inpher.clientapi.RankedSearchResult;

/**
 * @author jetchev
 *
 */
public class DemoSearch {
	@DemoArg
	private static String username = "inpherawsdemo"; 
	@DemoArg
	private static String pwd = "mypwd"; 
	@DemoArg
	private static String solrhttp = "https://54.148.151.19:8983/solr/inpher-frequency"; 
	@DemoArg
	private static String s3BucketName = "inpherbetademo"; 
	@DemoArg
	private static String keywords = "enterprise electronics"; 

	private InpherClient inpherClient;
	
	public DemoSearch(InpherClient inpherClient) {
		this.inpherClient = inpherClient; 
	}
	
	public void search(List<String> keywordsList) throws Exception {
		DecryptedSearchResponse res = inpherClient.search(keywordsList);
		for (RankedSearchResult e : res.getDocumentIds()) {
			System.out.format("%6.1f\t%s\n", e.getScore(), e.getDocId());
		}
	}
	
	public static void main(String [] args) throws Exception {
		InpherClient inpherClient = Demo.createInpherClient(solrhttp, s3BucketName); 
		InpherUser user = new InpherUser(username, pwd);  
		inpherClient.loginUser(user);
		List<String> kwds = new ArrayList<>();
		StringTokenizer stok = new StringTokenizer(keywords, " ;,.:?!()+");
		while (stok.hasMoreTokens())
			kwds.add(stok.nextToken());
		DemoSearch demoSearch = new DemoSearch(inpherClient);
		demoSearch.search(kwds);
	}
}