package quiz;

import ecologylab.serialization.ElementState;
import ecologylab.serialization.annotations.simpl_scalar;


public class Player extends ElementState{

	@simpl_scalar
	String name;
	
	@simpl_scalar
	int score;
	
	@simpl_scalar
	String message;
	
	public Player()
	{
	}
	
	public Player(String name, int score, String message)
	{
		this.name = name;
		this.score = score;
		this.message = message;
	}
	
}
