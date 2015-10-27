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

public class RegularStatus extends JsonTransferEntity {
	private boolean status;
	private String message;
	
	public static RegularStatus createNormal(String message) {
		return new RegularStatus(true,message);
	}
	public static RegularStatus createNormal() {
		return new RegularStatus(true,"done!");
	}
	public static RegularStatus createError(String message) {
		return new RegularStatus(false,message);
	}
	
	public RegularStatus() {
	}
	public RegularStatus(boolean status, String message) {
		super();
		this.status = status;
		this.message = message;
	}
	public boolean isStatus() {
		return status;
	}
	public void setStatus(boolean status) {
		this.status = status;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}

}
