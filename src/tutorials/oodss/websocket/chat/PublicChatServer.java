package tutorials.oodss.websocket.chat;

import java.io.IOException;
import java.net.InetAddress;

import ecologylab.collections.Scope;
import ecologylab.net.NetTools;
import ecologylab.oodss.distributed.server.OODSSWebSocketServer;
import ecologylab.oodss.distributed.server.DoubleThreadedNIOServer;
import ecologylab.serialization.SimplTypesScope;

/**
 * PublicChatServer: A sample server implemented via OODSS. Intended to be used
 * as a tutorial application.
 * 
 * @author bill
 */
public class PublicChatServer
{
	private static final int	idleTimeout	= -1;
	private static final int	MTU			= 40000;

	public static void main(String[] args) throws IOException
	{
		/*
		 * get base translations with static accessor
		 */
		SimplTypesScope publicChatTranslations = ChatTranslations.get();

		/*
		 * Creates a scope for the server to use as an application scope as well
		 * as individual client session scopes.
		 */
		Scope applicationScope = new Scope();

		/* Acquire an array of all local ip-addresses */
		InetAddress[] locals = NetTools.getAllInetAddressesForLocalhost();

		/*
		 * Create the server and start the server so that it can accept incoming
		 * connections.
		 */

		OODSSWebSocketServer historyServer = new OODSSWebSocketServer(publicChatTranslations,
						applicationScope, idleTimeout, MTU);
		

		/*DoubleThreadedNIOServer historyServer = DoubleThreadedNIOServer
				.getInstance(2108, locals, publicChatTranslations,
						applicationScope, idleTimeout, MTU);*/
		
		historyServer.start();
	}
}
