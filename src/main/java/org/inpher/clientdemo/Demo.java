package org.inpher.clientdemo;

import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.inpher.clientapi.AmazonS3StorageConfiguration;
import org.inpher.clientapi.CloudStorageConfiguration;
import org.inpher.clientapi.EncryptedSolrSearchEngineConfiguration;
import org.inpher.clientapi.InpherClient;
import org.inpher.clientapi.InpherClientConfiguration;
import org.inpher.clientapi.SearchEngineConfiguration;
import org.inpher.clientapi.exceptions.InpherException;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;

public class Demo {
	// configuration of the Amazon S3 client
    public static final Regions amazonS3Region = Regions.EU_CENTRAL_1;
    public static final String amazonS3EndPoint = "s3.eu-central-1.amazonaws.com";
    public static final String amazonS3bucketName = "inpherawsdemo";
	

    public static CloudStorageConfiguration getAmazonS3Configuration() {
    	//AmazonS3Client s3client = new AmazonS3Client();
    	AmazonS3Client s3client = new AmazonS3Client(new ProfileCredentialsProvider().getCredentials());
		s3client.setRegion(Region.getRegion(amazonS3Region));
    	s3client.setEndpoint(amazonS3EndPoint);
    	return new AmazonS3StorageConfiguration(s3client, amazonS3bucketName);
    }
    
    // configuration for the Solr Server
    public static final String solrUrl = "https://demosolr.inpher.io/solr/inpher-frequency";
    public static final String serverSSLTrustStore = "demokeys/client-truststore.jks";
    public static final String serverSSLTrustStorePassword = "changeit";
    public static final String clientSSLKeyStore = "demokeys/nicolas.jks";
    public static final String clientSSLKeyStorePassword = "secret";

    static {
    	if (clientSSLKeyStore!=null)         System.setProperty("javax.net.ssl.keyStore", clientSSLKeyStore);
    	if (clientSSLKeyStorePassword!=null) System.setProperty("javax.net.ssl.keyStorePassword", clientSSLKeyStorePassword);
    	if (serverSSLTrustStore!=null)           System.setProperty("javax.net.ssl.trustStore", serverSSLTrustStore);
    	if (serverSSLTrustStorePassword!=null)   System.setProperty("javax.net.ssl.trustStorePassword", serverSSLTrustStorePassword);
    	System.out.println("ssl keystore:"+System.getProperty("javax.net.ssl.keyStore"));
    	System.out.println("ssl keystorepwd:"+System.getProperty("javax.net.ssl.keyStorePassword"));
    	System.out.println("ssl truststore:"+System.getProperty("javax.net.ssl.trustStore"));
    	System.out.println("ssl truststorepwd:"+System.getProperty("javax.net.ssl.trustStorePassword"));
    }
    
    public static SearchEngineConfiguration getEncryptedSolrConfiguration() {
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