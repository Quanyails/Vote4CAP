package com.smogon.cap.voting;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

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
public abstract class ThreadScraper extends Scraper
{
	// Timeout for fetching a web page.
	private static final int TIMEOUT = 10_000; // in milliseconds

	// Helper function: Get the HTML document from a link, "handling" IOExceptions.
	private static Document getDocument(URL link)
	{
		try {return Jsoup.parse(link, ThreadScraper.TIMEOUT);}
		catch (IOException e){throw new AssertionError(e);}
	}

	// Ballot parsing

	/**
	 * Given a {@link Document} generated from its {@link URL},
	 * navigates its HTML DOM tree and selects the elements
	 * representing the posts to be parsed into ballots.
	 * <p>
	 * Normally, the subclass should implement this method
	 * by calling {@link Document#select(String)} and returning the selection.
	 * @param page The page to extract HTML elements from.
	 * @return A selection of {@link Element}
	 */
	protected abstract Elements getPosts(Document page);

	/**
	 * Given an HTML node returned from {@link #getPosts(Document)},
	 * parses it into memory as a {@link Ballot}.
	 * @param post One of the values returned from {@link #getPosts(Document)}.
	 * @return A {@link Ballot} for each post.
	 */
	protected abstract Ballot parse(Element post);

	/**
	 * Finds the next page to scrape to convert into {@link Ballot Ballots}.
	 * <p>
	 * TODO: Split method into hasNextLink() and findNextLink().
	 * @param page The web page to search for the next link in.
	 * @return The URL to the next page, or null if no next page exists.
	 */
	protected abstract URL findNextLink(Document page);

	/**
	 * This method scrapes posts until it reaches the end of the thread
	 * and builds a {@link List} of {@link Ballot Ballots} from it.
	 * <p>
	 * This method calls:
	 * <ul>
	 *     <li>{@link #getPosts(Document)} to split the page into HTML nodes</li>
	 *     <li>{@link #parse(Element)} to process each post</li>
	 *     <li>{@link #findNextLink(Document)} to find the next page</li>
	 * </ul>
	 */
	@Override
	public List<Ballot> makeBallots(URL link)
	{
		URL currentLink = link;
		List<Ballot> ballots = new ArrayList<>();
		while (currentLink != null) // Scrape pages until we reach the end of the thread.
		{
			Document page = ThreadScraper.getDocument(currentLink);

			// Find posts on page using CSS-style queries as HTML elements.
			Elements posts = this.getPosts(page);

			// Create ballots from the HTML elements.
			for (Element post: posts) ballots.add(this.parse(post));

			// Go to the next page.
			currentLink = this.findNextLink(page);
		}
		return ballots;
	}
}
