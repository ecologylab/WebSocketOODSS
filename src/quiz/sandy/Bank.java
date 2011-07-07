package quiz.sandy;

import java.util.HashMap;

import ecologylab.serialization.ElementState;
import ecologylab.serialization.ElementState.simpl_map;

public class Bank extends ElementState{
	@simpl_map("items")
	HashMap<String, Item> itemMap;

	public Bank(HashMap<String, Item> itemMap) {
		super();
		this.itemMap = itemMap;
	}
}
