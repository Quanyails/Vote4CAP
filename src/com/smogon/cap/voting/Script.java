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

	private Scraper scraper = new XenForoScraper();
	
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
			e.printStackTrace();
		}
	}
}
