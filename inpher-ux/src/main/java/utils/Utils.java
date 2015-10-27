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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.inpher.clientimpl.utils.AutoconfInpherClientUtils;
import org.slf4j.Logger;


public class Utils {
	public static Logger getLogger() {return AutoconfInpherClientUtils.getLogger();}
	public static Path getAppTmpDir() {return AutoconfInpherClientUtils.getAppTmpDir();}

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
				if (listener.onProgress(totalRead/totalLengthD)
						==DownloadProgressResult.ABORT) break;
				nextListenerRead+=chunks;
			}
		}
		listener.onProgress(totalRead/totalLengthD);
		wbc.close();
		rbc.close();
	}
	public static void downloadWithProgressListener(URL source, Path destination) throws IOException {
		downloadWithProgressListener(source, destination,
				(x)->{
					System.out.format("[ Downloading: %5.1f %% ]\r",100*x);
					return DownloadProgressResult.CONTINUE;
				});
	}

	public static boolean isNullOrEmpty(String s) {
		return s==null || s.isEmpty();
	}
	
	public static boolean isLocalPortInUse(int port) {
		  try {
		    (new Socket("127.0.0.1", port)).close();
		    return true;
		  }
		  catch(IOException e) {
			  return false;
		  }
	}
	
	/**
     * Verifies file's SHA256 checksum
     * @param Filepath and name of a file that is to be verified
     * @param testChecksum the expected checksum
     * @return true if the expeceted SHA256 checksum matches the file's SHA256 checksum; false otherwise.
     * @throws NoSuchAlgorithmException
     * @throws IOException
     */
    public static boolean verifyChecksum(String file, String testChecksum) {
    	try {
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        FileInputStream fis = new FileInputStream(file);
  
        byte[] data = new byte[1024];
        int read = 0; 
        while ((read = fis.read(data)) != -1) {
            sha256.update(data, 0, read);
        };
        fis.close();
        byte[] hashBytes = sha256.digest();
  
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < hashBytes.length; i++) {
          sb.append(Integer.toString((hashBytes[i] & 0xff) + 0x100, 16).substring(1));
        }
         
        String fileHash = sb.toString();
         
        return fileHash.equals(testChecksum);
    	} catch (NoSuchAlgorithmException e) {
    		e.printStackTrace();
    		return false;
    	} catch (IOException e) {
    		return false;
    	}
    }

	
}
