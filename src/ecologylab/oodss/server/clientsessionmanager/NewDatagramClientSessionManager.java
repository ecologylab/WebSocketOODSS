/**
 * 
 */
package ecologylab.oodss.server.clientsessionmanager;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;

import ecologylab.collections.Scope;
import ecologylab.oodss.distributed.server.AIODatagramServer;
import ecologylab.oodss.messages.RequestMessage;
import ecologylab.oodss.messages.ResponseMessage;
import ecologylab.oodss.messages.UpdateMessage;

/**
 * @author Zachary O. Toups (zach@ecologylab.net)
 * 
 */
public class NewDatagramClientSessionManager extends NewBaseSessionManager
{
	InetSocketAddress	address;
	
	AIODatagramServer server;
	
	SelectionKey mostRecentKey;
	
	/**
	 * @param sessionId
	 * @param socket
	 * @param baseScope
	 */
	public NewDatagramClientSessionManager(String sessionId, AIODatagramServer aioDatagramServer,
			SelectionKey socket, Scope<?> baseScope, InetSocketAddress	address)
	{
		super(sessionId, socket, baseScope);
		
		this.address = address;
		this.server = aioDatagramServer;
	}

	/**
	 * Calls RequestMessage.performService(Scope) and returns the result.
	 * 
	 * @param request
	 *          - the request message to process.
	 */
	@Override
	public ResponseMessage processRequest(RequestMessage request, InetAddress address)
	{
		return super.processRequest(request, address);
	}

	public InetSocketAddress getAddress()
	{
		return address;
	}

	@Override
  public void sendUpdateToClient(UpdateMessage update) 
	{
		server.sendMessage(update, mostRecentKey, (long) -1, getAddress());
  }

	public void updateKey(SelectionKey key) 
	{
		mostRecentKey = key;
  }
}
