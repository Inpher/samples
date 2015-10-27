/**
 * 
 */
package configuration;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

/**
 * 
 */
public class ProgressDialogController implements Initializable {
	@FXML Label installStateLabel; 
	@FXML ProgressBar solrInstallProgressBar; 
	
	private Dialog<Boolean> progressDialog; 
	
	public Dialog<Boolean> getProgressDialog() {
		return progressDialog;
	}

	public void setProgressDialog(Dialog<Boolean> progressDialog) {
		this.progressDialog = progressDialog;
	}

	/**
	 * 
	 */
	public ProgressDialogController() {
		// TODO Auto-generated constructor stub
	}
	
	public void changeLabel(String label) {
		this.installStateLabel.setText(label); 
	}
	
	public void updateProgressBar(double percent) {
		this.solrInstallProgressBar.setProgress(percent*0.01); 
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub
		
	}
}
