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

public class LoginResult extends JsonTransferEntity {
	private boolean status;
	private String username;
	private String errorDescription;

	public LoginResult() {
	}
	

	public LoginResult(boolean status, String username, String errorDescription) {
		super();
		this.status = status;
		this.username = username;
		this.errorDescription = errorDescription;
	}


	public boolean isStatus() {
		return status;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getErrorDescription() {
		return errorDescription;
	}

	public void setErrorDescription(String errorDescription) {
		this.errorDescription = errorDescription;
	}

	public static LoginResult createSuccess(String username) {
		return new LoginResult(true,username,null);
	}
	public static LoginResult createFailure(String errorMessage) {
		return new LoginResult(false,null,errorMessage);
	}
}
