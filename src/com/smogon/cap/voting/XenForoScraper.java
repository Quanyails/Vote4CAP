package com.smogon.cap.voting;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A class that scrapes a XenForo thread and collects the posts in the thread,
 * converting them into a {@code List} of {@link Ballot Ballots} for use by a voting algorithm.
 */
public class XenForoScraper extends ThreadScraper
{
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Elements getPosts(Document page) {
		return page.select(".block-body .message");
	}


	// Static constants
	private final String TAGNAME = "br";
	private final String TAG = "<" + this.TAGNAME + ">";
	private final Whitelist WHITELIST = Whitelist.none().addTags(this.TAGNAME); // For Jsoup

	/**
	 * Given an HTML element representing a user's post,
	 * this parser uses the {@code <br>} tags in the post
	 * as a means of determining the user's ballot.
	 * This method assumes that the ballots in a post are at the top,
	 * with each option separated by a {@code <br>} tag.
	 * Once the post encounters two consecutive {@code <br>} tags,
	 * the post is assumed to have reached comments
	 * and so the rest of the text is ignored.
	 */
	private Function<Element, Ballot> parser = post ->
	{
		// Find the user who made the post.
		User voter = new User(post.attr("data-author"));

		// Get the post message in HTML form.
		String html = post.select(".bbWrapper").outerHtml();
		// Remove all HTML except whitelisted tags.
		String text = Jsoup.clean(html, WHITELIST).trim();

		// Truncate the HTML to only the part before the comment.
		// http://stackoverflow.com/a/4972126
		// Regex: tag followed by any number of whitespace characters followed by another tag
		String divider = this.TAG + "[ \\t\\n]*" + TAG;
		String votesPart = text.split(divider, 2)[0];

		// Create a vote for every line in the truncated section.
		List<User> votes = Arrays.stream(votesPart.split(TAG))
			.map(String::trim) // trim whitespace
			.map(User::new)
			.collect(Collectors.toList());

		// Create the ballot from the data.
		return new Ballot(voter, votes);
	};

	/**
	 * This method could be left abstract and filled via an anonymous subclass.
	 * Anonymous subclasses are more convenient when a script needs
	 * a custom subclass of a class, but doesn't want to make a new class
	 * when little needs changing (i.e., a single overridden method).
	 */
	@Override
	protected Ballot parse(Element post) {
		return parser.apply(post);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected URL findNextLink(Document page)
	{
		try
		{
			// Use CSS query syntax to find the navigation bar to the next page.
			Elements nextElements = page
				.select(".PageNav") // Select navigation bar.
				.select("pageNav-jump")
				.select(".text") // Select prev/next button on the navbar.
				.select(":contains(next)"); // If inner HTML has 'next', it's the 'next' button.
			if (nextElements.isEmpty()) return null; // If we find no 'next' button, we're at the last page.

			// We want the absolute URL, hence why we use the 'abs' attribute prefix.
			// https://jsoup.org/cookbook/extracting-data/working-with-urls
			return new URL(nextElements.first().attr("abs:href"));
		}
		catch (IOException e){throw new AssertionError(e);}
	}

	@Override
	public List<Ballot> makeBallots(URL link) {
		List<Ballot> ballots = super.makeBallots(link);
		ballots.remove(0); // Skip the opening post.
		return ballots;
	}
}
