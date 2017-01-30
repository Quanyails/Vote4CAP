package com.smogon.cap.voting;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


/**
 * A class that scrapes a XenForo thread and collects the posts in the thread, 
 * converting them into a {@code List} of {@link Ballot Ballots} for use by a voting algorithm.
 * <p>
 * TODO: Refactor, so this project isn't limited to XenForo pages.
 */
// Note to self:
// We want to be as efficient as possible with scraping pages. That means:
// 1. Each page should be queried only once,
// 2. We should only use enough memory for one HTML document, and
// 3. We should convert between URLs, HTML Elements, and Ballots elegantly.

// Thus in pseudocode:
// Make a list of HTML representing users' posts.
// 	For each page:
// 		Find every post on the page and add it to the list.
// 	End for loop.
// 	Remove the opening post.
// 	For each remaining post:
// 		Map the post to a Ballot.
// 	End for loop.
// Return the List of Ballots.
public abstract class XenForoScraper
{
	// Timeout for fetching a web page.
	private static final int TIMEOUT = 10_000; // in milliseconds

	/**
	 * Default constructor.
	 * A {@code XenForoScraper} has no internal state, 
	 * so it shouldn't really be instantiatable. 
	 * Everything should be static and class-based, 
	 * but it is impossible to override static methods, 
	 * which is required for anonymous subclassing.
	 * (Hence why {@link XenForoScraper#makeBallots(URL)} is instance-based.)
	 * <p>
	 * A {@code XenForoScraper} does not take a {@link URL} as a field,
	 * as it represents a function that takes in {@code URLs}, 
	 * not a {@code URL} that can have functions applied on it.
	 */
	protected XenForoScraper(){}

	// Ballot parsing

	/**
	 * Given a XenForo post as HTML, parses the post to create a {@link Ballot} from it.
	 * The ballot format for a XenForo post may change between forum threads, 
	 * hence why this method is declared abstract.
	 * @param post The post scraped, as an HTML element.
	 * @return The ballot generated from the post.
	 */
	public abstract Ballot parse(Element post);

	
	// The remaining functions are internal and should not be lightly changed.

	// Helper function: Get the HTML document from a link, "handling" IOExceptions.
	// If this method throws an exception, XenForo likely changed something.
	private static Document getDocument(URL link)
	{
		try {return Jsoup.parse(link, XenForoScraper.TIMEOUT);}
		catch (IOException e){throw new AssertionError(e);}
	}
	
	// Helper function: Searches for the next link, given the current link.
	// TODO: This could probably be more elegant, since we have a URL -> Document -> URL conversion.
	// by splitting the methods into hasNextLink and findNextLink.
	// But it's all internal, anyway...
	private static URL findNextLink(Document page)
	{
		try
		{
			// Use CSS query syntax to find the navigation bar to the next page.
			Elements nextElements = page
				.select(".PageNav") // Select navigation bar.
				.select("nav")
				.select(".text") // Select prev/next button on the navbar.
				.select(":contains(next)"); // If inner HTML has 'next', it's the 'next' button.
			if (nextElements.isEmpty()) return null; // If we find no 'next' button, we're at the last page.

			// We want the absolute URL, hence why we use the 'abs' attribute prefix.
			// https://jsoup.org/cookbook/extracting-data/working-with-urls
			return new URL(nextElements.first().attr("abs:href"));
		}
		catch (IOException e){throw new AssertionError(e);}
	}
	
	/**
	 * Given a link to a XenForo thread, 
	 * this method scrapes posts until it reaches the end of the thread
	 * and builds a {@link List} of {@link Ballot Ballots} from it, 
	 * calling on {@link XenForoScraper#parse(Element)} to process each post.
	 * @param link The link to scrape.
	 * @return A list of all {@code Ballots} scraped from the thread.
	 */
	public List<Ballot> makeBallots(URL link)
	{
		URL currentLink = link;
		List<Ballot> ballots = new ArrayList<>();
		while (currentLink != null) // Scrape pages until we reach the end of the thread.
		{
			Document page = XenForoScraper.getDocument(currentLink);

			// Find posts on page using CSS-style queries as HTML elements.
			Elements posts = page.select(".messageList > li");
			
			// Create ballots from the HTML elements.
			for (Element post: posts) ballots.add(this.parse(post));

			// Go to the next page.
			currentLink = XenForoScraper.findNextLink(page);
		}
		
		ballots.remove(0); // Skip the opening post.
		return ballots;
	}
}
