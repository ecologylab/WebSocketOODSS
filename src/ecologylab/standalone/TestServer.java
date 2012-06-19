/**
 * 
 */
package ecologylab.standalone;

import java.io.IOException;
import java.net.BindException;
import java.util.Iterator;

import org.jwebsocket.config.JWebSocketServerConstants;
import org.jwebsocket.console.JWebSocketServerLight;

import quiz.JoinGameRequest;
import quiz.QuizGameServer;

import ecologylab.collections.Scope;
import ecologylab.generic.HashMapArrayList;
import ecologylab.net.NetTools;
import ecologylab.oodss.distributed.server.OODSSWebSocketServer;
import ecologylab.oodss.distributed.server.DoubleThreadedNIOServer;
import ecologylab.oodss.messages.DefaultServicesTranslations;
import ecologylab.oodss.messages.UpdateMessage;
//import ecologylab.oodss.server.clientsessionmanager.NewTCPClientSessionManager;
import ecologylab.serialization.SimplTypesScope;

/**
 * @author Zachary O. Toups (toupsz@cs.tamu.edu)
 *
 */
public class TestServer
{

	/**
	 * @param args
	 * @throws IOException 
	 * @throws BindException 
	 */
	public static void main(String[] args) throws BindException, IOException
	{
		//JWebSocketServer.main(null);
		OODSSWebSocketServer s = new OODSSWebSocketServer(
				DefaultServicesTranslations.get(), 
				new Scope(), 
				100000, 
				100000);
		s.start();
		
		//JWebSocketServerLight.startWebsocketServer(s);
		
	}
	
	public static OODSSWebSocketServer getServer()
	{
		
		OODSSWebSocketServer s = null;
		try {
			/*
			s = NewExtendedServer.getInstance(
					7833, 
					NetTools.getAllInetAddressesForLocalhost(), 
					DefaultServicesTranslations.get(), 
					new Scope(), 
					100000, 
					100000);
					*/
			SimplTypesScope ts = DefaultServicesTranslations.get();
			ts.addTranslation(JoinGameRequest.class);
			s = QuizGameServer.getInstance(
					7833, 
					NetTools.getAllInetAddressesForLocalhost(), 
					ts, 
					new Scope(), 
					100000, 
					100000);
		} catch (BindException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		s.start();
		

		 System.out.println("Hey.  I just started up the server.");
		
		return s;
	}

}
