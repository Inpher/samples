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

import static configuration.UIActivePanel.AMAZONS3_STORAGE_CONFIGURATION;
import static configuration.UIActivePanel.CLOUDSTORAGE_TYPE_CHOICE;
import static configuration.UIActivePanel.LOCAL_SOLR_CONFIGURATION;
import static configuration.UIActivePanel.LOCAL_SOLR_INSTALL;
import static configuration.UIActivePanel.LOCAL_STORAGE_CONFIGURATION;
import static configuration.UIActivePanel.REMOTE_SOLR_CONFIGURATION;
import static configuration.UIActivePanel.SOLR_TYPE_CHOICE;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import org.inpher.clientimpl.utils.CloudStorageType;
import org.inpher.clientimpl.utils.SolrInstallType;

import com.amazonaws.regions.Regions;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogEvent;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

/**
 * @author jetchev
 *
 */
public class SetupUIController extends SetupUIModel implements Initializable {	
	@FXML 	// fx:id="borderPane"
	private BorderPane borderPane;
	@FXML 	// fx:id="stackPane"
	private StackPane stackPane; 
	@FXML 	// fx:id="solrRadioPane"
	private AnchorPane solrRadioPane;
	@FXML 	// fx:id="newLocalSolrPane"
	private AnchorPane newLocalSolrPane;
	@FXML 	// fx:id="existingLocalSolrPane"
	private AnchorPane existingLocalSolrPane;
	@FXML 	// fx:id="remoteSolrPane"
	private AnchorPane remoteSolrPane;
	@FXML 	// fx:id="storageRadioPane"
	private AnchorPane storageRadioPane;
	@FXML 	// fx:id="localStoragePane"
	private AnchorPane localStoragePane;
	@FXML 	// fx:id="S3StoragePane"
	private AnchorPane S3StoragePane;
	
	@FXML 	// fx:id="nextButton"
	private Button nextButton;
	@FXML 	// fx:id="previousButton"
	private Button previousButton;
	@FXML 	// fx:id="finishButton"
	private Button finishButton;
	@FXML 	// fx:id="cancelButton"
	private Button cancelButton;
	@FXML 	// fx:id="installButton"
	private Button installButton;
	@FXML 	// fx:id="testButton"
	private Button testButton;
	
	@FXML 	// fx:id="newSolrRadioButton"
	private RadioButton newLocalSolrRadioButton;
	@FXML 	// fx:id="existingSolrRadioButton"
	private RadioButton existingLocalSolrRadioButton;
	@FXML 	// fx:id="remoteSolrRadioButton"
	private RadioButton remoteSolrRadioButton;
	
	@FXML RadioButton localStorageRadioButton; 
	@FXML RadioButton S3StorageRadioButton; 
	
	@FXML 	// fx:id="localSolrInstallLocationTextField"
	private TextField localSolrInstallLocationTextField; 
	
	@FXML private CheckBox existingLocalSolrManageCheckBox;
	@FXML private Button existingLocalSolrBrowseButton;
	@FXML private TextField existingLocalSolrUrlTextField;
	@FXML private TextField existingLocalSolrFolderTextField; 
	
	@FXML private TextField keyStorePathTextField;
	@FXML private TextField keyStorePasswordTextField;
	@FXML private TextField trustStorePathTextField;
	@FXML private TextField trustStorePasswordTextField;
	@FXML private Button keyStoreBrowseButton;
	@FXML private Button trustStoreBrowseButton;
	@FXML private TextField remoteSolrUrlTextfield;
	
	// LocalStoragePane 
	@FXML private TextField localStorageRootTextField; 
	@FXML private Button localStorageRootBrowseButton; 
	
	// S3StoragePane 
	@FXML private CheckBox defaultAWSConfigurationCheckBox; 
	@FXML private TextField awsAccessKeyTextField; 
	@FXML private TextField awsSecretKeyTextField; 
	@FXML private TextField awsBucketNameTextField; 
	@FXML private TextField awsRegionTextField; 
	
	private UIActivePanel currentPane; 
	private HashMap<UIActivePanel, AnchorPane> panes;
	
	
	private String localSolrRoot;
	private String solrURL;
	
	private Stage stage; 
	
	private ProgressDialogController progressDialogController; 
	private Dialog<Boolean> progressDialog; 
	
	/**
	 * 
	 */
	public SetupUIController() {
		panes = new HashMap<>(); 
	}

	@Override
	public CloudStorageType getCloudStorageType() {
		if (localStorageRadioButton.isSelected()) return LOCAL_STORAGE;
		if (S3StorageRadioButton.isSelected()) return AMAZONS3_STORAGE;
		return null;
	}

	@Override
	public void setCloudStorageType(CloudStorageType cloudStorageType) {
		switch(cloudStorageType) {
			case LOCAL_STORAGE:
				localStorageRadioButton.setSelected(true);
				return;
			case AMAZONS3_STORAGE: 
				S3StorageRadioButton.setSelected(true);
				return;
		}
	}

	@Override
	public String getLocalStorageRootFolder() {
		return this.localStorageRootTextField.getText(); 
	}

	@Override
	public void setLocalStorageRootFolder(String localStorageRootFolder) {
		this.localStorageRootTextField.setText(localStorageRootFolder); 
	}

	@Override
	public Boolean isAmazonS3UseDefaultCredentials() {
		if (this.defaultAWSConfigurationCheckBox.isSelected()) {
			return true; 
		}
		return false;
	}

	@Override
	public void setAmazonS3UseDefaultCredentials(Boolean amazonS3UseDefaultCredentials) {
		this.defaultAWSConfigurationCheckBox.setSelected(amazonS3UseDefaultCredentials);
		onDefaultAWSConfiguration();
	}

	@Override
	public String getAmazonS3AccessKeyId() {
		return awsAccessKeyTextField.getText();
	}

	@Override
	public void setAmazonS3AccessKeyId(String amazonS3AccessKeyId) {
		awsAccessKeyTextField.setText(amazonS3AccessKeyId);
	}

	@Override
	public String getAmazonS3SecretAccessKey() {
		return awsSecretKeyTextField.getText();
	}

	@Override
	public void setAmazonS3SecretAccessKey(String amazonS3SecretAccessKey) {
		awsSecretKeyTextField.setText(amazonS3SecretAccessKey);
	}

	private String amazonS3RegionName;
	@Override
	public String getAmazonS3RegionName() {
		return this.amazonS3RegionName;
	}

	@Override
	public void setAmazonS3RegionName(String amazonS3RegionName) {
		this.amazonS3RegionName=amazonS3RegionName;
	}

	@Override
	public String getAmazonS3BucketName() {
		return awsBucketNameTextField.getText();
	}

	@Override
	public void setAmazonS3BucketName(String amazonS3BucketName) {
		awsBucketNameTextField.setText(amazonS3BucketName);
	}

	@Override
	public SolrInstallType getSolrInstallationType() {
		if (newLocalSolrRadioButton.isSelected()) return INSTALL_SOLR_LOCALLY;
		if (existingLocalSolrRadioButton.isSelected()) return LOCAL_SOLR;
		if (remoteSolrRadioButton.isSelected()) return REMOTE_SOLR;
		return null;
	}

	@Override
	public void setSolrInstallationType(SolrInstallType solrInstallationType) {
		switch(solrInstallationType) {
		case INSTALL_SOLR_LOCALLY: newLocalSolrRadioButton.setSelected(true); return;
		case LOCAL_SOLR: existingLocalSolrRadioButton.setSelected(true); return;
		case REMOTE_SOLR: remoteSolrRadioButton.setSelected(true); return;
		}
	}

	@Override
	public String getSolrURL() {
		if (currentPane==LOCAL_SOLR_CONFIGURATION) 
			solrURL=this.existingLocalSolrUrlTextField.getText();
		else if (currentPane==REMOTE_SOLR_CONFIGURATION) 
			solrURL=this.remoteSolrUrlTextfield.getText();
		return solrURL;
	}

	@Override
	public void setSolrURL(String solrURL) {
		this.solrURL=solrURL;
		this.existingLocalSolrUrlTextField.setText(solrURL);
		this.remoteSolrUrlTextfield.setText(solrURL);
	}

	@Override
	public String getLocalSolrRootFolder() {
		if (currentPane==UIActivePanel.LOCAL_SOLR_INSTALL)
			localSolrRoot=this.localSolrInstallLocationTextField.getText();
		if (currentPane==UIActivePanel.LOCAL_SOLR_CONFIGURATION) 
			localSolrRoot=this.existingLocalSolrFolderTextField.getText();
		return localSolrRoot; 
	}

	@Override
	public void setLocalSolrRootFolder(String localSolrRootFolder) {
		this.localSolrRoot=localSolrRootFolder;
		this.localSolrInstallLocationTextField.setText(localSolrRootFolder);
		this.existingLocalSolrFolderTextField.setText(localSolrRootFolder);
	}

	@Override
	public Boolean isAutoRebootSolr() {
		return existingLocalSolrManageCheckBox.isSelected();
	}

	@Override
	public void setAutoRebootSolr(Boolean autoRebootSolr) {
		existingLocalSolrManageCheckBox.setSelected(autoRebootSolr!=null && autoRebootSolr);
	}

	@Override
	public String getKeyStorePath() {
		return keyStorePathTextField.getText();
	}

	@Override
	public void setKeyStorePath(String keyStorePath) {
		keyStorePathTextField.setText(keyStorePath);
	}

	@Override
	public String getKeyStorePasswd() {
		return keyStorePasswordTextField.getText();
	}

	@Override
	public void setKeyStorePasswd(String keyStorePasswd) {
		keyStorePasswordTextField.setText(keyStorePasswd);
	}

	@Override
	public String getTrustStorePath() {
		return trustStorePathTextField.getText();
	}

	@Override
	public void setTrustStorePath(String trustStorePath) {
		trustStorePathTextField.setText(trustStorePath);
	}

	@Override
	public String getTrustStorePasswd() {
		return trustStorePasswordTextField.getText();
	}

	@Override
	public void setTrustStorePasswd(String trustStorePasswd) {
		trustStorePasswordTextField.setText(trustStorePasswd);
	}
	
	@Override
	public UIActivePanel getActivePanel() {
		return currentPane;
	}

	@Override
	public void setActivePanel(UIActivePanel panel) {
		System.out.println("Switching from " + currentPane + " to " + panel);
		AnchorPane nextPane = panes.get(panel);
		AnchorPane currentPanel = panes.get(currentPane);
		if (nextPane==currentPanel) return;
		if (currentPanel!=null) currentPanel.setVisible(false);
		if ((panel == LOCAL_STORAGE_CONFIGURATION) || (panel == AMAZONS3_STORAGE_CONFIGURATION)) {
			finishButton.setVisible(true);
			nextButton.setDisable(true);
		} else {
			finishButton.setVisible(false);
			nextButton.setDisable(false);			
		}
		if (panel == SOLR_TYPE_CHOICE) {
		    previousButton.setDisable(true);
		} else {
		    previousButton.setDisable(false);			
		}
		nextPane.setVisible(true);
		currentPane=panel;
	}

	@Override
	public void exitNicely() {
		Platform.exit();
	}

	@Override
	public void showErrorAlert(String message) {
		Alert alert = new Alert(AlertType.ERROR);
		alert.setContentText(message);
		alert.showAndWait();		
	}

	@Override
	public void showInfoAlert(String message) {
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setContentText(message);
		alert.showAndWait();		
	}

	@Override
	public void showProgressPopup(String message) {
		progressDialog.show();
		progressDialog.setHeaderText(message);
	}

	@Override
	public void updateProgressPopup(double percent, String message) {
		progressDialogController.updateProgressBar(percent); 
		progressDialogController.changeLabel(message);
	}

	@Override
	public void hideProgressPopup() {
		progressDialog.close(); 
	}

	private ChoiceDialog<String> createS3BucketDialog = null;
	@Override
	public String showAndWaitCreateAmazonS3BucketDialog(String bucketName) {
		if (createS3BucketDialog==null) {
		List<String> choices = new ArrayList<>();
		for (Regions e: Regions.values())
			choices.add(e.getName());
		createS3BucketDialog = new ChoiceDialog<>("eu-central-1",choices);
		createS3BucketDialog.setTitle("Create Amazon S3 Bucket");
		createS3BucketDialog.setContentText("Choose a region");
		}
		createS3BucketDialog.setHeaderText("In which region would you like to create the bucket "+bucketName+"?");
		Optional<String> res = createS3BucketDialog.showAndWait();
		if (res.isPresent()) return res.get();
		return null;
	}
	
	private Alert confirmS3RegionDialog = null;
	@Override
	public boolean showAndWaitConfirmAmazonS3BucketRegionDialog(String region) {
		if (confirmS3RegionDialog==null) {
			confirmS3RegionDialog = new Alert(AlertType.CONFIRMATION);
			confirmS3RegionDialog.setTitle("Confirm bucket region");
			confirmS3RegionDialog.setContentText("You must have read/write permission on this bucket?");
		}
		confirmS3RegionDialog.setHeaderText("Use the existing AmazonS3 bucket "+getAmazonS3BucketName()+" in region "+region+"?");
		Optional<ButtonType> res = confirmS3RegionDialog.showAndWait();
		return res.isPresent() && res.get()==ButtonType.OK;
	}

	private Alert confirmLocalStorageMkdir = null;
	@Override
	public boolean showAndWaitConfirmLocalStorageMkdirDialog(String directory) {
		if (confirmLocalStorageMkdir==null) {
			confirmLocalStorageMkdir = new Alert(AlertType.CONFIRMATION);
			confirmLocalStorageMkdir.setContentText("You must have read/write permission on this folder");
		}
		confirmLocalStorageMkdir.setHeaderText("Do you want to create the new directory "+directory+"?");
		Optional<ButtonType> res = confirmLocalStorageMkdir.showAndWait();
		return res.isPresent() && res.get()==ButtonType.OK;
	}
	
	/**
	 * @param stage
	 */
	public void setStage(Stage stage) {
		this.stage = stage; 
	}
	
	/**
	 * Button Handlers 
	 */
	
	public void onNext() {
		super.showNextPage();
	}
	
	public void onPrevious() {
		super.showPreviousPage();
	}
	
	public void onFinish() {
		super.showNextPage();
	}
	
	public void onCancel() {
		Platform.exit(); 
	}
	
	public void onBrowse() {
		DirectoryChooser dirChooser = new DirectoryChooser();
		dirChooser.setTitle("Select directory to install Solr.");
		File installDir = dirChooser.showDialog(stage); 
		if (installDir != null) {
			setLocalSolrRootFolder(installDir.toString());
		}
	}
	
	public void onExistingLocalSolrBrowseButton() {
		DirectoryChooser dirChooser = new DirectoryChooser();
		dirChooser.setTitle("Select existing local Solr folder.");
		File existingLocalSolrDir = dirChooser.showDialog(stage); 
		if (existingLocalSolrDir != null) {
			setLocalSolrRootFolder(existingLocalSolrDir.toString());
		}
	}
	
	public void onLocalStorageRootBrowseButton() {
		DirectoryChooser dirChooser = new DirectoryChooser();
		dirChooser.setTitle("Select local storage root folder.");
		File localRootDir = dirChooser.showDialog(stage); 
		if (localRootDir != null) {
			setLocalStorageRootFolder(localRootDir.toString());
		}
	}
	
	public void onInstallSolr() {	
		doInstallSolrLocally(); 
	}
	
	public void onTestSolr() {
	}
	
	/**
	 * Radio Button Handlers 
	 */
	public void onNewLocalSolrSelection() {
		nextButton.setDisable(false);
	}
	
	public void onExistingLocalSolrSelection() {
		nextButton.setDisable(false);
	}
	
	public void onRemoteSolrSelection() {
		nextButton.setDisable(false);
	}
	
	public void onLocalDataStorageSelection() {
		nextButton.setDisable(false);
	}
	
	public void onS3StorageSelection() {
		nextButton.setDisable(false);
	}
	
	/**
	 * CheckBox handlers
	 */
	public void onDefaultAWSConfiguration() {
		if (this.defaultAWSConfigurationCheckBox.isSelected()) {
			this.awsAccessKeyTextField.setDisable(true);
			this.awsSecretKeyTextField.setDisable(true);
			return; 
		} 
		this.awsAccessKeyTextField.setDisable(false);
		this.awsSecretKeyTextField.setDisable(false);
	}
	
	/**
	 * Text Field Handlers 
	 */
	public void onChange() {
	}
	
	public void onProgressDialogClose(DialogEvent e) {
		super.killActiveThread();
	}
	
	
	/**
	 * 
	 * @param location
	 * @param resources
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		for (Field f : SetupUIController.class.getDeclaredFields()) {
			if (f.getAnnotation(FXML.class)!=null) {
				try {
					if (f.get(this)==null)
						System.out.println("ERROR: Field "+f.getName()+": "+f.get(this));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		panes.put(SOLR_TYPE_CHOICE, this.solrRadioPane);
		panes.put(REMOTE_SOLR_CONFIGURATION, this.remoteSolrPane);
		panes.put(LOCAL_SOLR_CONFIGURATION, this.existingLocalSolrPane);
		panes.put(LOCAL_SOLR_INSTALL, this.newLocalSolrPane);
		panes.put(CLOUDSTORAGE_TYPE_CHOICE, this.storageRadioPane);
		panes.put(LOCAL_STORAGE_CONFIGURATION, this.localStoragePane);
		panes.put(AMAZONS3_STORAGE_CONFIGURATION,this.S3StoragePane);
		for (AnchorPane pane : panes.values()) {
			pane.setVisible(false);
		}
		super.initializeSetup();
	}

	public ProgressDialogController getProgressDialogController() {
		return progressDialogController;
	}

	public void setProgressDialogController(ProgressDialogController progressDialogController) {
		this.progressDialogController = progressDialogController;
	}

	public Dialog<Boolean> getProgressDialog() {
		return progressDialog;
	}

	public void setProgressDialog(Dialog<Boolean> progressDialog) {
		this.progressDialog = progressDialog;
	}
}