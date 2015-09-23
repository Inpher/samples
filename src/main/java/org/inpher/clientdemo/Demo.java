package org.inpher.clientdemo;

import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.inpher.clientapi.AmazonS3StorageConfiguration;
import org.inpher.clientapi.CloudStorageConfiguration;
import org.inpher.clientapi.EncryptedSolrSearchEngineConfiguration;
import org.inpher.clientapi.InpherClient;
import org.inpher.clientapi.InpherClientConfiguration;
import org.inpher.clientapi.SearchEngineConfiguration;
import org.inpher.clientapi.exceptions.InpherException;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;

public class Demo {
	
	//configuration of the Amazon S3 client
    public static final Regions amazonS3Region = Regions.EU_CENTRAL_1;
    public static final String amazonS3EndPoint = "s3.eu-central-1.amazonaws.com";
    public static final String amazonS3bucketName = "testBucket";

    public static CloudStorageConfiguration getAmazonS3Configuration() {
    	AmazonS3Client s3client = new AmazonS3Client();
    	// ?? AmazonS3Client s3client = new AmazonS3Client(new ProfileCredentialsProvider().getCredentials());
		s3client.setRegion(Region.getRegion(amazonS3Region));
    	s3client.setEndpoint(amazonS3EndPoint);
    	return new AmazonS3StorageConfiguration(s3client, amazonS3bucketName);
    }
    
    //configuration for the Solr Server
    public static final String solrUrl = "https://demosolr.inpher.io/...";
    public static final String serverSSLTrustStore = null;
    public static final String serverSSLTrustStorePassword = null;
    public static final String clientSSLKeyStore = null;
    public static final String clientSSLKeyStorePassword = null;

    public static SearchEngineConfiguration getEncryptedSolrConfiguration() {
    	if (serverSSLTrustStore!=null)         System.setProperty("javax.net.ssl.keyStore", clientSSLKeyStore);
    	if (serverSSLTrustStorePassword!=null) System.setProperty("javax.net.ssl.keyStorePassword", clientSSLKeyStorePassword);
    	if (clientSSLKeyStore!=null)           System.setProperty("javax.net.ssl.trustStore", serverSSLTrustStore);
    	if (clientSSLKeyStorePassword!=null)   System.setProperty("javax.net.ssl.trustStorePassword", serverSSLTrustStorePassword);
    	HttpSolrClient solrServer = new HttpSolrClient(solrUrl);
    	return new EncryptedSolrSearchEngineConfiguration(solrServer);		
    }
    
    
	public static InpherClient generateInpherClient() {
		try {
			CloudStorageConfiguration cloudConfig = getAmazonS3Configuration();
			SearchEngineConfiguration searchConfig = getEncryptedSolrConfiguration();
			InpherClientConfiguration inpherConfig = new InpherClientConfiguration(cloudConfig, searchConfig);
			return InpherClient.getClient(inpherConfig);
		} catch (InpherException e) {
			System.err.println("Something went wrong: please double check your parameters");
			throw new RuntimeException(e);
		}
	}
}
