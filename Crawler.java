import java.io.*;
import java.net.*;
import java.net.http.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;
import java.util.regex.*;


public class Crawler {
	public static ArrayList<String> localLinkList = new ArrayList<String>();
	public static ArrayList<URI> urlList = new ArrayList<URI>();
	
	private static int MAX_LEVELS = 2;
	

	public static void connect(String url) {
		synchronized(localLinkList) {
			// Specify the Link's pattern
			Pattern pattern = Pattern.compile("href=\"([^\"]*)\"", Pattern.CASE_INSENSITIVE);
			// Search for the match Link in the string url
			Matcher matcher = pattern.matcher(url);

			while(matcher.find()) {
				// Store the found link
				String value = String.valueOf(matcher.group(1));
				// System.out.println("Found value: " + value);
				if (localLinkList.size() == 0) {
					// Add the link the visitedLink dictionary
					//visitedLink.put(key, value);
					//key++;
					System.out.println("value: " + value);

					// Add the link to localLinkList array
					localLinkList.add(value);
					
				}
				else {
					// Create a counter
					//int count = 0;
					boolean isVisited = false;

					// For each key in visietedLink dictionary
					for (String link : localLinkList) {
						
						if(link.equals(value)) {
							isVisited = true;
							break;
						}
						
					}
					
					if(!isVisited) {
						localLinkList.add(value);
						System.out.println("value: " + value);
						
					}
				}
			}
		}
	}
	public static void main(String[] args) {
		ArrayList<URI> urlList = new ArrayList<URI>(); 

		urlList.add(URI.create("http://" + args[0]));

		System.out.println(urlList);

		int currentLevel = 0;

		while (currentLevel < MAX_LEVELS) {
			List<HttpRequest> requests = urlList
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
					.thenAccept(Crawler::connect))
				.toArray(CompletableFuture<?>[]::new);

			CompletableFuture.allOf(asyncs).join();

			urlList.clear();

			

			for (int i = 0; j<localLinkList.size(); i++) {
				// System.out.println("http://" + args[0] + localLinkList.get(j));
				urlList.add(URI.create("http://" + args[0] + localLinkList.get(i)));
			}

			// Print the total URL
			System.out.println("localLinkList:" + urlList);
			// System.out.println("visitedURL:" + localLinkList);
			currentLevel++;
		}
	}
}