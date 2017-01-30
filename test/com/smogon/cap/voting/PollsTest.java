package com.smogon.cap.voting;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PollsTest
{
	@Test
	public void testEmpty()
	{
		List<Ballot> empty = new ArrayList<>();
		for (Poll poll: Polls.values())
		{
			SortedSet<Poll.Entry> results = poll.tally(empty);
			assertTrue(results.isEmpty());
			// If the above doesn't work, it's usually caused by a NullPointerException.
		}
	}

	@Test
	public void testPlurality()
	{
		User u1 = new User("U1");
		User u2 = new User("U2");
		User u3 = new User("U3");

		Ballot b1 = new Ballot(u1, Arrays.asList(u1));
		Ballot b2 = new Ballot(u2, Arrays.asList(u2));
		Ballot b3 = new Ballot(u3, Arrays.asList(u3));

		List<Ballot> ballots = Arrays.asList(b1, b2, b3);

		SortedSet<Poll.Entry> FPTPVResults = Polls.FPTPV.tally(ballots);
		assertEquals(3, FPTPVResults.size());

		SortedSet<Poll.Entry> AVVResults = Polls.AV.tally(ballots);
		assertEquals(3, AVVResults.size());

		SortedSet<Poll.Entry> IRVResults = Polls.IRV.tally(ballots);
		assertEquals(3, IRVResults.size());

		SortedSet<Poll.Entry> PBVResults = Polls.PBV.tally(ballots);
		assertEquals(3, PBVResults.size());
	}

	@Test
	public void testTie()
	{
		User u1 = new User("U1");
		User u2 = new User("U2");
		User u3 = new User("U3");
		User u4 = new User("U4");
		User u5 = new User("U5");
		User u6 = new User("U6");

		Ballot b1 = new Ballot(u1, Arrays.asList(u1));
		Ballot b2 = new Ballot(u2, Arrays.asList(u1));
		Ballot b3 = new Ballot(u3, Arrays.asList(u1));
		Ballot b4 = new Ballot(u4, Arrays.asList(u2));
		Ballot b5 = new Ballot(u5, Arrays.asList(u2));
		Ballot b6 = new Ballot(u6, Arrays.asList(u2));

		List<Ballot> ballots = Arrays.asList(b1, b2, b3, b4, b5, b6);

		SortedSet<Poll.Entry> results = Polls.IRV.tally(ballots);
		System.out.println(results);

		assertEquals(2, results.size());
	}

	@Test
	public void testCaseInsensitivity()
	{
		User u1 = new User("U1");
		User u2 = new User("U2");

		User u1_ = new User("u1");

		Ballot b1 = new Ballot(u1, Arrays.asList(u1));
		Ballot b2 = new Ballot(u2, Arrays.asList(u1_));
		List<Ballot> ballots = Arrays.asList(b1, b2);

		SortedSet<Poll.Entry> results = Polls.IRV.tally(ballots);

		assertEquals(1, results.size());
		Poll.Entry result = results.first();
		assertEquals(u1, result.getUser());
		assertEquals(u1_, result.getUser());
	}
}
