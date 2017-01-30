package com.smogon.cap.voting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * An enumeration of various polling methods subscribing to the {@link Poll} interface.
 * <p>
 * Why are polling methods represented as an {@code enum} and not a {@code class}?
 * Given that each polling method represents a specific, unique action rather than an object,
 * there only needs to be one instance of each sort of {@code Poll}.
 * Having multiple instances of a {@code Poll} that act exactly the same calls for an {@code enum}.
 * <p>
 * Additionally, it is more elegant and efficient 
 * to directly call {@code Polls.method.tally()}
 * instead of {@code new MethodPoll().tally()}.
 */
public enum Polls implements Poll
{
	/**
	 * First-past-the-post voting = single bold voting
	 */
	FPTPV
	{
		@Override
		public SortedSet<Entry> tally(Collection<Ballot> ballots)
		{
			Map<User, Integer> frequencies = new HashMap<>();
			// For each ballot, add the top vote.
			for (Ballot ballot : ballots)
				if (!ballot.getVotes().isEmpty())
					frequencies.merge(ballot.getVotes().get(0), 1, Integer::sum);

			return frequencies.entrySet().stream()
				.map(mse -> new Entry(mse.getKey(), mse.getValue()))
				.sorted(Entry.DECREASING)
				.collect(Polls.DECREASING_COLLECTOR);
		}
	}
	/**
	 * Approval voting = multiple bold voting
	 */
	, AV
	{
		@Override
		public SortedSet<Entry> tally(Collection<Ballot> ballots)
		{
			Map<User, Integer> frequencies = new HashMap<>();
			// For each ballot, add all votes on the ballot.
			for (Ballot ballot: ballots)
				for (User vote: ballot.getVotes())
					frequencies.merge(vote, 1, Integer::sum);

			return frequencies.entrySet().stream()
				.map(mse -> new Entry(mse.getKey(), mse.getValue()))
				.sorted(Entry.DECREASING)
				.collect(Polls.DECREASING_COLLECTOR);
		}
	}
	/**
	 * Instant runoff voting
	 */
	, IRV 
	{
		private boolean canIterate(SortedSet<Entry> rankings, int majority)
		{
			// Can't iterate on an empty set.
			if (rankings.isEmpty()) return false;
			
			// Can't iterate if everyone is tied, or there is a single winner.
			long distinctValues = rankings.stream().map(Entry::getValue).distinct().count();
			if (distinctValues == 1) return false;
			
			// Can't iterate if we've reached the majority needed.
			if (rankings.first().getValue() >= majority) return false;
			
			return true;
		}
		
		@Override
		public SortedSet<Entry> tally(Collection<Ballot> ballots)
		{
			final int population = ballots.size();
			final int majority = (int) Math.floor(0.5 * population) + 1;

			List<Ballot> roundBallots = new ArrayList<>(ballots); // make copy
			SortedSet<Entry> roundRankings = Polls.FPTPV.tally(roundBallots);
			while (this.canIterate(roundRankings, majority))
			{
				final double lastValue = roundRankings.last().getValue();

				Set<User> lastPlaces = roundRankings.stream()
					.filter(e -> e.getValue() == lastValue)
					.map(Entry::getUser)
					.collect(Collectors.toSet());

				roundBallots = Polls.filterOut(roundBallots, lastPlaces); // round ballots, not original ballots
				roundRankings = Polls.FPTPV.tally(roundBallots);
			}
			
			// TODO: Find a better place for the empty check?
			if (roundRankings.isEmpty()) return roundRankings;

			final double firstValue = roundRankings.first().getValue();

			return roundRankings.stream()
				.filter(e -> e.getValue() == firstValue)
				.collect(Polls.DECREASING_COLLECTOR);
		}
	}
	/**
	 * Preferential block voting
	 */
	, PBV
	{
		@Override
		public SortedSet<Entry> tally(Collection<Ballot> ballots)
		{
			SortedSet<Entry> rankings = Polls.INCREASING_SUPPLIER.get();
			
			int round = 1; // Make the round number 1-based for user-friendliness.
			List<Ballot> roundBallots = new ArrayList<>(ballots); // make copy
			while (!roundBallots.isEmpty())
			{
				SortedSet<Entry> roundResults = Polls.IRV.tally(roundBallots);
				
				Set<User> roundSeenUsers = roundResults.stream()
					.map(Entry::getUser)
					.collect(Collectors.toSet());
				
				final int currentRound = round; // final modifier needed for stream
				Set<Entry> roundRankings = roundSeenUsers.stream()
					.map(u -> new Entry(u, currentRound))
					.collect(Collectors.toSet());
				
				rankings.addAll(roundRankings);
				roundBallots = Polls.filterOut(roundBallots, roundSeenUsers); // not ballots
				round += roundSeenUsers.size();
			}
			return rankings;
		}
	}
	;

	// Suppliers/Collectors (for custom ranking):
	// Below are convenience constants to generate instances for and collectors for SortedSets,
	// Using TreeSets with customizable comparators.

	// Suppliers: Generate an instance of a particular class (in this case, a TreeSet).
	// Collectors: Collect a Stream of entries into a final Collection (in this case, a TreeSet).
	private static Supplier<TreeSet<Poll.Entry>> INCREASING_SUPPLIER = () -> new TreeSet<>(Entry.INCREASING);
	private static Supplier<TreeSet<Entry>> DECREASING_SUPPLIER = () -> new TreeSet<>(Entry.DECREASING);

	private static Collector<Entry, ?, TreeSet<Entry>> DECREASING_COLLECTOR
		= Collectors.toCollection(Polls.DECREASING_SUPPLIER);
	private static Collector<Entry, ?, TreeSet<Entry>> INCREASING_COLLECTOR
		= Collectors.toCollection(Polls.INCREASING_SUPPLIER);
	
	
	// Helper function.
	// Returns a new ballot sans the options provided.
	// http://stackoverflow.com/a/31873836
	private static Ballot filterOut(Ballot ballot, Collection<User> options)
	{
		List<User> newVotes = new ArrayList<>();
		for (User vote: ballot.getVotes())
			if (!options.contains(vote)) newVotes.add(vote);
		return new Ballot(ballot.getVoter(), newVotes);
	}
	private static List<Ballot> filterOut(List<Ballot> ballots, Collection<User> options)
	{
		List<Ballot> newBallots = new ArrayList<>();
		for (Ballot ballot: ballots)
		{
			Ballot newBallot = Polls.filterOut(ballot, options);
			if (!newBallot.getVotes().isEmpty())
				newBallots.add(newBallot);
		}
		return newBallots;
	}
}
