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

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.inpher.clientapi.InpherProgressListener;
import org.inpher.clientimpl.utils.AutoconfInpherClientUtils;
import org.inpher.clientimpl.utils.InpherConfigProperties;
import org.inpher.clientimpl.utils.ServiceTestResult;
import org.inpher.clientimpl.utils.ServiceTestStatus;
import org.rauschig.jarchivelib.ArchiveFormat;
import org.rauschig.jarchivelib.Archiver;
import org.rauschig.jarchivelib.ArchiverFactory;
import org.rauschig.jarchivelib.CompressionType;

import application.KillableThread;
import utils.DownloadProgressResult;
//import utils.Utils;
import utils.Utils;

/**
 * @author jetchev
 *
 */
public class LocalSolrInstaller {
	public static final String patchURL = "http://www.inpher.io/files/inpher-frequency.tgz"; 
	public static final String patchSHA256 = 
			"fed01424df362b75686cd98628008c10021fa8e2ac219fe2d740a4cdc41d7f52";
	public static final String solrDownloadURL = 
			"http://mirror.switch.ch/mirror/apache/dist/lucene/solr/5.3.1/solr-5.3.1.tgz";
	public static final String solrDownloadSHA256 = 
			"34ddcac071226acd6974a392af7671f687990aa1f9eb4b181d533ca6dca6f42d";
	public static final String relPatchDest = "solr-5.3.1/server/solr/"; 
	public static final String relPatchSolrCmdStr = "solr-5.3.1/bin/"; 
	public static final String solrInstallBaseUrl="http://localhost:8983/solr/inpher-frequency"; 
	/**
	 * 
	 */

	public LocalSolrInstaller() {
	}

	public void parseGlobalSolrForMirror() {
	}

	public static ServiceTestResult downloadAndUnzipSolrAndPatches(InpherConfigProperties icp, InpherProgressListener progress) {
		final double beginSolrDownloadPercent = 0;
		final double endSolrDownloadPercent = 0.7;
		final double endPatchDownloadPercent = 0.8;
		final double endUnzipPercent = 0.85;
		final double endPatchPercent = 0.86;
		final double endBootPercent = 0.95;
		final double endTestPercent = 1;
		final double coefSolrDownloadPercent = (endSolrDownloadPercent - beginSolrDownloadPercent);
		final double coefPatchDownloadPercent = (endPatchDownloadPercent - endSolrDownloadPercent);
		final double coefTestPercent = (endTestPercent - endBootPercent);
		final String messageSolrDownload="Downloading solr from "+solrDownloadURL;
		final String messagePatchDownload="Downloading patch from "+patchURL;
		Archiver archiver = ArchiverFactory.createArchiver(ArchiveFormat.TAR, CompressionType.GZIP);

		if (Utils.isLocalPortInUse(8983)) {
			return ServiceTestResult.createFailure(
					ServiceTestStatus.UNKNOWN_ERROR, 
					"You already have a server listening on port 8983.\n"
					+"Please re-use it or kill it first.\n"
					+"Cowardly refusing to install a new Solr server");			
		}
		String solrDestFolderStr = icp.getLocalSolrRootFolder();
		if (Utils.isNullOrEmpty(solrDestFolderStr)) {
			return ServiceTestResult.createFailure(
					ServiceTestStatus.INCOMPLETE_CONFIGURATION, 
					"The solr destination root folder has not been specified");
		}
		Path solrDestFolder = Paths.get(solrDestFolderStr);

		// download the latest solr distribution 
		progress.onProgress(0,messageSolrDownload);
		Path solrZipFile = null;
		try {
			solrZipFile = Utils.getAppTmpDir().resolve("solr.tgz");
			URL solrPath = new URL(solrDownloadURL);
			if (!Files.exists(solrZipFile) || !Utils.verifyChecksum(solrZipFile.toString(), solrDownloadSHA256))
				Utils.downloadWithProgressListener(solrPath, solrZipFile, x -> {
					progress.onProgress(beginSolrDownloadPercent+x*coefSolrDownloadPercent, messageSolrDownload);
					if (Thread.currentThread()==KillableThread.runningThread && KillableThread.runningThread.killRequest) 
						return DownloadProgressResult.ABORT; 
					return DownloadProgressResult.CONTINUE;
				});
		} catch (IOException e) {
			return ServiceTestResult.createFailure(
					ServiceTestStatus.UNKNOWN_ERROR, 
					"Error Downloading Solr",e);
		}
		// download the patches 
		if (Thread.currentThread()==KillableThread.runningThread && KillableThread.runningThread.killRequest) 
			return ServiceTestResult.createFailure(ServiceTestStatus.UNKNOWN_ERROR,"aborted");
		progress.onProgress(endSolrDownloadPercent,messagePatchDownload);
		Path patchFile = Utils.getAppTmpDir().resolve("patches.tgz"); 
		try {
			URL patchesPath = new URL(patchURL);
			if (!Files.exists(patchFile) || !Utils.verifyChecksum(solrZipFile.toString(), patchSHA256))
				Utils.downloadWithProgressListener(patchesPath, patchFile, x -> {
					progress.onProgress(endSolrDownloadPercent+x*coefPatchDownloadPercent, messagePatchDownload);
					if (Thread.currentThread()==KillableThread.runningThread && KillableThread.runningThread.killRequest) 
						return DownloadProgressResult.ABORT; 
					return DownloadProgressResult.CONTINUE;				
				});
		} catch (IOException e) {
			return ServiceTestResult.createFailure(
					ServiceTestStatus.UNKNOWN_ERROR, 
					"Error Downloading Inpher Core",e);
		}
		if (Thread.currentThread()==KillableThread.runningThread && KillableThread.runningThread.killRequest) 
			return ServiceTestResult.createFailure(ServiceTestStatus.UNKNOWN_ERROR,"aborted");
		progress.onProgress(endPatchDownloadPercent, "Unzipping Solr to "+solrDestFolder);
		try {
			Files.createDirectories(solrDestFolder);
			archiver.extract(solrZipFile.toFile(), solrDestFolder.toFile());  
		} catch (IOException e) {
			return ServiceTestResult.createFailure(
					ServiceTestStatus.UNKNOWN_ERROR, 
					"Error Unzipping Solr to "+solrDestFolder,e);
		}
		if (Thread.currentThread()==KillableThread.runningThread && KillableThread.runningThread.killRequest) 
			return ServiceTestResult.createFailure(ServiceTestStatus.UNKNOWN_ERROR,"aborted");
		progress.onProgress(endUnzipPercent,"Adding inpher core to "+solrDestFolder);
		try {
			archiver.extract(patchFile.toFile(), solrDestFolder.resolve(relPatchDest).toFile()); 
			Files.copy(solrDestFolder.resolve(relPatchDest).resolve("inpher-frequency/bin/solr.cmd"), 
					solrDestFolder.resolve(relPatchSolrCmdStr));
		} catch (IOException e) {
			return ServiceTestResult.createFailure(
					ServiceTestStatus.UNKNOWN_ERROR, 
					"Error Adding Inpher Core to "+solrDestFolder,e);
		}
		if (Thread.currentThread()==KillableThread.runningThread && KillableThread.runningThread.killRequest) 
			return ServiceTestResult.createFailure(ServiceTestStatus.UNKNOWN_ERROR,"aborted");
		progress.onProgress(endPatchPercent,"Starting Solr Server!");
		try { 
			AutoconfInpherClientUtils.startLocalSolr(solrDestFolder, "start");
		} catch (Exception e) {
			return ServiceTestResult.createFailure(
					ServiceTestStatus.UNKNOWN_ERROR, 
					"Error starting Solr",e);			
		}
		if (Thread.currentThread()==KillableThread.runningThread && KillableThread.runningThread.killRequest) 
			return ServiceTestResult.createFailure(ServiceTestStatus.UNKNOWN_ERROR,"aborted");
		progress.onProgress(endBootPercent,"Testing the new Solr Installation!");		
		try { 
			ServiceTestResult finalResult = AutoconfInpherClientUtils.testSolr(icp, (x,message)->{
				progress.onProgress(endBootPercent+x*coefTestPercent, message);
			});
			return finalResult;
		} catch (Exception e) {
			return ServiceTestResult.createFailure(
					ServiceTestStatus.UNKNOWN_ERROR, 
					"The new solr installation does not work",e);
		}
	}
}