package com.smogon.cap.voting;

/**
 * A representation of a user.
 * <p>
 * This class is currently a {@code CaseInsensitiveString}, 
 * but the script uses this class instead of s {@link String}, 
 * as the latter requires putting {@link String#equalsIgnoreCase(String)}
 * and {@link String#CASE_INSENSITIVE_ORDER} in multiple places to ensure correctness, 
 * which is harder to maintain, rather than 
 * having data structures such as Sets and Streams call the natural {@code equals()} and {@code hashCode()} methods.
 *
 * Additionally, a if the definition of equality changes,
 * it would be trivial to modify the method in this class.
 * 
 * <p>
 * This class is preferred to a using {@code String} in the {@link Ballot} class
 * due to different definitions of equality (i.e., case insensitivity).
 */
public class User
{
	private final String name;
	public User(String name){this.name = name;}
	
	public String getName(){return this.name;}
	
	@Override public boolean equals(Object o)
	{
		if (!(o instanceof User)) return false;
		User u = (User) o;
		return this.name.toLowerCase().equals(u.name.toLowerCase());
			
	}
	@Override public int hashCode()
	{
		return this.name.toLowerCase().hashCode();
	}
	@Override public String toString(){return this.name;}
}
