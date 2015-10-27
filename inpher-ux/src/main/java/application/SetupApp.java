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

import configuration.ProgressDialogController;
import configuration.SetupUIController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * @author jetchev
 *
 */
public class SetupApp extends Application {
	private Stage primaryStage; 
	private BorderPane layout; 
	private BorderPane dialogLayout; 
	
	public SetupApp() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		// TODO Auto-generated method stub
		this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Inpher GUI Sample Application Setup");
        
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/setupapp.fxml"));
        this.layout = (BorderPane) loader.load(); 
        
        FXMLLoader loader2 = new FXMLLoader();
        loader2.setLocation(getClass().getResource("/dialog-progress.fxml"));
        this.dialogLayout = (BorderPane) loader2.load();
        
        SetupUIController controller = (SetupUIController) loader.getController(); 
        controller.setStage(primaryStage); 
        
        ProgressDialogController progressDialogController = (ProgressDialogController) loader2.getController(); 
                
        Scene scene = new Scene(layout);
        Dialog<Boolean> progressDialog = new Dialog<>();
		progressDialog.setTitle("Operation in progress");
		progressDialog.setHeaderText("Installing Local Solr");
		progressDialog.getDialogPane().getButtonTypes().clear();
		progressDialog.getDialogPane().setContent(dialogLayout);
		
		// Scene dialogScene = new Scene(dialogLayout); 
		progressDialog.getDialogPane().getButtonTypes().add(new ButtonType("Cancel", ButtonData.CANCEL_CLOSE));
		progressDialog.setOnCloseRequest(controller::onProgressDialogClose);    
        
		controller.setProgressDialogController(progressDialogController);
		controller.setProgressDialog(progressDialog); 
		progressDialogController.setProgressDialog(progressDialog);
		
        primaryStage.setScene(scene);
        primaryStage.show();
	}
	
	public static void main(String[] args) {
		launch(args); 
    }
}
