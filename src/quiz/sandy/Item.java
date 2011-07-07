package quiz.sandy;

import ecologylab.serialization.ElementState;
import ecologylab.serialization.ElementState.simpl_scalar;

public class Item extends ElementState{
	
	@simpl_scalar float price;
	@simpl_scalar String ownerName;
	@simpl_scalar String name;
	public Item(float price, String ownerName, String name) {
		super();
		this.price = price;
		this.ownerName = ownerName;
		this.name = name;
	}
}
