package org.inpher.clientdemo;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.inpher.clientapi.AmazonS3StorageConfiguration;
import org.inpher.clientapi.DemoPlaintextSolrSearchEngineConfiguration;
import org.inpher.clientapi.EncryptedSolrSearchEngineConfiguration;
import org.inpher.clientapi.InpherClient;
import org.inpher.clientapi.InpherClientConfiguration;
import org.inpher.clientapi.exceptions.InpherException;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;

public class Demo {
	public static void main(String[] args) throws Exception {
		/*
		System.out.println("\u001B[32m"+
				"****************************************************************\n" + 
				"****     _____ __   _  _____  _     _ _______  ______       ****\n" + 
				"****       |   | \\  | |_____] |_____| |______ |_____/       ****\n" + 
				"****     __|__ |  \\_| |       |     | |______ |    \\_	    ****\n" + 
				"****                                                        ****\n" + 
				"****************************************************************\n\n"+
				"\u001B[0m");
		*/
		Map<String, Class<?> > demos = new HashMap<String, Class<?>>();
		demos.put("Download", DemoDownload.class);
		demos.put("Login",    DemoLogin.class);
		demos.put("Register", DemoRegister.class);
		demos.put("Remove",   DemoRemove.class);
		demos.put("Search",   DemoSearch.class);
		demos.put("PlainSearch", DemoPlainSearch.class);
		demos.put("Upload",   DemoUpload.class);
		demos.put("List",     DemoList.class);
		demos.put("WipeEverything", DemoWipeEverything.class);

		if (args.length<1 || !demos.containsKey(args[0])) {
			System.err.println("At least one demo command must be specified: ");
			for (String s: demos.keySet()) System.err.print(s + ", ");
			System.err.println("");
			System.exit(1);
		}
		
		Class<?> requestedDemo = demos.get(args[0]);
		//Parse command line arguments and if they correspond to a modifiable
		//argument in one of the demo classes, set them
		int i=1;
		boolean stopAfterMessage = false;
		while (i<args.length) {
			String argName = args[i].substring(2);
			if (argName.equals("help") || argName.isEmpty()) {
				stopAfterMessage=true; break;				
			}
			Field f;
			try { f = requestedDemo.getDeclaredField(argName); }
			catch (NoSuchFieldException e) { f=null;}
			if (f==null || !f.isAnnotationPresent(DemoArg.class)) {
				System.err.println("\u001B[1;31mInvalid option --"+argName+"\u001B[0m");
				stopAfterMessage=true; break;
			}
			if (i+1>=args.length) {
				System.err.println("\u001B[1;31mMissing value for --"+argName+"\u001B[0m");
				stopAfterMessage=true; break;
			}
			f.setAccessible(true);
			f.set(null, args[i+1]);
			i+=2;			
		}
		//Print the list of modifiable arguments and their value
		System.err.println("\u001B[32m================================================================\u001B[0m");
		System.err.println("\u001B[32mRunning Demo "+args[0]+" using:\u001B[0m");
		for (Field f : requestedDemo.getDeclaredFields()) {
			if (!f.isAnnotationPresent(DemoArg.class)) continue;
			f.setAccessible(true);
			System.err.println("\u001B[32m"+f.getName()+" = "+f.get(null)+"\u001B[0m");
		}
		System.err.println("\u001B[32m================================================================\u001B[0m");
		if (stopAfterMessage) System.exit(1);
		
		Method m = requestedDemo.getMethod("main", String[].class);
		String[] demoargs = {};
		Object[] ddemoargs = {demoargs};
		m.invoke(null,ddemoargs);		
	}
	public static InpherClient createInpherClient(
			String solrServerStr, 
			String s3BucketName,
			boolean isPlainText) {
		try {
		// cloud storage configuration  
		return generateInpherClient(solrServerStr, s3BucketName, isPlainText);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	//set up and get an Inpher client
	public static InpherClient createInpherClient(
			String solrServerStr,
			String s3BucketName) {
		try {
		// cloud storage configuration  
		return generateInpherClient(solrServerStr, s3BucketName, false);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}


	private static InpherClient generateInpherClient(
			String solrServerStr, 
			String s3BucketName,
			boolean isPlaintext)
			throws InpherException {
		AmazonS3Client s3client = new AmazonS3Client(new ProfileCredentialsProvider().getCredentials());
		s3client.setRegion(Region.getRegion(Regions.EU_CENTRAL_1));
		s3client.setEndpoint("s3.eu-central-1.amazonaws.com");
		AmazonS3StorageConfiguration s3Config = new AmazonS3StorageConfiguration(s3client, s3BucketName); 
		// //or
		//LocalFileStorageConfiguration s3Config = new LocalFileStorageConfiguration(new File("storage"));
		
		// Solr configuration 
		HttpSolrClient solrServer = new HttpSolrClient(solrServerStr, 
				Demo.getHttpClientWithTLSDemoCertificates()); 
		
		if(!isPlaintext) {
		EncryptedSolrSearchEngineConfiguration solrConfig = new EncryptedSolrSearchEngineConfiguration(solrServer); 
		// Inpher client configuration 
		InpherClientConfiguration inpherClientConfig = new InpherClientConfiguration(s3Config, solrConfig); 
		
		// creating the client  
		return InpherClient.getClient(inpherClientConfig); 
		} else {
			DemoPlaintextSolrSearchEngineConfiguration solrConfig = new DemoPlaintextSolrSearchEngineConfiguration(solrServer); 
			// Inpher client configuration 
			InpherClientConfiguration inpherClientConfig = new InpherClientConfiguration(s3Config, solrConfig); 
			
			// creating the client  
			return InpherClient.getClient(inpherClientConfig); 
		}
	}
	
	//The code below creates a HTTPClient with the demo certificate and private key included
	public static final String demoHttpsWalletJKS = "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"; 
	public static HttpClient getHttpClientWithTLSDemoCertificates() {
		try {
		 HttpClient client = new DefaultHttpClient();
		 SSLContext sslContext = SSLContext.getInstance("TLS");
		 byte[] demoHttpsWalletJKSbytes = 
				 org.apache.commons.codec.binary.Base64.decodeBase64(demoHttpsWalletJKS);
		 KeyStore ks = KeyStore.getInstance("JKS");
		 ks.load(new ByteArrayInputStream(demoHttpsWalletJKSbytes), "secret".toCharArray());
		 TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		 tmf.init(ks);
		 KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		 kmf.init(ks, "secret".toCharArray());
		 sslContext.init(
				 kmf.getKeyManagers(), 
				 tmf.getTrustManagers(),null);  
		 SSLSocketFactory sf = new SSLSocketFactory(sslContext);
		 sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
		 Scheme scheme = new Scheme("https", sf, 443);
		 client.getConnectionManager().getSchemeRegistry().register(scheme);
		 return client;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
