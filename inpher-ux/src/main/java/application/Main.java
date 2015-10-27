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

package application;
	
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Security;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
import javafx.scene.Scene;
import javafx.scene.input.Dragboard;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import netscape.javascript.JSObject;
import utils.Utils;


public class Main extends Application {
	static {
		Security.addProvider(new BouncyCastleProvider());
	}
	
	private JavaBridge javaBridge = new JavaBridge();
	@Override
	public void start(Stage primaryStage) {
		Path appTmpDir = Utils.getAppTmpDir();
		javaBridge.initializeInpherClient();
		try {
			WebView webview = new WebView();
			webview.setContextMenuEnabled(false);
			WebEngine webEngine = webview.getEngine();
			String url=null;
			if (appTmpDir!=null && Files.isReadable(appTmpDir.resolve("html/a.html")))
				url = appTmpDir.resolve("html/a.html").toUri().toString();
			else
				url=Paths.get("src/html/a.html").toUri().toString();
			System.out.println("Loading: "+url);
			webEngine.load(url);			
			Scene scene = new Scene(webview,1200,720);
			//scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.show();
	        // Dropping over surface
	        webview.setOnDragEntered((event) -> {
	        	Dragboard db = event.getDragboard();
	        	if (db.hasFiles()) {
	        		List<File> fileList = db.getFiles();
	        		javaBridge.setLastFilesDrop(fileList);
	        		for (File file:fileList) {
	        			String filePath = file.getAbsolutePath();
	        			System.out.println(filePath);
	        		}
	        	}
	        });
			webEngine.getLoadWorker().stateProperty().addListener(
					new ChangeListener<State>() {
						@Override
						public void changed(ObservableValue<? extends State> observable, State oldValue,
								State newValue) {
							System.out.println("State changed: "+newValue);
							JSObject window = (JSObject) webEngine.executeScript("window");
							window.setMember("java", javaBridge);
							javaBridge.setWindow(window);
							webEngine.executeScript("java_init();");
						}
					}
			);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws Exception {
		if (Main.class.getResource("/html.zip")!=null) {
			//unzip the site to a temp folder
			ZipInputStream websiteZip = new ZipInputStream(Main.class.getResourceAsStream("/html.zip")); 
			for (ZipEntry ze = websiteZip.getNextEntry(); ze!=null; ze=websiteZip.getNextEntry()) {
				if (ze.isDirectory()) {websiteZip.closeEntry(); continue;}
				Path dest = Utils.getAppTmpDir().resolve(ze.getName());
				Files.createDirectories(dest.getParent());
				Files.copy(websiteZip, dest);
				websiteZip.closeEntry();
			}
			websiteZip.close();
		} else {
			System.err.println("WARNING: running in dev environment");
		}
		launch(args);
	}
}
