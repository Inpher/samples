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

package application.transfer;

public class UploadProgressEntity extends JsonTransferEntity {
	private String source;
	private String destination;
	private String status;

	public static UploadProgressEntity create(String source, String destination, String status) {
		return new UploadProgressEntity(source, destination, status);
	}
	
	public UploadProgressEntity() {
	}

	public UploadProgressEntity(String source, String destination, String status) {
		super();
		this.source = source;
		this.destination = destination;
		this.status = status;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
}
