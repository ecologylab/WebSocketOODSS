/**
 * 
 */
package ecologylab.standalone;

import java.io.IOException;
import java.net.BindException;

import org.jwebsocket.config.JWebSocketServerConstants;

import ecologylab.collections.Scope;
import ecologylab.net.NetTools;
import ecologylab.oodss.distributed.server.DoubleThreadedAIOServer;
import ecologylab.oodss.distributed.server.DoubleThreadedNIOServer;
import ecologylab.oodss.messages.DefaultServicesTranslations;
import ecologylab.oodss.messages.UpdateMessage;

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
		DoubleThreadedAIOServer s = null;
		try {
			s = DoubleThreadedAIOServer.getInstance(
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
	
		
		return s;
	}

}
