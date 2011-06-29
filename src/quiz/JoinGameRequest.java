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
		Debug.println(playerName+" just decided to join the game");
		return OkResponse.get();
	}
	
	public JoinGameRequest(){}
	
	public JoinGameRequest(String name){
		playerName = name;
	}

}
