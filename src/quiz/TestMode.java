package quiz;

import ecologylab.helper.Helper;

public class TestMode {

	public static void main(String[] args)
	{
		JoinGameRequest joinGameRequest = new JoinGameRequest("Rhema");
		System.out.println("Here is the request with the name \"Rhema\"");
		
		//joinGameRequest.serialize(outStream, format)
		System.out.println(Helper.ElementStateToJSON(joinGameRequest));
	}
}
