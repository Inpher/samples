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
package application.transfer;

import java.util.ArrayList;
import java.util.List;

import org.inpher.clientapi.FrontendPath;
import org.inpher.clientapi.RankedSearchResult;

/**
 *
 */
public class RankedSearchResultsToJs extends JsonTransferEntity {
	public static class MyRankedSearchResult {
		private double score;
		private String docId;
		private String docName;
		private String contentType;
		
		public MyRankedSearchResult(RankedSearchResult r) {
			this.score=r.getScore();
			this.docId=r.getDocId();
			FrontendPath p = FrontendPath.parse(r.getDocId());
			this.docName=p.getLastElementName();
			this.contentType="text/plain";
			if (docName.endsWith(".pdf")) contentType="application/pdf";
			if (docName.endsWith(".PDF")) contentType="application/pdf";
		}
		public MyRankedSearchResult() {}
		public double getScore() {
			return score;
		}
		public void setScore(double score) {
			this.score = score;
		}
		public String getDocId() {
			return docId;
		}
		public void setDocId(String docId) {
			this.docId = docId;
		}		
		public String getDocName() {
			return docName;
		}
		public void setDocName(String docName) {
			this.docName = docName;
		}
		public String getContentType() {
			return contentType;
		}
		public void setContentType(String contentType) {
			this.contentType = contentType;
		}		
	}
	
	private boolean status;
	private List<MyRankedSearchResult> results;
	private String errorMessage;
	
	public static RankedSearchResultsToJs createSuccess(List<RankedSearchResult> results) {
		List<MyRankedSearchResult> xresults = new ArrayList<>();
		for (RankedSearchResult r: results)
			xresults.add(new MyRankedSearchResult(r));
		return new RankedSearchResultsToJs(true,xresults,null);		
	}
	public static RankedSearchResultsToJs createError(String errorMessage) {
		return new RankedSearchResultsToJs(false,null,errorMessage);
	}
	
	/**
	 * 
	 */
	public RankedSearchResultsToJs(boolean status, List<MyRankedSearchResult> results, String errorMessage) {
		this.status=status;
		this.results=results;
		this.errorMessage=errorMessage;
	}
	public RankedSearchResultsToJs() {
	}
	public boolean isStatus() {
		return status;
	}
	public void setStatus(boolean status) {
		this.status = status;
	}
	public List<MyRankedSearchResult> getResults() {
		return results;
	}
	public void setResults(List<MyRankedSearchResult> results) {
		this.results = results;
	}
	public String getErrorMessage() {
		return errorMessage;
	}
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	
	

}