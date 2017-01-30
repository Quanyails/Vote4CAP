package com.smogon.cap.voting;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;

/**
 * A {@code Poll} can be thought of as an algorithm for converting 
 * a {@link List} of {@link Ballot Ballots} into a ranking--a {@link SortedSet} of {@code Poll} entries.
 * 
 */
@FunctionalInterface
public interface Poll
{
	/**
	 * A nested class consisting of a {@code User}, {@code double} pair,
	 * that is analogous to {@link java.util.Map.Entry Map.Entry}{@code <User, Double>}.
	 * <p>
	 * The {@code double} value represents a ranking.
	 * However, the meaning of the value is arbitrary, being defined by the implementation.
	 * What matters to a {@code Poll} is the order the values are sorted.
	 */
	class Entry
	{
		private User user;
		private Number number; // Internally used for information preservation.
		private double value; // Used for arithmetic operations.

		// Constructor
		public Entry(User user, Number number)
		{
			this.user = user;
			this.number = number;
			this.value = number.doubleValue();
		}
		
		// Getters
		public User getUser(){return this.user;}
		public double getValue(){return this.value;}

		// Object overridden methods
		@Override public boolean equals(Object o)
		{
			if (!(o instanceof Entry)) return false;
			Entry f = (Entry) o;
			// We don't compare their Numbers, since that's a hidden implementation detail.
			return this.getUser().equals(f.getUser())
				&& this.getValue() == f.getValue();
		}
		@Override public int hashCode(){return this.user.hashCode() + Double.hashCode(value);}
		@Override public String toString()
		{
			return this.user.toString() + ": " + this.number.toString(); // number, not value
		}

		// Comparators:
		// We allow the interface two comparators instead of making Poll.Entry
		// implement Comparable<Entry>, since there is no canonical ordering
		// of what value is considered "better" than another value.
		private static final Comparator<Entry> doubleComparator = Comparator.comparingDouble(Entry::getValue);
		private static final Comparator<Entry> stringComparator =
			(e1, e2) -> String.CASE_INSENSITIVE_ORDER.compare(e1.getUser().getName(), e2.getUser().getName());
		public static final Comparator<Entry> INCREASING = doubleComparator.thenComparing(stringComparator);
		public static final Comparator<Entry> DECREASING = doubleComparator.reversed().thenComparing(stringComparator);
	}
	
	// Interface method

	/**
	 * Tallies ballots into a ranking.
	 * <p>
	 * The {@link SortedSet} produced from the method must not be null, and 
	 * it must be ranked from the best option to the worst, 
	 * although it is implementation-defined exactly how the {@code Users} are ranked.
	 * <p>
	 * We use a {@code} Collection as the data structure for holding ballots,
	 * as we need the size of the voters (for majorities)
	 * and we need to be able to iterate over the elements.
	 * @param ballots An collection of ballots to process.
	 * @return A sorted set of entries.
	 */
	SortedSet<Entry> tally(Collection<Ballot> ballots);
}

