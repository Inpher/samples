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
package configuration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.amazonaws.regions.Regions;

/**
 *
 */
public class InpherConfigProperties {
	public static final CloudStorageType LOCAL_STORAGE=CloudStorageType.LOCAL_STORAGE;
	public static final CloudStorageType AMAZONS3_STORAGE=CloudStorageType.AMAZONS3_STORAGE;
	public static final SolrInstallType LOCAL_SOLR=SolrInstallType.LOCAL_SOLR;
	public static final SolrInstallType REMOTE_SOLR=SolrInstallType.REMOTE_SOLR;

 	private CloudStorageType cloudStorageType;
    
    private String localStorageRootFolder;
    
    private Boolean amazonS3UseDefaultCredentials;
    private String amazonS3AccessKeyId;
    private String amazonS3SecretAccessKey;
    private String amazonS3RegionName;
    private String amazonS3BucketName;
    
    private SolrInstallType solrInstallationType;
    private String solrURL;
    
    private String localSolrRootFolder;
    private Boolean autoRebootSolr; // if solr is locally installed, shall we automatically start it if it is not started?
    
    private String keyStorePath; // jks file
    private String keyStorePasswd;
    private String trustStorePath; // jks file
    private String trustStorePasswd;
    
    public InpherConfigProperties() {
    	//set default values
    	this.cloudStorageType=LOCAL_STORAGE;
    	this.localStorageRootFolder="./demorootfolder";
    	this.amazonS3UseDefaultCredentials=true;
    	this.solrInstallationType=LOCAL_SOLR;
    	this.autoRebootSolr=true;
    	this.solrURL="http://localhost:8983/solr/inpher-frequency";
    }

	public CloudStorageType getCloudStorageType() {
		return cloudStorageType;
	}

	public void setCloudStorageType(CloudStorageType cloudStorageType) {
		this.cloudStorageType = cloudStorageType;
	}

	public String getLocalStorageRootFolder() {
		return localStorageRootFolder;
	}

	public void setLocalStorageRootFolder(String localStorageRootFolder) {
		this.localStorageRootFolder = localStorageRootFolder;
	}

	public Boolean isAmazonS3UseDefaultCredentials() {
		return amazonS3UseDefaultCredentials;
	}

	public void setAmazonS3UseDefaultCredentials(Boolean amazonS3UseDefaultCredentials) {
		this.amazonS3UseDefaultCredentials = amazonS3UseDefaultCredentials;
	}

	public String getAmazonS3AccessKeyId() {
		return amazonS3AccessKeyId;
	}

	public void setAmazonS3AccessKeyId(String amazonS3AccessKeyId) {
		this.amazonS3AccessKeyId = amazonS3AccessKeyId;
	}

	public String getAmazonS3SecretAccessKey() {
		return amazonS3SecretAccessKey;
	}

	public void setAmazonS3SecretAccessKey(String amazonS3SecretAccessKey) {
		this.amazonS3SecretAccessKey = amazonS3SecretAccessKey;
	}

	public Boolean isAutoRebootSolr() {
		return autoRebootSolr;
	}

	public void setAutoRebootSolr(Boolean autoRebootSolr) {
		this.autoRebootSolr = autoRebootSolr;
	}

	public String getAmazonS3RegionName() {
		return amazonS3RegionName;
	}

	public void setAmazonS3RegionName(String amazonS3RegionName) {
		this.amazonS3RegionName = amazonS3RegionName;
	}

	public String getAmazonS3BucketName() {
		return amazonS3BucketName;
	}

	public void setAmazonS3BucketName(String amazonS3BucketName) {
		this.amazonS3BucketName = amazonS3BucketName;
	}

	public SolrInstallType getSolrInstallationType() {
		return solrInstallationType;
	}

	public void setSolrInstallationType(SolrInstallType solrInstallationType) {
		this.solrInstallationType = solrInstallationType;
	}

	public String getSolrURL() {
		return solrURL;
	}

	public void setSolrURL(String solrURL) {
		this.solrURL = solrURL;
	}

	public String getLocalSolrRootFolder() {
		return localSolrRootFolder;
	}

	public void setLocalSolrRootFolder(String localSolrRootFolder) {
		this.localSolrRootFolder = localSolrRootFolder;
	}

	public String getKeyStorePath() {
		return keyStorePath;
	}

	public void setKeyStorePath(String keyStorePath) {
		this.keyStorePath = keyStorePath;
	}

	public String getKeyStorePasswd() {
		return keyStorePasswd;
	}

	public void setKeyStorePasswd(String keyStorePasswd) {
		this.keyStorePasswd = keyStorePasswd;
	}

	public String getTrustStorePath() {
		return trustStorePath;
	}

	public void setTrustStorePath(String trustStorePath) {
		this.trustStorePath = trustStorePath;
	}

	public String getTrustStorePasswd() {
		return trustStorePasswd;
	}

	public void setTrustStorePasswd(String trustStorePasswd) {
		this.trustStorePasswd = trustStorePasswd;
	}
	
	
	/**
	 * 
	 * @param configFile
	 */
	public void saveToFile(File configFile) throws Exception {
		FileOutputStream fos = new FileOutputStream(configFile);
		PrintWriter bw = new PrintWriter(new OutputStreamWriter(fos,StandardCharsets.UTF_8));
		for (Field field : getClass().getDeclaredFields()) {
			if (Modifier.isStatic(field.getModifiers())) continue;
			String val = String.valueOf(field.get(this));
			String key = field.getName();
			if (val.equals("null"))
				bw.println("#"+key+"=");
			else
				bw.println(key + "=" + val);
		}
		bw.close();
	}
	
	/**
	 * 
	 * @param configFile
	 * @return
	 */
	public static InpherConfigProperties fromConfigFile(File configFile) throws Exception {
		InpherConfigProperties configProperties = new InpherConfigProperties(); 		
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(configFile),StandardCharsets.UTF_8));
		for (String line = br.readLine(); line!=null; line=br.readLine()) {
			if (line.matches("^\\s*$")) continue; //ignore empty lines
			if (line.matches("^\\s*#.*")) continue; //ignore comments
			Pattern p = Pattern.compile("^\\s*([a-zA-Z0-9_]+)\\s*=(.*)$");
			Matcher m = p.matcher(line);
			if (m.matches()) {
				String key = m.group(1);
				String valueStr = m.group(2);
				System.out.println("Got ["+key+"] = ["+valueStr+"]");
				try {
					Field field = InpherConfigProperties.class.getDeclaredField(key);
					Class<?> t = field.getType();
					if (t.equals(String.class)) 
						field.set(configProperties, valueStr);
					/*
					else if (t.equals(int.class)) 
						field.set(configProperties, Integer.valueOf(valueStr));
					else if (t.equals(boolean.class)) 
						field.set(configProperties, Boolean.valueOf(valueStr));
					else if (t.equals(float.class))
						field.set(configProperties, Float.valueOf(valueStr));
					else if (t.equals(long.class))
						field.set(configProperties, Long.valueOf(valueStr));
					else if (t.equals(double.class))
						field.set(configProperties, Double.valueOf(valueStr));
					*/
					else {
						Method valueOfMethod = t.getMethod("valueOf", String.class);
						field.set(configProperties, valueOfMethod.invoke(null, valueStr));
					}
				} catch (Exception e) {
					br.close();
					throw new RuntimeException(e);
				}
			} else {
				System.err.println("Impossible to parse the line "+line);
			}
		}
		br.close();
		return configProperties; 
	}
	
	public static void main(String[] args) throws Exception {
		InpherConfigProperties props  = new InpherConfigProperties();
		props.setCloudStorageType(AMAZONS3_STORAGE);
		props.setAmazonS3RegionName(Regions.EU_CENTRAL_1.getName());
		File f1 = new File("/dev/stdout");
		File f2 = new File("config.properties");
		props.saveToFile(f1);
		props.saveToFile(f2);
		InpherConfigProperties props2 = InpherConfigProperties.fromConfigFile(f2);
		props2.saveToFile(f1);
	}
}