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

import java.io.File;

import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.inpher.clientapi.CloudStorageConfiguration;
import org.inpher.clientapi.EncryptedSolrSearchEngineConfiguration;
import org.inpher.clientapi.InpherClient;
import org.inpher.clientapi.InpherClientConfiguration;
import org.inpher.clientapi.LocalFileStorageConfiguration;
import org.inpher.clientapi.SearchEngineConfiguration;
import org.inpher.clientapi.exceptions.InpherException;

/**
 * A basic Inpher client configuration class.  
 */
public class DemoConfig {
	/**
	 * Creates a local root folder configuration in the current directory. 
	 * 
	 * @return a local file storage configuration
	 */
	public static CloudStorageConfiguration getLocalStorageConfiguration() {
		File rootFolder = new File("demorootfolder"); 
		rootFolder.mkdir(); 
		return new LocalFileStorageConfiguration(rootFolder);  
	}
	
	/*
	 * Below is a typical example of an amazon S3 storage setting.  
	 *
    public static final Regions amazonS3Region = <your region>;
    public static final String amazonS3EndPoint = <your endpoint>;
    public static final String amazonS3bucketName = <your bucket name>;

    public static CloudStorageConfiguration getAmazonS3Configuration() {
    	AmazonS3Client s3client = new AmazonS3Client(new ProfileCredentialsProvider().getCredentials());
		s3client.setRegion(Region.getRegion(amazonS3Region));
    	s3client.setEndpoint(amazonS3EndPoint);
    	return new AmazonS3StorageConfiguration(s3client, amazonS3bucketName);
    }
    */
	
    public static final String solrUrl = "http://127.0.0.1:8983/solr/inpher-frequency";
    
    // If using https solrUrl, specify these variables if needed.  
    public static final String clientSSLKeyStore = null; 			// your key store file location 
    public static final String clientSSLKeyStorePassword = null;	// your key store password 
    public static final String serverSSLTrustStore = null;			// your trust store file location 
    public static final String serverSSLTrustStorePassword = null;	// your trust store password

    static {
    	if (clientSSLKeyStore!=null)         System.setProperty("javax.net.ssl.keyStore", clientSSLKeyStore);
    	if (clientSSLKeyStorePassword!=null) System.setProperty("javax.net.ssl.keyStorePassword", clientSSLKeyStorePassword);
    	if (serverSSLTrustStore!=null)           System.setProperty("javax.net.ssl.trustStore", serverSSLTrustStore);
    	if (serverSSLTrustStorePassword!=null)   System.setProperty("javax.net.ssl.trustStorePassword", serverSSLTrustStorePassword);
    }
    
    public static SearchEngineConfiguration getEncryptedSolrConfiguration() {
    	HttpSolrClient solrServer = new HttpSolrClient(solrUrl);
    	return new EncryptedSolrSearchEngineConfiguration(solrServer);		
    }
    
	public static InpherClient generateInpherClient() {
		try {
			// Uncomment one of the two options depending on your storage configuration choice. 
			// CloudStorageConfiguration cloudConfig = getAmazonS3Configuration();
			CloudStorageConfiguration cloudConfig = getLocalStorageConfiguration();
			SearchEngineConfiguration searchConfig = getEncryptedSolrConfiguration();
			InpherClientConfiguration inpherConfig = new InpherClientConfiguration(cloudConfig, searchConfig);
			return InpherClient.getClient(inpherConfig);
		} catch (InpherException e) {
			System.err.println("Something went wrong: please double check your parameters");
			throw new RuntimeException(e);
		}
	}
}