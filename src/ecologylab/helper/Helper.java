package ecologylab.helper;

import java.io.ByteArrayOutputStream;

import ecologylab.serialization.ElementState;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.SimplTypesScope;
import ecologylab.serialization.formatenums.Format;

//This includes helper functions that I use for debugging that I may need to implement other places

public class Helper {

public static String ElementStateToJSON(ElementState elementState)
   {
	ByteArrayOutputStream outStream = new ByteArrayOutputStream();
	try {
		SimplTypesScope.serialize(elementState,outStream, Format.JSON);
		//elementState.serialize(outStream, FORMAT.JSON);
	} catch (SIMPLTranslationException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
	String pJSON = new String(outStream.toByteArray());
	return pJSON;
   }
	
}
