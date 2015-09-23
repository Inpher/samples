package org.inpher.clientdemo;

import java.util.ArrayList;
import java.util.List;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.DeleteObjectsRequest.KeyVersion;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;

public class DemoWipeEverything {
	@DemoArg
	private static String bucketName = ""; 
	@DemoArg
	private static String preserveRoot = "true"; 
	@DemoArg
	private static String region = "eu-central-1"; 
	

	public static void main(String [] args) throws Exception {
		if (bucketName.isEmpty()) System.exit(0);
		boolean presRoot = Boolean.valueOf(preserveRoot);
		
		AmazonS3Client s3client = new AmazonS3Client(new ProfileCredentialsProvider().getCredentials());
		if (region.equals("eu-central-1")) {
			Region euCentral1 = Region.getRegion(Regions.EU_CENTRAL_1);
			s3client.setRegion(euCentral1);
			s3client.setEndpoint("s3.eu-central-1.amazonaws.com");
		} else if (region.equals("us-west-1")) {
			Region usWest1 = Region.getRegion(Regions.US_WEST_1);
			s3client.setRegion(usWest1);
			s3client.setEndpoint("s3-us-west-1.amazonaws.com");
			
		} else {
			System.err.println("invalid region! (eu-central-1 or us-west-1)");
			System.exit(1);
		}
		ObjectListing lo = s3client.listObjects(bucketName);
		List<S3ObjectSummary> li = lo.getObjectSummaries();
		List<KeyVersion> keysToDelete = new ArrayList<>();
		for (S3ObjectSummary obj: li) {
			String key = obj.getKey();
			if (presRoot) {
				if (key.equals(bucketName) || key.equals(bucketName+"_config"))
					continue;
			}
			System.err.println(" [-] "+key);
			keysToDelete.add(new KeyVersion(key));
			if (keysToDelete.size()>=200) {
				DeleteObjectsRequest dlr = new DeleteObjectsRequest(bucketName);
				dlr.setKeys(keysToDelete);
				s3client.deleteObjects(dlr);
				keysToDelete.clear();
			}
		}
		if (keysToDelete.size()>0) {
			DeleteObjectsRequest dlr = new DeleteObjectsRequest(bucketName);
			dlr.setKeys(keysToDelete);
			s3client.deleteObjects(dlr);
			keysToDelete.clear();
		}
	}
}
