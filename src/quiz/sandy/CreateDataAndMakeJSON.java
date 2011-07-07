package quiz.sandy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ecologylab.helper.Helper;

public class CreateDataAndMakeJSON {

	public static void main(String args[])
	{
		//make a few players.
		System.out.println("Player");
		System.out.println(Helper.ElementStateToJSON(new Player("Bob", 4, 3, 2)));
		System.out.println("Human");
		System.out.println(Helper.ElementStateToJSON(new Human(3, 4, (float)4.6, "Sam", 33, 44, 2)));
		System.out.println("Computer");
		System.out.println(Helper.ElementStateToJSON(new Computer((float) 8.5, "monster", "sneaky", "Jarvis", 30, 33, 6)));
		System.out.println("Move");
		Move m = new Move(4,4,true,false,4);
		System.out.println(Helper.ElementStateToJSON(m));
		System.out.println("Movements");
		ArrayList<Move> moves = new ArrayList<Move>();
		for(int i=0;i<10;i++)
			moves.add(new Move(4*i,i*3,true,false,4));
		Movements movements = new Movements(5, moves);
		System.out.println(Helper.ElementStateToJSON(movements));
		System.out.println("Item");
	    System.out.println(Helper.ElementStateToJSON(new Item(25,"Rhema","pick")));
	    HashMap<String, Item> itemMap = new HashMap<String, Item>();
	    itemMap.put("pick1", new Item(25,"Rhema","pick"));
	    itemMap.put("pick2", new Item(25,"Rhema","gold-pick"));
	    itemMap.put("pick3", new Item(25,"Rhema","iron-pick"));
	    itemMap.put("door1", new Item(25,"George","door"));
	    System.out.println("Bank");
		System.out.println( Helper.ElementStateToJSON( new Bank(itemMap)));
		
	}
}
