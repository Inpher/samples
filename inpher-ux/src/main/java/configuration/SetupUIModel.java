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

package configuration;

import java.io.File;

import org.inpher.clientapi.InpherProgressListener;
import org.inpher.clientimpl.utils.AutoconfInpherClientUtils;
import org.inpher.clientimpl.utils.CloudStorageType;
import org.inpher.clientimpl.utils.DefaultInpherProgressListener;
import org.inpher.clientimpl.utils.InpherConfigProperties;
import org.inpher.clientimpl.utils.ServiceTestResult;
import org.inpher.clientimpl.utils.ServiceTestStatus;
import org.inpher.clientimpl.utils.SolrInstallType;

import application.KillableThread;
import javafx.application.Platform;

//In the control panel, we will use the logical state of the GUI to store the logical state of the application
public abstract class SetupUIModel {
		public static final CloudStorageType LOCAL_STORAGE=CloudStorageType.LOCAL_STORAGE;
		public static final CloudStorageType AMAZONS3_STORAGE=CloudStorageType.AMAZONS3_STORAGE;
		public static final SolrInstallType LOCAL_SOLR=SolrInstallType.LOCAL_SOLR;
		public static final SolrInstallType REMOTE_SOLR=SolrInstallType.REMOTE_SOLR;
		public static final SolrInstallType INSTALL_SOLR_LOCALLY=SolrInstallType.INSTALL_SOLR_LOCALLY;

		public InpherProgressListener uiProgressListener;
		
		public abstract UIActivePanel getActivePanel();
		public abstract void setActivePanel(UIActivePanel panel);
		public abstract CloudStorageType getCloudStorageType();
		public abstract void setCloudStorageType(CloudStorageType cloudStorageType);
		public abstract String getLocalStorageRootFolder();
		public abstract void setLocalStorageRootFolder(String localStorageRootFolder);
		public abstract Boolean isAmazonS3UseDefaultCredentials();
		public abstract void setAmazonS3UseDefaultCredentials(Boolean amazonS3UseDefaultCredentials);
		public abstract String getAmazonS3AccessKeyId();
		public abstract void setAmazonS3AccessKeyId(String amazonS3AccessKeyId);
		public abstract String getAmazonS3SecretAccessKey();
		public abstract void setAmazonS3SecretAccessKey(String amazonS3SecretAccessKey);
		public abstract String getAmazonS3RegionName();
		public abstract void setAmazonS3RegionName(String amazonS3RegionName);
		public abstract String getAmazonS3BucketName();
		public abstract void setAmazonS3BucketName(String amazonS3BucketName);
		public abstract SolrInstallType getSolrInstallationType();
		public abstract void setSolrInstallationType(SolrInstallType solrInstallationType);
		public abstract String getSolrURL();
		public abstract void setSolrURL(String solrURL);
		public abstract String getLocalSolrRootFolder();
		public abstract void setLocalSolrRootFolder(String localSolrRootFolder);
		public abstract Boolean isAutoRebootSolr();
		public abstract void setAutoRebootSolr(Boolean autoRebootSolr);
		public abstract String getKeyStorePath();
		public abstract void setKeyStorePath(String keyStorePath);
		public abstract String getKeyStorePasswd();
		public abstract void setKeyStorePasswd(String keyStorePasswd);
		public abstract String getTrustStorePath();
		public abstract void setTrustStorePath(String trustStorePath);
		public abstract String getTrustStorePasswd();
		public abstract void setTrustStorePasswd(String trustStorePasswd);
        public abstract void exitNicely();
        public abstract void showErrorAlert(String message);
        public abstract void showInfoAlert(String message);
        public abstract void showProgressPopup(String message);
        public abstract void updateProgressPopup(double percent, String message);
        public abstract void hideProgressPopup();
        public abstract String showAndWaitCreateAmazonS3BucketDialog(String bucketName);
        public abstract boolean showAndWaitConfirmAmazonS3BucketRegionDialog(String region);
        public abstract boolean showAndWaitConfirmLocalStorageMkdirDialog(String directory);
        
        public SetupUIModel() {
        	uiProgressListener = new DefaultInpherProgressListener() {
        	    @Override
        		public void onProgress(double progress, String status) {
        	    	super.onProgress(progress, status);
        	    	Platform.runLater(() -> updateProgressPopup(100*progress, status));
        	    }
        	};
        }
        
		public void loadEverythingFrom(InpherConfigProperties icp) {
			setCloudStorageType(icp.getCloudStorageType());
			setLocalStorageRootFolder(icp.getLocalStorageRootFolder());
			setAmazonS3UseDefaultCredentials(icp.isAmazonS3UseDefaultCredentials());
			setAmazonS3AccessKeyId(icp.getAmazonS3AccessKeyId());
			setAmazonS3SecretAccessKey(icp.getAmazonS3SecretAccessKey());
			setAmazonS3RegionName(icp.getAmazonS3RegionName());
			setAmazonS3BucketName(icp.getAmazonS3BucketName());
			setSolrInstallationType(icp.getSolrInstallationType());
			setSolrURL(icp.getSolrURL());
			setLocalSolrRootFolder(icp.getLocalSolrRootFolder());
			setAutoRebootSolr(icp.isAutoRebootSolr());
			setKeyStorePath(icp.getKeyStorePath());
			setKeyStorePasswd(icp.getKeyStorePasswd());
			setTrustStorePath(icp.getTrustStorePath());
			setTrustStorePasswd(icp.getTrustStorePasswd());
		}
		
		public void saveEverythingTo(InpherConfigProperties icp) {
			icp.setCloudStorageType(getCloudStorageType());
			icp.setLocalStorageRootFolder(getLocalStorageRootFolder());
			icp.setAmazonS3UseDefaultCredentials(isAmazonS3UseDefaultCredentials());
			icp.setAmazonS3AccessKeyId(getAmazonS3AccessKeyId());
			icp.setAmazonS3SecretAccessKey(getAmazonS3SecretAccessKey());
			icp.setAmazonS3RegionName(getAmazonS3RegionName());
			icp.setAmazonS3BucketName(getAmazonS3BucketName());
			icp.setSolrInstallationType(getSolrInstallationType());
			icp.setSolrURL(getSolrURL());
			icp.setLocalSolrRootFolder(getLocalSolrRootFolder());
			icp.setAutoRebootSolr(isAutoRebootSolr());
			icp.setKeyStorePath(getKeyStorePath());
			icp.setKeyStorePasswd(getKeyStorePasswd());
			icp.setTrustStorePath(getTrustStorePath());
			icp.setTrustStorePasswd(getTrustStorePasswd());
		}
		public InpherConfigProperties getEverything() {
			InpherConfigProperties icp = new InpherConfigProperties();
			saveEverythingTo(icp);
			return icp;
		}
		
		/**
		 * 
		 */
		protected void showNextPage() {
			switch (getActivePanel()) {			
			case SOLR_TYPE_CHOICE:
				switch (getSolrInstallationType()) {
				case LOCAL_SOLR:
					setActivePanel(UIActivePanel.LOCAL_SOLR_CONFIGURATION);
					return;
				case REMOTE_SOLR:
					setActivePanel(UIActivePanel.REMOTE_SOLR_CONFIGURATION);
					return;
				case INSTALL_SOLR_LOCALLY:
					setActivePanel(UIActivePanel.LOCAL_SOLR_INSTALL);
					return;
				default:
					return;
				}
			case LOCAL_SOLR_CONFIGURATION:
				doTestLocalSolrConfiguration();
				return;
			case REMOTE_SOLR_CONFIGURATION:
				doTestRemoteSolrConfiguration();
				return;
			case LOCAL_SOLR_INSTALL:
				doInstallSolrLocally();
				return;
			case CLOUDSTORAGE_TYPE_CHOICE:
				switch(getCloudStorageType()) {
				case LOCAL_STORAGE:
					setActivePanel(UIActivePanel.LOCAL_STORAGE_CONFIGURATION);
					return;
				case AMAZONS3_STORAGE:
					setActivePanel(UIActivePanel.AMAZONS3_STORAGE_CONFIGURATION);
				default:
					return;
				}
			case LOCAL_STORAGE_CONFIGURATION:
				doTestLocalStorage();
				return;
			case AMAZONS3_STORAGE_CONFIGURATION:
				doTestAmazonS3Storage();
				return;
			default:
				return;
			}
		}
		/**
		 * 
		 */
		protected void showPreviousPage() {
			switch (getActivePanel()) {			
			case SOLR_TYPE_CHOICE: return;
			case LOCAL_SOLR_CONFIGURATION:
			case REMOTE_SOLR_CONFIGURATION:
			case LOCAL_SOLR_INSTALL:
			case CLOUDSTORAGE_TYPE_CHOICE:
				setActivePanel(UIActivePanel.SOLR_TYPE_CHOICE);
				return;
			case LOCAL_STORAGE_CONFIGURATION:
			case AMAZONS3_STORAGE_CONFIGURATION:
				setActivePanel(UIActivePanel.CLOUDSTORAGE_TYPE_CHOICE);
			default:
				return;
			}
		}
		
		protected void doInstallSolrLocally() {
			showProgressPopup("Downloading and installing Solr Locally");
			InpherConfigProperties icp = getEverything();
			asyncRun(new KillableThread() {
				public void run() {
					ServiceTestResult installStatus = LocalSolrInstaller.downloadAndUnzipSolrAndPatches(icp, uiProgressListener);
					Platform.runLater(() -> {
					hideProgressPopup();						
					if (installStatus.getStatus()==ServiceTestStatus.OK) {
						setSolrURL(LocalSolrInstaller.solrInstallBaseUrl);
						setAutoRebootSolr(true);
						setSolrInstallationType(LOCAL_SOLR);
						setActivePanel(UIActivePanel.CLOUDSTORAGE_TYPE_CHOICE);
						return;
					}
					System.out.println(installStatus);
					showErrorAlert("There was an error: "+installStatus.getStatusMessage());
					});
				}
			});
		}
		
		private void doTestRemoteSolrConfiguration() {
			showProgressPopup("Testing Remote Solr Configuration");
			InpherConfigProperties icp = getEverything();
			asyncRun(new KillableThread() {
				public void run() {
					ServiceTestResult solrStatus = AutoconfInpherClientUtils.testSolr(icp, uiProgressListener);
					Platform.runLater(() -> {
					hideProgressPopup();
					if (solrStatus.getStatus()==ServiceTestStatus.OK) {
						setActivePanel(UIActivePanel.CLOUDSTORAGE_TYPE_CHOICE);
						return;
					}
					System.out.println(solrStatus);
					showErrorAlert("There was an error: "+solrStatus.getStatusMessage());
					}); 
				}
			});
		}
		
		private void doTestLocalSolrConfiguration() {
			showProgressPopup("Testing Local Solr Configuration");
			InpherConfigProperties icp = getEverything();
			asyncRun(new KillableThread() {
				public void run() {
					ServiceTestResult solrStatus = AutoconfInpherClientUtils.testSolr(icp, uiProgressListener);
					Platform.runLater(() -> {
					hideProgressPopup();
					if (solrStatus.getStatus() == ServiceTestStatus.OK) {
						setActivePanel(UIActivePanel.CLOUDSTORAGE_TYPE_CHOICE);
						return;
					}
					System.out.println(solrStatus);
					showErrorAlert("There was an error: " + solrStatus.getStatusMessage());
					}); 
				}
			});
		}
		
		private void doTestLocalStorage() {
			showProgressPopup("Testing Local Configuration");
			InpherConfigProperties icp = getEverything();
			asyncRun(new KillableThread() {
				public void run() {
					ServiceTestResult localstatus = AutoconfInpherClientUtils.testLocalStorage(icp, uiProgressListener);
					Platform.runLater(() -> {
					hideProgressPopup();
					if (localstatus.getStatus()==ServiceTestStatus.OK) {
						saveConfigurationAndExit(icp);
						return;
					}
					if (localstatus.getStatus()==ServiceTestStatus.LOCALSTORAGE_ROOT_DOES_NOT_EXIST) {
						boolean b = showAndWaitConfirmLocalStorageMkdirDialog(icp.getLocalStorageRootFolder());
						if (b) {
							AutoconfInpherClientUtils.createLocalStorageRoot(icp.getLocalStorageRootFolder());
							saveConfigurationAndExit(icp);
						}
						return;
					}
					System.out.println(localstatus);
					showErrorAlert("Incorrect Local configuration! "+localstatus.getStatusMessage());
					}); 
				}
			}); 
		}
		
		private void doTestAmazonS3Storage() {
			showProgressPopup("Testing Amazon S3 Configuration");
			InpherConfigProperties icp = getEverything();
			asyncRun(new KillableThread() {
				public void run() {
					ServiceTestResult s3status = AutoconfInpherClientUtils.testAmazonS3(icp, uiProgressListener);
					Platform.runLater(() -> {
					hideProgressPopup();
					if (s3status.getStatus()==ServiceTestStatus.OK) {
						saveConfigurationAndExit(icp);
						return;
					}
					if (s3status.getStatus()==ServiceTestStatus.AMAZONS3_BUCKET_IN_WRONG_REGION) {
						String actualRegion = s3status.getData("amazons3region",String.class);
						boolean b = showAndWaitConfirmAmazonS3BucketRegionDialog(actualRegion);
						if (b) {
							setAmazonS3RegionName(actualRegion);
							icp.setAmazonS3RegionName(actualRegion);
							saveConfigurationAndExit(icp);
							return;
						}
					}
					if (s3status.getStatus()==ServiceTestStatus.AMAZONS3_BUCKET_DOES_NOT_EXIST) {
						String bucketName = icp.getAmazonS3BucketName();
						String region = showAndWaitCreateAmazonS3BucketDialog(bucketName);
						if (region!=null) {
							icp.setAmazonS3RegionName(region);
							AutoconfInpherClientUtils.createBucket(icp);
							return;
						}
						return;
					}			
					System.out.println(s3status);
					showErrorAlert("Incorrect S3 configuration! "+s3status.getStatusMessage());	
					}); 
				}
			}); 
		}
		
		private void saveConfigurationAndExit(InpherConfigProperties icp) {
			try {
				icp.saveToFile(new File("config.properties"));
				showInfoAlert("Configuration Complete!");
				Platform.exit();
				return;
			} catch (Exception e) {
				e.printStackTrace();
				showErrorAlert("Error saving the configuration!");
				return;	
			}
		}
		public void initializeSetup() {
			InpherConfigProperties icp;
			try {
				File configFile = new File("config.properties");
				icp = InpherConfigProperties.fromConfigFile(configFile);
			} catch (Exception e) {
				icp = new InpherConfigProperties();	
			}
			loadEverythingFrom(icp);
			setActivePanel(UIActivePanel.SOLR_TYPE_CHOICE);
		}
		
		public void killActiveThread() {
			KillableThread.runningThread.requestKill();
			// KillableThread.runningThread.join(); 
		}
		
		private void asyncRun(KillableThread x) {
			if (KillableThread.runningThread!=null) {
				KillableThread.runningThread.requestKill();
				try {
					KillableThread.runningThread.join();
				} catch (InterruptedException e) {}
				KillableThread.runningThread=null;
			}
			KillableThread.runningThread=x;
			x.start();
		}
}
