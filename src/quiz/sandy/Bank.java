package quiz.sandy;

import java.util.HashMap;

import ecologylab.serialization.ElementState;

import ecologylab.serialization.annotations.*;

public class Bank extends ElementState{
	@simpl_nowrap
	@simpl_map("items")
	@simpl_map_key_field("name")
	HashMap<String, Item> itemMap;

	public Bank(HashMap<String, Item> itemMap) {
		super();
		this.itemMap = itemMap;
	}
}
