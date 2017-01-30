package com.smogon.cap.voting;

import org.junit.Test;

/**
 */
public class ScriptTest
{
	@Test
	public void testMain()
	{
//		String link = "http://www.smogon.com/forums/threads/cap-22-pre-evo-part-6-name-poll-2.3590977/";
//		String link = "http://www.smogon.com/forums/threads/cap-22-pre-evo-part-3-art-poll-1.3588040/";
//		String link = "http://www.smogon.com/forums/threads/cap-22-part-7-art-poll-1.3579579/";
		String link = "http://www.smogon.com/forums/threads/cap-22-part-7-art-poll-2.3579745/";
		String pollName = "PBV";
//		String pollName = "AV";
		String flag = "-v";
		
//		URL source = new URL(link);
//		Poll poll = Polls.valueOf(pollName);
//		boolean validate = flag.equals("-v");
		
		Script.main();
		Script.main(link, pollName, flag);
	}
}