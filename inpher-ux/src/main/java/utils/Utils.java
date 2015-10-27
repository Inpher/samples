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

package utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Properties;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.inpher.clientapi.AmazonS3StorageConfiguration;
import org.inpher.clientapi.CloudStorageConfiguration;
import org.inpher.clientapi.EncryptedSolrSearchEngineConfiguration;
import org.inpher.clientapi.InpherClient;
import org.inpher.clientapi.InpherClientConfiguration;
import org.inpher.clientapi.LocalFileStorageConfiguration;
import org.inpher.clientapi.SearchEngineConfiguration;
import org.inpher.clientapi.exceptions.InpherException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.kms.model.GrantOperation;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.AccessControlList;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.CreateBucketRequest;
import com.amazonaws.services.s3.model.GetBucketAclRequest;
import com.amazonaws.services.s3.model.Grant;
import com.amazonaws.services.s3.model.HeadBucketRequest;
import com.amazonaws.services.securitytoken.model.Credentials;

import configuration.CloudStorageType;
import configuration.InpherConfigProperties;
import configuration.SolrInstallType;

public class Utils {
	private static Logger logger; 
	private static Path appTmpDir;

	
	static { try {
		logger = LoggerFactory.getLogger("test");		
		appTmpDir = Files.createTempDirectory("inphergui");
		//schedule deletion of the tmpdir on shutdown
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() { try {
				System.out.println("cleaning up "+appTmpDir);
				Utils.deleteRecursively(appTmpDir);
			} catch (IOException e) {}
			}
		});
	} catch (Exception e) {} }
	public static Logger getLogger() {return logger;}
	public static Path getAppTmpDir() {return appTmpDir;}

	
	public static InpherClientConfiguration readClientConfiguration() {
		return null; 
	}
	
	public static void saveClientConfiguration(Properties prop) {
	}
	
	public static void downloadWithProgressListener(URL source, Path destination, DownloadProgressListener listener) throws IOException {
		HttpURLConnection inconn = (HttpURLConnection) source.openConnection();
		InputStream in = source.openStream();
		long totalLength = inconn.getContentLengthLong();
		WritableByteChannel wbc = Files.newByteChannel(destination, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);		
		ReadableByteChannel rbc = Channels.newChannel(in);
		ByteBuffer buffer = ByteBuffer.allocate(2048);
		double totalLengthD = totalLength;
		double totalRead=0;
		double nextListenerRead=0;
		double chunks=Math.max(1024, totalLengthD/1000);
		while (true)
		{
			if (!wbc.isOpen()) return;
			int r = rbc.read(buffer);
			if (r<0) break;
			totalRead += r;
			buffer.flip();
			wbc.write(buffer);
			buffer.clear();
			if (listener!=null && totalRead>nextListenerRead) {
				if (listener.onProgress(100*totalRead/totalLengthD)
						==DownloadProgressResult.ABORT) break;
				nextListenerRead+=chunks;
			}
		}
		listener.onProgress(100*totalRead/totalLengthD);
		wbc.close();
		rbc.close();
	}
	public static void downloadWithProgressListener(URL source, Path destination) throws IOException {
		downloadWithProgressListener(source, destination,
				(x)->{
					System.out.format("[ Downloading: %5.1f %% ]\r",x);
					return DownloadProgressResult.CONTINUE;
				});
	}
	
	public static void deleteRecursively(Path basePath) throws IOException {
		Files.walkFileTree(basePath, new FileVisitor<Path>() {

			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				Files.deleteIfExists(file);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
				Files.deleteIfExists(dir);
				return FileVisitResult.CONTINUE;
			}
		});
	}
	
	//Solr management
	public static void startLocalSolr(Path solrDestFolder, String startOrStop) throws Exception {
		if (System.getProperty("os.name").startsWith("Windows")) {
			// Windows OS
			logger.info("Starting solr server for windows...");
			String solrCmdAbsURI = solrDestFolder.resolve("solr-5.3.1/bin/solr.cmd").toString(); 
			Process p = Runtime.getRuntime().exec("cmd /c " + solrCmdAbsURI + " " + startOrStop);
			new PipeThread(p.getInputStream(),System.out).start();
			new PipeThread(p.getErrorStream(),System.err).start();
			p.waitFor(); 
		} else {
			// any other OS 
			logger.info("Starting solr server for linux/osx...");
			String solrBinAbsURI = solrDestFolder.resolve("solr-5.3.1/bin/solr").toString();
			Process p = Runtime.getRuntime().exec(solrBinAbsURI + " " + startOrStop);
			new PipeThread(p.getInputStream(),System.out).start();
			new PipeThread(p.getErrorStream(),System.err).start();
			p.waitFor();
		}
	}

	private static ServiceTestResult testSolrQuery(String solrBaseUrl) {
		try {
			Utils.getLogger().debug("XXX");
			SolrClient server = new HttpSolrClient(solrBaseUrl);
			SolrQuery query = new SolrQuery();
			query.set("q", "encKeywords:(\"impossible1\" AND \"impossible2\") OR id:bla");
			query.set("defType", "myqp"); 
			query.set("fl", "id,score");
			query.set("rows", 100);
			query.set("debugQuery", "false");
			QueryResponse response = server.query(query);
			SolrDocumentList list = response.getResults();
			server.close();
			ServiceTestResult success = ServiceTestResult.createSuccess();
			success.setData("solrclient", server);
			return success;
		} catch (Exception e) {
			return ServiceTestResult.createFailure(
					ServiceTestStatus.UNKNOWN_ERROR, 
					e);
		}
	}
	public static ServiceTestResult testSolr(InpherConfigProperties icp, ServiceTestProgressListener progress) {
		String solrBaseUrl = icp.getSolrURL();
		progress.onProgress(0, "setting SSL keys");
    	if (icp.getKeyStorePath()!=null) System.setProperty("javax.net.ssl.keyStore", icp.getKeyStorePath());
    	if (icp.getKeyStorePasswd()!=null) System.setProperty("javax.net.ssl.keyStorePassword", icp.getKeyStorePasswd());
    	if (icp.getTrustStorePath()!=null) System.setProperty("javax.net.ssl.trustStore", icp.getTrustStorePath());
    	if (icp.getTrustStorePasswd()!=null) System.setProperty("javax.net.ssl.trustStorePassword", icp.getTrustStorePasswd());
		progress.onProgress(10, "Issuing a solr test query");
		ServiceTestResult solrStatus = testSolrQuery(solrBaseUrl);
		//if the query is successful, return the solr status
		if (solrStatus.getStatus()==ServiceTestStatus.OK) {
			progress.onProgress(100, "Solr is up and running");
			return solrStatus;
		}
		//if it is a remote solr, return the failure status
		if (icp.getSolrInstallationType()!=SolrInstallType.LOCAL_SOLR)
			return solrStatus;
		if (isNullOrEmpty(icp.getLocalSolrRootFolder())) {
			return ServiceTestResult.createFailure(
					ServiceTestStatus.INCOMPLETE_CONFIGURATION, 
					"The local solr root folder is not specified");
		}
		File f = new File(icp.getLocalSolrRootFolder());
		if (!f.exists() || !f.isDirectory()) {
			return ServiceTestResult.createFailure(
					ServiceTestStatus.INCOMPLETE_CONFIGURATION, 
					"The local solr root folder is invalid");
		}
		if (icp.isAutoRebootSolr()) {
			progress.onProgress(10, "Trying to boot or reboot solr");
			try {
				Utils.startLocalSolr(Paths.get(icp.getLocalSolrRootFolder()), "restart");
			} catch (Exception e) {}
		}
		progress.onProgress(80, "Issuing a solr test query");
		solrStatus = Utils.testSolrQuery(solrBaseUrl);
		if (solrStatus.getStatus()==ServiceTestStatus.OK) {
			progress.onProgress(100, "Solr is up and running");
			return solrStatus;
		}
		return solrStatus;
	}
	
	public static boolean isNullOrEmpty(String s) {
		return s==null || s.isEmpty();
	}
	
	public static ServiceTestResult testAmazonS3(InpherConfigProperties icp, SimpleServiceTestProgressListener progress) {
		progress.onProgress(0, "Getting the credentials");
		AmazonS3 s3 = null;
		if (!icp.isAmazonS3UseDefaultCredentials()) {
			if (icp.getAmazonS3AccessKeyId()==null)
				return ServiceTestResult.createFailure(
						ServiceTestStatus.INCOMPLETE_CONFIGURATION,
						"s3 access key id is not set");
			if (icp.getAmazonS3SecretAccessKey()==null)
				return ServiceTestResult.createFailure(
						ServiceTestStatus.INCOMPLETE_CONFIGURATION,
						"s3 secret access key is not set");
			s3 = new AmazonS3Client(new BasicAWSCredentials(icp.getAmazonS3AccessKeyId(), icp.getAmazonS3SecretAccessKey()));
		} else {
			s3 = new AmazonS3Client();			
		}
		//Parsing and setting region
		progress.onProgress(10, "parsing region");
		Region region = null;
		try {
			Regions r = Regions.fromName(icp.getAmazonS3RegionName());
			region = Region.getRegion(r);
			s3.setRegion(region);
		} catch (RuntimeException e) {}
		//Query the bucket
		progress.onProgress(20, "querying the bucket");
		if (icp.getAmazonS3BucketName()==null)
			return ServiceTestResult.createFailure(
					ServiceTestStatus.INCOMPLETE_CONFIGURATION,
					"s3 bucketName is not set");
		String region2=null;
		try {
			region2 = s3.getBucketLocation(icp.getAmazonS3BucketName());
		} catch (AmazonS3Exception e) {
			if (e.getErrorCode().equals("NoSuchBucket")) {
				return ServiceTestResult.createFailure(
						ServiceTestStatus.AMAZONS3_BUCKET_DOES_NOT_EXIST,
						"The bucket does not exist. Please create it",
						e);
			}
			return ServiceTestResult.createFailure(
					ServiceTestStatus.UNKNOWN_ERROR,
					e.getErrorCode(),
					e);
	    } catch (Exception e) {
			return ServiceTestResult.createFailure(
					ServiceTestStatus.UNKNOWN_ERROR,
					e);
	    }
		//checking that the region in the properties match 
		//the region of the bucket
		ServiceTestResult badRegion = ServiceTestResult.createFailure(
				ServiceTestStatus.AMAZONS3_BUCKET_IN_WRONG_REGION,
				"Amazon region name is wrong (should be "+region2+")");
		badRegion.setData("amazons3region",region2);
		if (region==null) {
			return badRegion;
		}
		if (!region2.equals(region.toString())) {
			return badRegion;
		}
		
		progress.onProgress(100, "Finished");
		ServiceTestResult success = ServiceTestResult.createSuccess();
		success.setData("s3client", s3);
		return success;
	}
	
	public static void createBucket(InpherConfigProperties icp) {
		AmazonS3 s3 = null;
		if (!icp.isAmazonS3UseDefaultCredentials()) {
			s3 = new AmazonS3Client(new BasicAWSCredentials(icp.getAmazonS3AccessKeyId(), icp.getAmazonS3SecretAccessKey()));
		} else {
			s3 = new AmazonS3Client();			
		}
		Regions r = Regions.fromName(icp.getAmazonS3RegionName());
		Region region = Region.getRegion(r);
		s3.setRegion(region);
		s3.createBucket(icp.getAmazonS3BucketName());
	}

	public static ServiceTestResult testLocalStorage(InpherConfigProperties icp,
			ServiceTestProgressListener progress) {
		String rootFolderStr = icp.getLocalStorageRootFolder();
		if (isNullOrEmpty(rootFolderStr)) {
			return ServiceTestResult.createFailure(
					ServiceTestStatus.INCOMPLETE_CONFIGURATION, 
					"The Storage Root folder has not been specified");
		}
		File rootFolder = new File(rootFolderStr);
		if (!rootFolder.exists()) {
			return ServiceTestResult.createFailure(
					ServiceTestStatus.LOCALSTORAGE_ROOT_DOES_NOT_EXIST, 
					"The Storage Root folder does not exist (create it?)");
		} 
		if (!rootFolder.isDirectory()) {
			return ServiceTestResult.createFailure(
					ServiceTestStatus.INVALID_LOCALSTORAGE_ROOT, 
					"The Storage Root is not a directory");
		}
		ServiceTestResult success = ServiceTestResult.createSuccess();
		success.setData("localroot", rootFolder);
		return success;
	}


	public static void createLocalStorageRoot(String localStorageRootFolder) {
		File localRoot = new File(localStorageRootFolder);
		try { localRoot.mkdirs(); } catch (Exception e) {}		
	}
	
	/**
	 * Creates a local root folder configuration in the current directory. 
	 * 
	 * @return a local file storage configuration
	 */
	public static CloudStorageConfiguration getLocalStorageConfiguration(String rootFolderStr) {
    	Utils.getLogger().info("setting Local Storage");
		File rootFolder = new File(rootFolderStr);
		if (!rootFolder.exists()) {
			Utils.getLogger().info("root folder does not exist. creating it");
			if (!rootFolder.mkdirs()) {
				Utils.getLogger().error("error creating the root folder");
				System.exit(1);				
			}
		} else {
			if (!rootFolder.isDirectory()) {
				Utils.getLogger().error("error the specified root folder is not a directory");
				System.exit(1);								
			}
		}
		return new LocalFileStorageConfiguration(rootFolder);  
	}
	
    public static CloudStorageConfiguration getAmazonS3Configuration(InpherConfigProperties icp) {
    	Utils.getLogger().info("setting up AmazonS3 Storage");
    	ServiceTestResult s3status = Utils.testAmazonS3(icp, new SimpleServiceTestProgressListener());
    	if (s3status.getStatus()!=ServiceTestStatus.OK) {
    		System.err.println(s3status);
    		System.exit(1);
    	}
    	return new AmazonS3StorageConfiguration(s3status.getData("s3client", AmazonS3Client.class), icp.getAmazonS3BucketName());
    }
    
    public static SearchEngineConfiguration getSolrSearchEngineConfiguration(InpherConfigProperties icp) {
    	//set up solr
    	Utils.getLogger().info("testing Solr");
    	ServiceTestResult solrStatus = Utils.testSolr(icp, new SimpleServiceTestProgressListener());
    	if (solrStatus.getStatus()!=ServiceTestStatus.OK) {
    		System.err.println(solrStatus);
    		System.exit(1);
    	}
    	return new EncryptedSolrSearchEngineConfiguration(solrStatus.getData("solrclient",	HttpSolrClient.class));   
    }
	
    
	public static InpherClient generateInpherClient() {
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
		//set up SSL keystore and Truststore
    	Utils.getLogger().info("setting SSL keys");
    	if (icp.getKeyStorePath()!=null) System.setProperty("javax.net.ssl.keyStore", icp.getKeyStorePath());
    	if (icp.getKeyStorePasswd()!=null) System.setProperty("javax.net.ssl.keyStorePassword", icp.getKeyStorePasswd());
    	if (icp.getTrustStorePath()!=null) System.setProperty("javax.net.ssl.trustStore", icp.getTrustStorePath());
    	if (icp.getTrustStorePasswd()!=null) System.setProperty("javax.net.ssl.trustStorePassword", icp.getTrustStorePasswd());
    	
    	//set up Solr
    	SearchEngineConfiguration searchConfig = getSolrSearchEngineConfiguration(icp);

    	CloudStorageConfiguration cloudConfig = null;
    	if (icp.getCloudStorageType()==CloudStorageType.LOCAL_STORAGE) {
    		cloudConfig = getLocalStorageConfiguration(icp.getLocalStorageRootFolder());
    	} else {
    		cloudConfig = getAmazonS3Configuration(icp);
    	}
		try  {
    	InpherClientConfiguration inpherConfig = new InpherClientConfiguration(cloudConfig, searchConfig);
			return InpherClient.getClient(inpherConfig);
		} catch (InpherException e) {
			System.err.println("Something went wrong: please double check your parameters");
			throw new RuntimeException(e);
		}
	}

	
}
