package com.smogon.cap.voting;

import java.net.URL;
import java.util.List;


/**
 * A representation of an object that converts
 * a {@link URL} into a {@link List} of {@link Ballot Ballots}.
 *
 */
public abstract class Scraper
{
	/**
	 * Default constructor.
	 * A {@code Scraper} has no internal state,
	 * so it shouldn't really be instantiatable.
	 * Everything should be static and class-based,
	 * but it is impossible to override static methods,
	 * which is required for anonymous subclassing.
	 * (Hence why {@link Scraper#makeBallots(URL)} is instance-based.)
	 * <p>
	 * A {@code Scraper} does not take a {@link URL} as a field,
	 * as it represents a function that takes in {@code URLs},
	 * not a {@code URL} that can have functions applied on it.
	 */
	protected Scraper(){}

	/**
	 * Given a {@link URL}, parses the URL and converts
	 * the results into a {@link List} of {@link Ballot Ballots}.
	 * @param link The link to scrape.
	 * @return A list of all {@code Ballots} scraped from the URL.
	 */
	public abstract List<Ballot> makeBallots(URL link);
}
