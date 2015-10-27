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

import org.rauschig.jarchivelib.ArchiveFormat;
import org.rauschig.jarchivelib.Archiver;
import org.rauschig.jarchivelib.ArchiverFactory;
import org.rauschig.jarchivelib.CompressionType;
import application.Main;
import utils.DownloadProgressResult;
import utils.ServiceTestProgressListener;
import utils.ServiceTestResult;
import utils.ServiceTestStatus;
import utils.Utils;

/**
 * @author jetchev
 *
 */
public class LocalSolrInstaller {
	public static final String patchURL = "http://www.inpher.io/files/inpher-frequency.tgz"; 
	public static final String solrDownloadURL = 
			"http://mirror.switch.ch/mirror/apache/dist/lucene/solr/5.3.1/solr-5.3.1.tgz";  
	public static final String relPatchDest = "solr-5.3.1/server/solr/";  
	public static final String solrInstallBaseUrl="http://localhost:8983/solr/inpher-frequency"; 
	/**
	 * 
	 */

	public LocalSolrInstaller() {
	}

	public void parseGlobalSolrForMirror() {
	}

	public static ServiceTestResult downloadAndUnzipSolrAndPatches(InpherConfigProperties icp, ServiceTestProgressListener progress) {
		final double beginSolrDownloadPercent = 0;
		final double endSolrDownloadPercent = 70;
		final double endPatchDownloadPercent = 80;
		final double endUnzipPercent = 85;
		final double endPatchPercent = 86;
		final double endBootPercent = 100;
		final double coefSolrDownloadPercent = (endSolrDownloadPercent - beginSolrDownloadPercent)/100.;
		final double coefPatchDownloadPercent = (endPatchDownloadPercent - endSolrDownloadPercent)/100.;
		//final double coefBootPercent = (endBootPercent - endPatchPercent)/100.;
		final String messageSolrDownload="Downloading solr from "+solrDownloadURL;
		final String messagePatchDownload="Downloading patch from "+patchURL;
		Archiver archiver = ArchiverFactory.createArchiver(ArchiveFormat.TAR, CompressionType.GZIP);

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
			if (!Files.exists(solrZipFile))
				Utils.downloadWithProgressListener(solrPath, solrZipFile, x -> {
					progress.onProgress(beginSolrDownloadPercent+x*coefSolrDownloadPercent, messageSolrDownload);
					return DownloadProgressResult.CONTINUE;
				});
		} catch (IOException e) {
			return ServiceTestResult.createFailure(
					ServiceTestStatus.UNKNOWN_ERROR, 
					"Error Downloading Solr",e);
		}
		// download the patches 
		progress.onProgress(endSolrDownloadPercent,messagePatchDownload);
		Path patchFile = Utils.getAppTmpDir().resolve("patches.tgz"); 
		try {
			URL patchesPath = new URL(patchURL);
			if (!Files.exists(patchFile))
				Utils.downloadWithProgressListener(patchesPath, patchFile, x -> {
					progress.onProgress(endSolrDownloadPercent+x*coefPatchDownloadPercent, messagePatchDownload);
					return DownloadProgressResult.CONTINUE;				
				});
		} catch (IOException e) {
			return ServiceTestResult.createFailure(
					ServiceTestStatus.UNKNOWN_ERROR, 
					"Error Downloading Inpher Core",e);
		}
		progress.onProgress(endPatchDownloadPercent, "Unzipping Solr to "+solrDestFolder);
		try {
			Files.createDirectories(solrDestFolder);
			archiver.extract(solrZipFile.toFile(), solrDestFolder.toFile());  
		} catch (IOException e) {
			return ServiceTestResult.createFailure(
					ServiceTestStatus.UNKNOWN_ERROR, 
					"Error Unzipping Solr to "+solrDestFolder,e);
		}
		progress.onProgress(endUnzipPercent,"Adding inpher core to "+solrDestFolder);
		try {
			archiver.extract(patchFile.toFile(), solrDestFolder.resolve(relPatchDest).toFile());  
		} catch (IOException e) {
			return ServiceTestResult.createFailure(
					ServiceTestStatus.UNKNOWN_ERROR, 
					"Error Adding Inpher Core to "+solrDestFolder,e);
		}
		progress.onProgress(endPatchPercent,"Starting Solr Server!");
		try { 
			Utils.startLocalSolr(solrDestFolder, "start");
		} catch (Exception e) {
			return ServiceTestResult.createFailure(
					ServiceTestStatus.UNKNOWN_ERROR, 
					"Error starting Solr",e);			
		}
		progress.onProgress(endBootPercent,"Solr installation finished!");		
		return ServiceTestResult.createSuccess();
	}
}