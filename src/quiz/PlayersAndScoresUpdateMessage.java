package quiz;

import java.util.ArrayList;

import ecologylab.oodss.messages.UpdateMessage;
import ecologylab.serialization.annotations.simpl_collection;

public class PlayersAndScoresUpdateMessage extends UpdateMessage {
	
	@simpl_collection("player")
	ArrayList<Player> players = new ArrayList<Player>();
	
	public PlayersAndScoresUpdateMessage()
	{
		System.out.println("Hey.  There are this many things in the array list:"+players.size());
	}
	
	public PlayersAndScoresUpdateMessage(ArrayList<Player> players)
	{
		this.players = players;
	}
	
}
