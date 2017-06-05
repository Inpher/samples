import org.inpher.clientapi.*;
import org.inpher.clientapi.efs.BackendIterator;
import org.inpher.clientapi.efs.Element;
import org.inpher.clientapi.efs.SearchableFileSystem;
import org.inpher.clientapi.efs.exceptions.ParentNotFoundException;
import org.inpher.clientapi.efs.exceptions.PathNotFoundException;
import org.inpher.clientapi.exceptions.InpherException;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class Samples {
    private static String username = "Deadpool";
    private static String password = "07*OYNh09D4?3sQ";

    private static FrontendPath root = FrontendPath.root(username);
    private static FrontendPath dir1 = root.append("dir1");
    private static FrontendPath dir2 = root.append("dir2");
    private static FrontendPath file = dir1.append("my-file");
    private static FrontendPath file2 = dir1.append("deleteMe");

    private static String username2 = "Vanessa";
    private static String password2 = "3~2#aqD*<}6x6Qk";

    private static String groupName = "Exclusive-Sharing-Group";
    private static String shareName = "sharedFile";

    private static FrontendPath root2 = FrontendPath.root(username2);
    private static FrontendPath sharingGroupRoot = FrontendPath.root(groupName);
    private static FrontendPath sharedFile = sharingGroupRoot.append(shareName);

    private static String hl = "\n=============================================================\n";

    public static void main(String[] args) {
        try {
            System.out.println("INPHER _ULTRA SAMPLE");

            System.out.println(hl);
            // create the object

            System.out.println("Creating the InpherClient object");
            InpherClient client = InpherClient.getClient();

            System.out.println("Object InpherClient is ready.");
            System.out.println();

            InpherUser user = new InpherUser(username, password);
            System.out.printf("Register user: %s%n", username);
            // register a new user in the environment
            client.registerUser(user);
            System.out.printf("Login user: %s%n", username);
            // get the object that handles system operations by login
            SearchableFileSystem searchableFileSystem = client.loginUser(user);

            System.out.println(hl);

            System.out.printf("Make directories %s and %s started%n", dir1.getLastElementName(),
                    dir2.getLastElementName());
            // create sample directories
            searchableFileSystem.mkdir(dir1);
            searchableFileSystem.mkdir(dir2);
            System.out.println("Make directories completed");
            System.out.printf("List the root directory: %s%n", root.getLastElementName());
            //list the directories
            BackendIterator<Element> list = searchableFileSystem.list(root);
            // and print them
            while (list.hasNext()) {
                System.out.println(list.next().getFrontendPath());
            }

            System.out.println(hl);

            System.out.printf("Upload 2 files with paths:%n%s%n%s%n", file, file2);
            // create a doc in one of the directories
            File tmpFile = randomFile("tmpFile");
            searchableFileSystem.upload(tmpFile, file);
            searchableFileSystem.upload(tmpFile, file2);
            System.out.println("Upload completed");

            System.out.println("Check that the files exists");
            if (searchableFileSystem.exists(file)) {
                System.out.printf("%s exists%n", file);
            }
            if (searchableFileSystem.exists(file2)) {
                System.out.printf("%s exists%n", file2);
            }
            System.out.printf("Delete file %s%n", file2);
            searchableFileSystem.delete(file2);

            System.out.printf("Check that %s does not exist%n", file2);
            if (!searchableFileSystem.exists(file2)) {
                System.out.printf("%s does not exist anymore%n", file2);
            }

            System.out.println(hl);

            System.out.println("Search for a keyword: \"test\"");
            List<String> keywords = new ArrayList<String>();
            keywords.add("test");
            SearchResponse searchResponse = searchableFileSystem.search(keywords);
            System.out.println("Search operation completed");
            System.out.printf("Total hits: %d%n", searchResponse.getTotalHits());
            for (RankedSearchResult el : searchResponse.getAllRankedSearchResults()) {
                System.out.printf("File found: %s  with a rank score: %f%n", el.getPath(),
                        el.getScore());
            }

            System.out.println(hl);

            System.out.printf("Register a new user: %s  to include in our sharing group%n",
                    username2);
            InpherUser user2 = new InpherUser(username2, password2);
            client.registerUser(user2);
            System.out.println("Register completed");
            System.out.printf("Login the second user: %s%n", username2);
            SearchableFileSystem searchableFileSystem2 = client.loginUser(user2);
            System.out.println("Login completed");
            List<String> myFriends = new ArrayList<String>();
            myFriends.add(username);
            myFriends.add(username2);

            System.out.println(hl);

            System.out.printf("Create a sharing group: %s%n", groupName);
            client.createSharingGroup(searchableFileSystem, groupName, myFriends);
            Thread.sleep(1000);
            System.out.println("Sharing group created");

            System.out.println(hl);
            System.out.printf("Share: %s with sharing group %s under the name %s%n", dir1.toURI(),
                    groupName, sharedFile);

            searchableFileSystem.shareElement(groupName, dir1, shareName);
            System.out.println("Check that the shared file exists in the sharing group.");
            if (searchableFileSystem.exists(sharedFile)) {
                System.out.println(sharedFile.toURI());
            }

            System.out.println(hl);

            System.out
                    .printf("Second user %s lists the sharing group root folder: %s as the second user: %s%n",
                            username2, sharingGroupRoot.toURI(), username2);
            BackendIterator<Element> listShare = searchableFileSystem2.list(sharingGroupRoot);
            while (listShare.hasNext()) {
                System.out.println(listShare.next().getFrontendPath());
            }

            System.out.println(hl);

            System.out.printf("Second user %s searches for \"test\" keyword%n", user2);
            SearchResponse searchResponse2 = searchableFileSystem2.search(keywords);
            System.out.println("Search operation completed");
            System.out.printf("Total hits: %d%n", searchResponse2.getTotalHits());
            for (RankedSearchResult el : searchResponse2.getAllRankedSearchResults()) {
                System.out.printf("File found: %s  with a rank score: %f%n", el.getPath(),
                        el.getScore());
            }

            System.out.println(hl);
            System.exit(0);
        } catch (InpherException e) {
            e.printStackTrace();
        } catch (PathNotFoundException e) {
            e.printStackTrace();
        } catch (ParentNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static File randomFile(String name) throws Exception {
        File file = File.createTempFile(name, ".tmp");
        FileWriter fileWriter = new FileWriter(file);
        fileWriter.write("This is a test file");
        fileWriter.flush();
        fileWriter.close();
        return file;
    }
}
