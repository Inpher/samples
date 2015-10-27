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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

public class JsonTransferEntity {
	public static final ObjectMapper mapper;
	public static final ObjectWriter writer;
	
	static {
		mapper = new ObjectMapper();
		writer = mapper.writer();
	}
	
	public String toJson() {
		
		try {
			return writer.writeValueAsString(this);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return "\"error\"";
		}
	}
	public static<T> T fromJson(Class<T> type, String s) {
		try {
			return mapper.readValue(s, type);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
