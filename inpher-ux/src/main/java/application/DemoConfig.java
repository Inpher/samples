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
package application;

import java.io.File;

import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.inpher.clientapi.AmazonS3StorageConfiguration;
import org.inpher.clientapi.CloudStorageConfiguration;
import org.inpher.clientapi.EncryptedSolrSearchEngineConfiguration;
import org.inpher.clientapi.InpherClient;
import org.inpher.clientapi.InpherClientConfiguration;
import org.inpher.clientapi.LocalFileStorageConfiguration;
import org.inpher.clientapi.SearchEngineConfiguration;
import org.inpher.clientapi.exceptions.InpherException;

import com.amazonaws.services.s3.AmazonS3Client;

import configuration.CloudStorageType;
import configuration.InpherConfigProperties;
import utils.ServiceTestResult;
import utils.ServiceTestStatus;
import utils.SimpleServiceTestProgressListener;
import utils.Utils;

/**
 * A basic Inpher client configuration class.  
 */
public class DemoConfig {
	
	public static void main(String[] args) throws Exception {
	    //InpherClient inpher = generateInpherClient();
		File configFile = new File("config.properties");
		if (!configFile.exists()) {
			System.err.println("Config file does not exist!");
			System.exit(0);
		}
		InpherConfigProperties icp = null;
		try {
			icp=InpherConfigProperties.fromConfigFile(configFile);
		} catch (Exception e) {
			System.err.println("There was an error in your config file");
			throw new RuntimeException(e);
		}
		System.out.println(Utils.testAmazonS3(icp, new SimpleServiceTestProgressListener()));
		System.out.println(Utils.testSolr(icp, new SimpleServiceTestProgressListener()));
	}
}