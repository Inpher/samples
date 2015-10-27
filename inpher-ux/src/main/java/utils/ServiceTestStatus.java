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

public enum ServiceTestStatus {
	OK,
	UNKNOWN_ERROR,
	INCOMPLETE_CONFIGURATION,
	AMAZONS3_INVALID_CREDENTIALS,
	AMAZONS3_BUCKET_DOES_NOT_EXIST,
	AMAZONS3_BUCKET_IN_WRONG_REGION,
	AMAZONS3_BUCKET_INSUFFICIENT_PERMISSIONS, 
	LOCALSTORAGE_ROOT_DOES_NOT_EXIST, 
	INVALID_LOCALSTORAGE_ROOT,
}
