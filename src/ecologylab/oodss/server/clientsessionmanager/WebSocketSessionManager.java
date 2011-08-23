/**
 * 
 */
package ecologylab.oodss.server.clientsessionmanager;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;

import ecologylab.collections.Scope;
import ecologylab.generic.Debug;
import ecologylab.oodss.distributed.server.clientsessionmanager.SessionHandle;
import ecologylab.oodss.messages.BadSemanticContentResponse;
import ecologylab.oodss.messages.InitConnectionRequest;
import ecologylab.oodss.messages.InitConnectionResponse;
import ecologylab.oodss.messages.RequestMessage;
import ecologylab.oodss.messages.ResponseMessage;
import ecologylab.oodss.messages.UpdateMessage;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.TranslationScope;
import ecologylab.serialization.ElementState.FORMAT;

/**
 * @author Zachary O. Toups (zach@ecologylab.net)
 * 
 */
public class WebSocketSessionManager<S extends Scope> extends Debug
{

	/**
	 * Indicates whether or not one or more messages are queued for execution by this ContextManager.
	 */
	protected boolean							messageWaiting	= false;

	/**
	 * Session handle available to use by clients
	 */
	protected SessionHandle				handle;

	/**
	 * sessionId uniquely identifies this ContextManager. It is used to restore the state of a lost
	 * connection.
	 */
	protected String							sessionId				= null;

	protected S										localScope;

	protected long								lastActivity		= System.currentTimeMillis();

	/** The selection key for this context manager. */
	protected SelectionKey				socketKey;

	/**
	 * The frontend for the server that is running the ContextManager. This is needed in case the
	 * client attempts to restore a session, in which case the frontend must be queried for the old
	 * ContextManager.
	 */

	/**
	 * Indicates whether the first request message has been received. The first request may be an
	 * InitConnection, which has special properties.
	 */
	protected boolean							initialized			= false;

	/**
	 * Used for disconnecting. A disconnect message will call the setInvalidating method, which will
	 * set this value to true. The processing method will set itself as pending invalidation after it
	 * has produces the bytes for the response to the disconnect message.
	 */
	private boolean								invalidating		= false;

	private TranslationScope translationScope;

	public static final String		SESSION_ID			= "SESSION_ID";

	public static final String		CLIENT_MANAGER	= "CLIENT_MANAGER";

	/**
	 * 
	 */
	public WebSocketSessionManager(String sessionId, SelectionKey socket,
			Scope<?> baseScope)
	{
		super();

		this.socketKey = socket;
		this.sessionId = sessionId;

		this.localScope = generateContextScope(baseScope);

		this.localScope.put(SESSION_ID, sessionId);
		this.localScope.put(CLIENT_MANAGER, this);
	}

	public WebSocketSessionManager(String sessionId2, TranslationScope translationScope,
			S applicationObjectScope) {
		this.sessionId = sessionId2;
		this.localScope = applicationObjectScope;
		this.translationScope = translationScope;
		}

	/**
	 * Provides the context scope for the client attached to this session manager. The base
	 * implementation instantiates a new Scope<?> with baseScope as the argument. Subclasses may
	 * provide specific subclasses of Scope as the return value. They must still incorporate baseScope
	 * as the lexically chained application object scope.
	 * 
	 * @param baseScope
	 * @return
	 */
	protected S generateContextScope(Scope<?> baseScope)
	{
		return (S) new Scope(baseScope);
	}


	
protected ResponseMessage performService(RequestMessage requestMessage)
	{
		//requestMessage.setSender(address);

		try
		{
			return requestMessage.performService(localScope);
		}
		catch (Exception e)
		{
			e.printStackTrace();

			return new BadSemanticContentResponse("The request, "
					+ requestMessage.toString()
					+ " caused an exception on the server.");
		}
	}
	
	
	/**
	 * Appends the sender's IP address to the incoming message and calls performService on the given
	 * RequestMessage using the local ObjectRegistry.
	 * 
	 * performService(RequestMessage) may be overridden by subclasses to provide more specialized
	 * functionality. Generally, overrides should then call super.performService(RequestMessage) so
	 * that the IP address is appended to the message.
	 * 
	 * @param requestMessage
	 * @return
	 */
	protected ResponseMessage performService(RequestMessage requestMessage, InetAddress address)
	{
		requestMessage.setSender(address);

		try
		{
			return requestMessage.performService(localScope);
		}
		catch (Exception e)
		{
			e.printStackTrace();

			return new BadSemanticContentResponse("The request, "
					+ requestMessage.toString()
					+ " caused an exception on the server.");
		}
	}

	/**
	 * Calls RequestMessage.performService(Scope) and returns the result.
	 * 
	 * @param request
	 *          - the request message to process.
	 */
	protected ResponseMessage processRequest(RequestMessage request, InetAddress address)
	{
		this.lastActivity = System.currentTimeMillis();

		ResponseMessage response = null;

		if (request == null)
		{
			debug("No request.");
		}
		else
		{
			if (!isInitialized())
			{
				// special processing for InitConnectionRequest
				if (request instanceof InitConnectionRequest)
				{
					String incomingSessionId = ((InitConnectionRequest) request).getSessionId();

					if (incomingSessionId == null)
					{ // client is not expecting an old ContextManager
						response = new InitConnectionResponse(this.sessionId);
					}
					else
					{ // client is expecting an old ContextManager
						
							response = new InitConnectionResponse(this.sessionId);
						
					}

					initialized = true;
				}
			}
			else
			{
				// perform the service being requested
				response = performService(request, address);
			}

			if (response == null)
			{
				debug("context manager did not produce a response message.");
			}
		}

		return response;
	}

	

	/**
	 * Calls RequestMessage.performService(Scope) and returns the result.
	 * 
	 * @param request
	 *          - the request message to process.
	 */
	public /*protected*/ ResponseMessage processRequest(RequestMessage request)
	{
		this.lastActivity = System.currentTimeMillis();

		ResponseMessage response = null;

		if (request == null)
		{
			debug("No request.");
		}
		
		else
		{		
			response = performService(request);
		}
		

		return response;
	}

	
	
	/**
	 * Indicates the last System timestamp was when the ContextManager had any activity.
	 * 
	 * @return the last System timestamp indicating when the ContextManager had any activity.
	 */
	public final long getLastActivity()
	{
		return lastActivity;
	}

	/**
	 * @return the socket
	 */
	public SelectionKey getSocketKey()
	{
		return socketKey;
	}

	/**
	 * Indicates whether there are any messages queued up to be processed.
	 * 
	 * isMessageWaiting() should be overridden if getNextRequest() is overridden so that it properly
	 * reflects the way that getNextRequest() works; it may also be important to override
	 * enqueueRequest().
	 * 
	 * @return true if getNextRequest() can return a value, false if it cannot.
	 */
	public boolean isMessageWaiting()
	{
		return messageWaiting;
	}

	/**
	 * Indicates whether or not this context manager has been initialized. Normally, this means that
	 * it has shared a session id with the client.
	 * 
	 * @return
	 */
	public boolean isInitialized()
	{
		return initialized;
	}

	/**
	 * @param invalidating
	 *          the invalidating to set
	 */
	public void setInvalidating(boolean invalidating)
	{
		this.invalidating = invalidating;
	}

	/**
	 * Indicates whether or not the client manager is expecting a disconnect. If this method returns
	 * true, then this client manager should be disposed of when the client disconnects; otherwise, it
	 * should be retained until the client comes back, or the client managers are cleaned up.
	 * 
	 * @return true if the client manager is expecting the client to disconnect, false otherwise
	 */
	public boolean isInvalidating()
	{
		return invalidating;
	}

	public void sendUpdateToClient(UpdateMessage<?> update)
	{
		System.out.println("SEND UPDATE MESSAGE PLEASE BLAAAAAHHHH!!!!!!!");
	}
	
	/**
	 * @return the address
	 */
	
	public String getSessionId()
	{
		return this.sessionId;
	}

	public Scope getScope()
	{
		return this.localScope;
	}

	/**
	 * Hook method for having shutdown behavior.
	 * 
	 * This method is called whenever the server is closing down the connection to this client.
	 */
	public void shutdown()
	{

	}
	
	public /*protected*/ RequestMessage translateOODSSRequestJSON(CharSequence messageCharSequence)
	throws SIMPLTranslationException
	{
		System.out.println(1.1);
		RequestMessage rm =  (RequestMessage) translationScope.deserializeCharSequence(messageCharSequence, FORMAT.JSON);
		System.out.println(1.2);
		return rm;//deserializeCharSequence(messageCharSequence);	
	}
}