import java.io.*;
import java.net.*;
import java.net.http.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;
import java.util.regex.*;

/**
* Crawler class creates a crawler to crawl a website 
* and collect links on a visited webpage 
* upto a certain of deep levels of crawling
* @author Jen Hoang
*/
public class Crawler {
	public ArrayList<URI> urlList = new ArrayList<URI>();
	private ArrayList<String> localLinkList = new ArrayList<String>();
	private String host;
	
	/**
	* Given server address, get add the root address '/' to urlList and localList
	*	and return an object of class Crawler
	* @param host String of webiste address to crawl
	* @return a Crawler object
	*/
	Crawler(String host){
		this.host = host;
		addNewLink("/");
	}

	/**
	* Given the a string of a relative path, add the link to urlList and localLinkList
	* @param link String of a collected link (path) to add to localLinkList and urlList
	* @side-effects create and add a full path (URI) to urlList and add relative path to localLinkList
	*/
	private void addNewLink(String path){
		urlList.add(URI.create("http://" + host + path));
		localLinkList.add(path);
	}

	/**
	* Given a url of a webpage find all the links on this webpage that matches to the defined Regex
	* If the link was not visited, add the link to the localLinkList and urlList by calling addNewLink method
	* @param url String of valid url whose request is accepted
	* @note the code is forced to be synchronous when add the path to localLinkList and urlList
	*	to avoid multi-threads access these lists at the same time
	*/
	public void connect(String url) {
		
		// Specify the Link's patternn with Regex
		Pattern pattern = Pattern.compile("href=\"([^\"]*)\"", Pattern.CASE_INSENSITIVE);
		// Search for the match Link in the string url
		Matcher matcher = pattern.matcher(url);
		while(matcher.find()) {
			// Store the found link
			String value = String.valueOf(matcher.group(1));
			synchronized(this) {
				if(!localLinkList.contains(value)){
					addNewLink(value);
				}
			}
			
		}
	}

	/**
	* Print the current value of locaLinkList to the terminal
	* @param N/A
	*/
	public void printLocalLink(){
		System.out.println("localLinkList: " + localLinkList);

	}

	/** 
	* The main flow of Crawler program
	* 	Create a Crawler object
	*	Print the current LocalLinkList
	*	Create a list of request according to current urlList of crawler
	* 	Clear urlList to minimize visit the url again
	*	Send request to website, get the response and pass result to crawler.accept() method
	* 	Print the current value of localLinkList on the terminal
	* @note Exceptions are captured and printed on the terminal, the program will then be terminated
	*/
	public static void main(String[] args) {
		// deep level of crawling
		final int MAX_LEVELS = 2;
		final String HOST = "127.0.0.1";

		// Create a crawler
		Crawler crawler = new Crawler(HOST);
		crawler.printLocalLink();

		try{
			for (int currentLevel = 0; currentLevel < MAX_LEVELS; currentLevel++) {
				List<HttpRequest> requests = crawler.urlList
					.stream()
					.map(url -> HttpRequest.newBuilder(url))
					.map(reqBuilder -> reqBuilder.build())
					.collect(Collectors.toList());

				crawler.urlList.clear();
				
				HttpClient client = HttpClient.newHttpClient();
				CompletableFuture<?>[] asyncs = requests
					.stream()
					.map(request -> client
						.sendAsync(request, HttpResponse.BodyHandlers.ofString())
						.thenApply(HttpResponse::body)
						.thenAccept(crawler::connect))
					.toArray(CompletableFuture<?>[]::new);

				CompletableFuture.allOf(asyncs).join();

				crawler.printLocalLink();
			}
		} catch(Exception e){
			System.err.println(e);
            e.printStackTrace();
		}
	}
}