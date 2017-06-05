package org.inpher.secmailarchiver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.Vector;

import org.apache.commons.io.FileUtils;
import org.inpher.clientapi.DecryptedSearchResponse;
import org.inpher.clientapi.FrontendPath;
import org.inpher.clientapi.InpherClient;
import org.inpher.clientapi.InpherUser;
import org.inpher.clientapi.MakeDirectoryRequest;
import org.inpher.clientapi.RankedSearchResult;
import org.inpher.clientapi.ReadDocumentRequest;
import org.inpher.clientapi.UploadDocumentRequest;
import org.inpher.clientapi.exceptions.ExistingDirectoryException;
import org.inpher.clientapi.exceptions.ExistingUserException;
import org.inpher.clientapi.exceptions.InpherException;
import org.inpher.clientapi.exceptions.InvalidCredentialsException;
import org.inpher.clientapi.exceptions.NonRegisteredUserException;

import com.pff.PSTException;
import com.pff.PSTFile;
import com.pff.PSTFolder;
import com.pff.PSTMessage;

/**
 * @author alex@inpher.io
 * @version 0.0.1
 * 
 * secmailarchive version 0.0.1
 * inpher-api version 0.6 (www.inpher.io)
 * java-libpst version 0.8.1
 *
 * Description:
 * This application allow you to securely store an email archive (Microsoft .PST) in 
 * Amazon s3 without losing the ability to search for specific emails based on keywords.
 * A searchable encryption scheme is used to search the archive on s3 without
 * downloading the encrypted archive.
 *
 * Usage:
 *
 * java jar secmailarchive.jar <username> <password> <options...>
 *
 * <username>    Inpher Username
 * <password>    Inpher Password
 *
 * <options>
 *
 *    register                 Registers a new Inpher user using <username> <password>
 *    upload <file>            Encrypts indexes and uploads .pst <file>
 *    search <kw 1> <kw 2> ... Searches and downloads top 10 emails containing all
 *                             provided keywords
 **/

public class SecMailArchiver {
	static int counter = 0;
	private static InpherClient inpherClient = null;
	private static String username;
	private static String pass;
	
	public static void main(String[] args) {
		if (args.length < 3) usage();		
		else{
			username = args[0];
			pass = args[1];
					
			switch (args[2]) {
				case "search":
					login();
					search(args);
					break;
				case "register":
					register();
					break;
				case "upload": 
					login();
					upload(args[3]);
					break;
				default: 
					usage();
					break;
			}
		}	
	}

	/**
	 * login inpher user <username> <password>
	 */
	private static void login() {
		try {
			// creating the Inpher client  
			inpherClient = InpherClient.getClient();
			
			// login with the specified credentials
			inpherClient.loginUser(new InpherUser(username,pass));
		} catch (InvalidCredentialsException e) {
			System.out.println("Error: Invalid Credentials! Please try again.\n");
			usage();
			System.exit(1);
		} catch (NonRegisteredUserException e) {
			System.out.println("Error: User doesn't exist. Please register first.\n");
			usage();
			System.exit(1);
		} catch (InpherException e) {
			e.printStackTrace();
			System.exit(1);
		} 
	}

	/**
	 * print program usage
	 */
	private static void usage() {
		StringBuilder usage = new StringBuilder();

		usage.append("secmailarchive version 0.0.1\r\n");
		usage.append("inpher-api version 0.6 (www.inpher.io)\r\n");
		usage.append("java-libpst version 0.8.1\r\n");
		usage.append("\r\n");
		usage.append("Description:\r\n");
		usage.append("This application allow you to securely store an email archive (Microsoft .PST) in\r\n");
		usage.append("Amazon s3 without losing the ability to search for specific emails based on keywords.\r\n");
		usage.append("A searchable encryption scheme is used to search the archive on s3 without\r\n");
		usage.append("downloading the encrypted archive.\r\n");
		usage.append("\r\n");
		usage.append("Usage:\r\n");
		usage.append("\r\n");
		usage.append("java jar secmailarchive.jar <username> <password> <options...>\r\n");
		usage.append("\r\n");
		usage.append("<username>    Inpher Username\r\n");
		usage.append("<password>    Inpher Password\r\n");
		usage.append("\r\n");
		usage.append("<options>\r\n");
		usage.append("\r\n");
		usage.append("    register                 Registers a new Inpher user using <username> <password>\r\n");
		usage.append("    upload <file>            Encrypts indexes and uploads .pst <file>\r\n");
		usage.append("    search <kw 1> <kw 2> ... Searches and downloads top 10 emails containing all\r\n");
		usage.append("                             provided keywords to a new directory called \"output\".\r\n");
		System.out.println(usage);	
	}

	/**
	 * upload file
	 * @param filename : path to PST file 
	 */
	private static void upload(String filename) {
		try {
			// Open PST file
			PSTFile pstFile = new PSTFile(filename);
			
			// Call recursive function to process PST file
			System.out.println("Creating temporary files...");
			processFolder(pstFile.getRootFolder(),-1);
			
			// Now, we will use the java.nio FileVisitor interface to recursively upload 
			// the source file/folder. This interface allows you to walk across the local filesystem
			// tree and at each file/folder encountered, call a custom callback function.
			//
			// In this demo, all we need to do is the following:
			//  1. whenever we enter into a local directory, we create the same remote directory.
			//  2. whenever we encounter a local file, we upload it.
			FrontendPath destDir = FrontendPath.parse(username + ":/" + "mail"); 
			final Path localPath = Paths.get("tmp"); 
			final String destdirStr = destDir.getFrontendURI();
			
			Files.walkFileTree(localPath,new FileVisitor<Path>() {
				@Override
				public FileVisitResult preVisitDirectory(Path dir,
						BasicFileAttributes attrs) throws IOException {
					// Whenever we enter a local directory, we create the same remote directory
					// before treating the content of the folder
					// (we do nothing if the remote directory already exists)
					String relPath = localPath.relativize(dir).toString();
					FrontendPath newdir = FrontendPath.parse(destdirStr+"/"+relPath);
					System.err.println("[mkdir] "+newdir);
					try { inpherClient.makeDirectory(new MakeDirectoryRequest(newdir));
					} catch (ExistingDirectoryException e) {}
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult visitFile(Path file,
						BasicFileAttributes attrs) throws IOException {
					// Whenever we encounter a local file, we upload it
					String relPath = localPath.relativize(file).toString();
					FrontendPath newfile = FrontendPath.parse(destdirStr+"/"+relPath);
					File f = file.toFile();
					System.err.println("" + file + " -> "+newfile);
					inpherClient.uploadDocument(new UploadDocumentRequest(f,newfile));
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult visitFileFailed(Path file, IOException exc)
						throws IOException {
					// Treating errors on the local filesystem (corrupted filesystem, 
					// circular symlink refs, ...) is out of scope of this demo
					System.err.println("[IGNORING]" + file);
					throw exc;
				}

				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException exc)
						throws IOException {
					// nothing particular needs to be done when we leave a directory
					return FileVisitResult.CONTINUE;
				}
			});	
			
			FileUtils.deleteDirectory(new File("tmp"));
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * registering new inpher user <username> <password>
	 */
	private static void register() {
		InpherUser user = null;
		try {
			inpherClient = InpherClient.getClient(); 
			user = new InpherUser(username, pass);
		
			System.out.println("Registering user " + user.getUsername() + " ...");
		
			// calling the register method of the Inpher client 
			inpherClient.registerUser(user);
			System.out.println("User " + user.getUsername() + " successfully registered."); 
		} catch(ExistingUserException e) {
			System.out.println("User " + user.getUsername() + " already exists.");  
		} catch (InpherException e) {
			e.printStackTrace();
		}
	}

	/**
	 * search and download keywords provided in the parameters
	 * @param args
	 */
	private static void search(String[] args) {
		List<String> kwds = Arrays.asList(args).subList(3, args.length);;
		
		// call the search method 
		DecryptedSearchResponse res = inpherClient.search(kwds);
		
		// display the search results
		try {
			FileUtils.forceMkdir(new File("output"));
			int i = 1;
			Scanner scanner = new Scanner(System.in);
			for (RankedSearchResult e : res.getDocumentIds()) {
				System.out.format("%6.4f\t%s\n", e.getScore(), e.getDocId());
					File destFile = new File("output/" + i + ".txt");
					inpherClient.readDocument(new ReadDocumentRequest(e.getDocId(), destFile));
					
					System.out.println("\r\n\r\n\r\n");
					System.out.println("###############################################");
					System.out.println("Showing result: " +i + " Score: " + e.getScore() );
					System.out.println("###############################################");
					// print file
					try (BufferedReader br = new BufferedReader(new FileReader("output/" + i++ + ".txt"))) {
						   String line = null;
						   while ((line = br.readLine()) != null) {
						       System.out.println(line);
						   }
					}
				    
					System.out.println("\r\n\r\n\r\n");
					System.out.println("###############################################");
					System.out.println("Showing result: " +i + " Score: " + e.getScore() );
					System.out.println("Scroll up to read the full message and headers");
					System.out.println("###############################################");
					System.out.println("Press \"n\" followed by <ENTER> to read the next messages");
					System.out.println("Press \"q\" followed by <ENTER> to exit");
					System.out.println("###############################################");
					String next = scanner.next();
					
					// Exit if q key is pressed
					if (next.compareToIgnoreCase("q") == 0 ) break;
			} 
			// Cleanup scanner
			scanner.close();
			
			// Cleanup temporary files
			FileUtils.deleteDirectory(new File("output"));
		}
		catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	/**
	 * traverses the pst archive for mails
	 * taken from the java-libpst project
	 * @param folder : path to pst file 
	 * @param depth : depth indication for recursive function
	 * @throws PSTException
	 * @throws java.io.IOException
	 */
	private static void processFolder(PSTFolder folder, int depth) throws PSTException,
			java.io.IOException {
		depth++;

		// go through the folders...
		if (folder.hasSubfolders()) {
			Vector<PSTFolder> childFolders = folder.getSubFolders();
			for (PSTFolder childFolder : childFolders) {
				processFolder(childFolder,depth);
			}
		}

		// and now the emails for this folder
		if (folder.getContentCount() > 0) {
			depth++;
			PSTMessage email = (PSTMessage) folder.getNextChild();
			while (email != null) {
				String filename = "tmp/" + counter++;
				File f = new File(filename);
				FileUtils.writeStringToFile(f, email.getTransportMessageHeaders() + email.getBody());
				email = (PSTMessage) folder.getNextChild();
			}
			depth--;
		}
		depth--;
	}
}
