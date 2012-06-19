/**
 * 
 */
package ecologylab.standalone;

import java.io.IOException;

import ecologylab.collections.Scope;
import ecologylab.oodss.distributed.client.OODSSWebSocketClient;
import ecologylab.oodss.distributed.exception.MessageTooLargeException;
import ecologylab.oodss.messages.DefaultServicesTranslations;
import ecologylab.oodss.messages.Ping;
import ecologylab.oodss.messages.PingRequest;
import ecologylab.serialization.SimplTypesScope;

/**
 * @author Zachary O. Toups (toupsz@cs.tamu.edu)
 *
 */
public class TestClinetMerged implements TestUpdateMessageListener
{

	public static TestClinetMerged myInstance;
	/**
	 * @param args
	 * @throws IOException 
	 * @throws MessageTooLargeException 
	 */
	public static void main(String[] args) throws IOException, MessageTooLargeException
	{
		 SimplTypesScope ts = DefaultServicesTranslations.get();
		 ts.addTranslation(TestUpdateMessage.class);
		 Scope scope = new Scope();
		 myInstance = new TestClinetMerged();
		 
		 OODSSWebSocketClient c = new OODSSWebSocketClient("localhost", 7833,
				ts, scope);
		 scope.put(TestUpdateMessageListener.TEST_UPDATE_LISTENER, myInstance);
		
	//	c.isServerRunning();

	//	c.connect();
		
		long startTime = System.currentTimeMillis();
		c.sendMessage(new PingRequest());
		long endTime = System.currentTimeMillis();
		System.out.println("round trip: "+(endTime - startTime)+"ms");
		
		startTime = System.currentTimeMillis();
		c.sendMessage(new PingRequest());
		endTime = System.currentTimeMillis();
		System.out.println("round trip: "+(endTime - startTime)+"ms");
		
		startTime = System.currentTimeMillis();
		c.sendMessage(new PingRequest());
		endTime = System.currentTimeMillis();
		System.out.println("round trip: "+(endTime - startTime)+"ms");
		
		c.disconnect();
	}

	@Override
	public void recievedUpdate(TestUpdateMessage update)
	{
		System.out.println("Now there is an update from the server");
		System.out.println("Message:"+update.getMessage());
		System.out.println("Tone:"+update.getTone());
		System.out.println("Points:"+update.getPoints());
	}
}
