package com.smogon.cap.voting;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The class that combines all of the constituent classes for polling
 * and lets a user run a polling method on a forum thread.
 */
public final class Script
{
	
	// Instance fields
	
	/*
	 * The following field is an anonymous subclass of XenForoScraper.
	 * Anonymous subclasses are more convenient when a script needs
	 * a custom subclass of a class, but doesn't want to make a new clsas
	 * when little needs changing (i.e., a single overridden method).
	 */
	private XenForoScraper scraper = new XenForoScraper()
	{
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
			String html = post.select(".messageText").outerHtml();
			// Remove all HTMl except whitelisted tags.
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
		
		@Override public Ballot parse(Element post){return parser.apply(post);}
	};
	
	// TODO: Add more print statements.
	private void run(URL source, Poll poll, boolean verify)
	{
		System.out.println("Running script on " + source + "...");
		System.out.println();
		
		List<Ballot> ballots = this.scraper.makeBallots(source);
		if (verify)
		{
			System.out.println("Verifying ballots...");
			Ballot.validate(ballots);
			System.out.println("End of verification.");
			System.out.println();
		}
		SortedSet<Poll.Entry> results = poll.tally(ballots);

		System.out.println(poll.toString() + " results:");
		results.forEach(System.out::println);
		System.out.println();
		System.out.println("Total voters: " + ballots.size());
	}
	
	public static void main(String... args)
	{
		// http://stackoverflow.com/a/36787811
		if (args.length < 2)
		{
			System.out.println("Usage: <poll URL> <poll type> [-v]");
			System.out.println("<poll type> can be one of: ");
			for (Polls poll: Polls.values()) // Polls is the enum, Poll is the interface.
				System.out.println(poll.name());
			System.out.println("Add the [-v] flag to validate ballots before determining results.");
			return;
		}
		
		// else, generate the poll
		try
		{
			URL source = new URL(args[0]);
			Poll poll = Polls.valueOf(args[1]);
			boolean verify = (args.length >= 3 && args[2].equals("-v"));

			new Script().run(source, poll, verify);
		}
		catch (Error | Exception e)
		{
			System.out.println("Error or Exception while running the script: ");
			System.out.println(e.getMessage());
		}
	}
}

