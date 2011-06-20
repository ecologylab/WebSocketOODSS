/**
 * 
 */
package ecologylab.standalone;

import java.io.IOException;
import java.net.BindException;
import java.util.Iterator;

import org.jwebsocket.config.JWebSocketServerConstants;

import ecologylab.collections.Scope;
import ecologylab.generic.HashMapArrayList;
import ecologylab.net.NetTools;
import ecologylab.oodss.distributed.server.DoubleThreadedAIOServer;
import ecologylab.oodss.distributed.server.DoubleThreadedNIOServer;
import ecologylab.oodss.messages.DefaultServicesTranslations;
import ecologylab.oodss.messages.UpdateMessage;
import ecologylab.oodss.server.clientsessionmanager.NewTCPClientSessionManager;

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
		DoubleThreadedAIOServer s = DoubleThreadedAIOServer.getInstance(
				7833, 
				NetTools.getAllInetAddressesForLocalhost(), 
				DefaultServicesTranslations.get(), 
				new Scope(), 
				100000, 
				100000);
		
		s.start();
		
	}
	
	public static DoubleThreadedAIOServer getServer()
	{
		NewExtendedServer s = null;
		try {
			s = NewExtendedServer.getInstance(
					7833, 
					NetTools.getAllInetAddressesForLocalhost(), 
					DefaultServicesTranslations.get(), 
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
