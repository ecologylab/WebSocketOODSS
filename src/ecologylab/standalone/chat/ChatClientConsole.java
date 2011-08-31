/**
 * 
 */
package ecologylab.standalone.chat;

import java.io.IOException;
import java.util.Scanner;

import ecologylab.collections.Scope;
import ecologylab.oodss.distributed.client.AIOClient;
import ecologylab.oodss.distributed.exception.MessageTooLargeException;
import ecologylab.oodss.messages.DefaultServicesTranslations;
import ecologylab.oodss.messages.Ping;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.TranslationScope;

/**
 * @author Zachary O. Toups (toupsz@cs.tamu.edu)
 *
 */
public class ChatClientConsole implements ChatUpdateListener
{

	/**
	 * @param args
	 * @throws IOException 
	 * @throws MessageTooLargeException 
	 */
	public static void main(String[] args) throws IOException, MessageTooLargeException 
	{
		AIOClient c = new AIOClient("localhost", 7833,
				 ChatServer.getTranslationScope(), new Scope());
		
		c.isServerRunning();

		c.connect();
		
		try {
			c.translateJSONStringToServiceMessage("{\"ok_response\":{}}");
			System.out.println("This did not fail.");
		} catch (SIMPLTranslationException e) {
			System.out.println("This failed.");
		}
		/*
		long startTime = System.currentTimeMillis();
		c.sendMessage(new Ping());
		long endTime = System.currentTimeMillis();
		System.out.println("round trip: "+(endTime - startTime)+"ms");
		
		startTime = System.currentTimeMillis();
		c.sendMessage(new Ping());
		endTime = System.currentTimeMillis();
		System.out.println("round trip: "+(endTime - startTime)+"ms");
		
		startTime = System.currentTimeMillis();
		c.sendMessage(new Ping());
		endTime = System.currentTimeMillis();
		System.out.println("round trip: "+(endTime - startTime)+"ms");
		*/
		String name;
		String message = "null";
		
		System.out.println("Please type your name and press enter:");
		Scanner scanner = new Scanner(System.in);
		name = scanner.nextLine();
		
		while(message.length() > 0)
		{
			System.out.print("Message:");
			message = scanner.nextLine();
			c.sendMessage(new SendChatMessage(name,message));
		}
		
		c.disconnect();
	}

	@Override
	public void recievedUpdate(ChatUpdate response) {
		System.out.println("BZZZZZZZZZZZZZZ");
		System.out.println(response.getMessage());
	}

}
