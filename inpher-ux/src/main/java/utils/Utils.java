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

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

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

	public static boolean isNullOrEmpty(String s) {
		return s==null || s.isEmpty();
	}
	

	
}
