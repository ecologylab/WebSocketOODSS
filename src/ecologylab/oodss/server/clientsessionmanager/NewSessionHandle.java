package ecologylab.oodss.server.clientsessionmanager;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import ecologylab.collections.Scope;
import ecologylab.oodss.messages.UpdateMessage;

/**
 * 
 * @author William A. Hamilton (bill@ecologylab.net)
 */
public class NewSessionHandle
{
	private NewBaseSessionManager	sessionManager;

	public NewSessionHandle(NewBaseSessionManager cm)
	{
		sessionManager = cm;
	}

	public InetAddress getInetAddress()
	{
		return getSocketAddress().getAddress();
	}

	public InetSocketAddress getSocketAddress()
	{
		return sessionManager.getAddress();
	}
	
	public int getPortNumber()
	{
		return getSocketAddress().getPort();
	}

	public void sendUpdate(UpdateMessage update)
	{
		sessionManager.sendUpdateToClient(update);
	}

	public Scope getSessionScope()
	{
		return sessionManager.getScope();
	}

	public void invalidate()
	{
		sessionManager.setInvalidating(true);
	}

	public Object getSessionId()
	{
		return sessionManager.getSessionId();
	}

	@Override
	public boolean equals(Object other)
	{
		if (other instanceof NewSessionHandle)
		{
			return this.getSessionId().equals(((NewSessionHandle) other).getSessionId());
		}
		else
		{
			return false;
		}
	}
}
