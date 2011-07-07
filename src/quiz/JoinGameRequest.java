package quiz;

import ecologylab.collections.Scope;
import ecologylab.generic.Debug;
import ecologylab.oodss.messages.OkResponse;
import ecologylab.oodss.messages.RequestMessage;
import ecologylab.oodss.messages.ResponseMessage;
import ecologylab.serialization.simpl_inherit;
import ecologylab.serialization.ElementState.simpl_scalar;


public @simpl_inherit class JoinGameRequest extends RequestMessage {

	@simpl_scalar
	String playerName;
	
	@Override
	public ResponseMessage performService(Scope clientSessionScope) {
		QuizGameServer server = ((QuizGameServer)clientSessionScope.get("server"));
		Debug.println(playerName+" just decided to join the game");
		server.players.add(new Player(playerName, 1, ""));
		//need new method
		server.UpdatePlayers();
		return OkResponse.get();
	}
	
	public JoinGameRequest(){}
	
	public JoinGameRequest(String name){
		playerName = name;
		Scope done = null;
	}

}
