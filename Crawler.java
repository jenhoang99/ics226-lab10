import java.io.*;
import java.net.*;
import java.net.http.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;
import java.util.regex.*;


public class Crawler {
	public ArrayList<URI> urlList = new ArrayList<URI>();
	private ArrayList<String> localLinkList = new ArrayList<String>();
	private String host;
	
	Crawler(String host){
		this.host = host;
		addNewLink("/");
	}

	private void addNewLink(String link){
		//System.out.print(link);
		urlList.add(URI.create("http://" + host + link));
		if(!localLinkList.isEmpty()){
			localLinkList.add(localLinkList.size()-1,link);
		}
		else {
			localLinkList.add(link);
		}
	}

	public ArrayList<URI> getUrlList(){
		return urlList;
	}
	public void connect(String url) {
		synchronized(localLinkList) {
			// Specify the Link's patternn with Regex
			Pattern pattern = Pattern.compile("href=\"([^\"]*)\"", Pattern.CASE_INSENSITIVE);
			// Search for the match Link in the string url
			Matcher matcher = pattern.matcher(url);

			while(matcher.find()) {
				// Store the found link
				String value = String.valueOf(matcher.group(1));

				if(!localLinkList.contains(value)){
					addNewLink(value);
				}
			}
		}
	}

	public void printLocalLink(){
		System.out.println("localLinkList:" + localLinkList);
	}
	public static void main(String[] args) {
		// deep level of crawling
		final int MAX_LEVELS = 2;
		final String HOST = "127.0.0.1";

		Crawler crawler = new Crawler(HOST);
		crawler.printLocalLink();

		int currentLevel = 0;

		while (currentLevel < MAX_LEVELS) {
			List<HttpRequest> requests = crawler.getUrlList()
				.stream()
				.map(url -> HttpRequest.newBuilder(url))
				.map(reqBuilder -> reqBuilder.build())
				.collect(Collectors.toList());

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
			currentLevel++;
		}
	}
}