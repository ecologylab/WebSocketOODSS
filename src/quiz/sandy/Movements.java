package quiz.sandy;

import java.util.ArrayList;

import ecologylab.serialization.ElementState;
import ecologylab.serialization.ElementState.simpl_collection;
import ecologylab.serialization.ElementState.simpl_scalar;

public class Movements extends ElementState {
	@simpl_scalar float time;
	@simpl_nowrap
	@simpl_collection("moves") ArrayList<Move> moves;
	public Movements(float time, ArrayList<Move> moves) {
		super();
		this.time = time;
		this.moves = moves;
	}
}
