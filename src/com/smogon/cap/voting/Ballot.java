package com.smogon.cap.voting;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * An immutable representation of a voting ballot, 
 * consisting of a voter and the {@link List} of votes the voter has selected.
 * Both voters and votes are represented as {@link User Users}.
 */
public class Ballot
{
	private final User voter;
	private final List<User> votes;
	public Ballot(User voter, List<User> votes)
	{
		this.voter = voter;
		this.votes = Collections.unmodifiableList(votes); // make List immutable
	}
	public Ballot(Ballot ballot) // copy constructor
	{
		this.voter = ballot.getVoter();
		this.votes = ballot.getVotes(); // should be unmodifiable/immutable
	}

	// Getters
	public User getVoter(){return this.voter;}
	public List<User> getVotes(){return this.votes;}
	
	// Object methods
	@Override
	public boolean equals(Object o)
	{
		if (!(o instanceof Ballot)) return false;
		Ballot b = (Ballot) o;
		return this.getVoter().equals(b.getVoter())
			&& this.getVotes().equals(b.getVotes());
	}
	@Override public int hashCode() {return this.getVoter().hashCode() + this.getVotes().hashCode();}
	@Override public String toString() {return "Ballot for " + this.getVoter();}

	
	// Class methods

	/**
	 * Verifies that a list of ballots has no malformed ballots.
	 * @param ballots The ballots to validate.
	 */
	public static void validate(Collection<Ballot> ballots)
	{
		Set<User> seenVoters = new HashSet<>();
		for (Ballot ballot : ballots)
		{
			// Ensure each voter voted once.
			User voter = ballot.getVoter();
			if (seenVoters.contains(voter)) System.out.println("Invalid vote detected: "  + voter + " repeated post.");
			seenVoters.add(voter);

			// Ensure voter made at least one vote.
			if (ballot.getVotes().isEmpty()) System.out.println("Invalid vote detected: " + voter + " made no votes.");
			
			Set<User> seenVotes = new HashSet<>();
			for (User vote : ballot.getVotes())
			{
				// Ensure each voter voted for at most one instance of each option.
				if (seenVotes.contains(vote)) System.out.println("Invalid vote detected: "  + voter + " repeated vote " + vote);
				seenVotes.add(vote);
			}
		}
		// Otherwise, ballots are fine. :)
	}
}
