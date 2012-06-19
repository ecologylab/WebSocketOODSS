package tutorials.oodss.websocket.chat;

import java.util.HashMap;

import ecologylab.collections.Scope;
import ecologylab.oodss.distributed.common.SessionObjects;
import ecologylab.oodss.distributed.server.clientsessionmanager.SessionHandle;
import ecologylab.oodss.messages.OkResponse;
import ecologylab.oodss.messages.RequestMessage;
import ecologylab.oodss.messages.ResponseMessage;
import ecologylab.oodss.server.clientsessionmanager.WebSocketSessionManager;
import ecologylab.serialization.annotations.simpl_scalar;

/**
 * Implements a message that will be sent to PublicChatServer to indicate that
 * this client is posting a message to the chat service.
 * 
 * @author bill, modified by rhema
 */
public class ChatRequest extends RequestMessage
{
	/**
	 * The message being posted.
	 */
	@simpl_scalar
	String									message;

	/**
	 * Constructor used on server. Fields populated automatically by
	 * ecologylab.serialization
	 */
	public ChatRequest()
	{
	}

	/**
	 * Constructor used on client.
	 * 
	 * @param newEcho
	 *           a String that will be passed to the server to broadcast
	 * */
	public ChatRequest(String message)
	{
		this.message = message;
	}

	/**
	 * Called automatically by LSDCS on server
	 */
	@Override
	public ResponseMessage performService(Scope cSScope)
	{
		/*
		 * Get all of the handles for the sessions currently connected to the
		 * server
		 */
		HashMap<String, WebSocketSessionManager> sessionsMap = (HashMap<String, WebSocketSessionManager>)cSScope.get(SessionObjects.SESSIONS_MAP_BY_SESSION_ID);
		
		/*
		 * Get this client's sessionId
		 */
		String sessionId = (String) cSScope.get(SessionObjects.SESSION_ID);
		/*
		 * Form a update message to send out to all of the
		 * other clients.
		 */
		ChatUpdate messageUpdate = new ChatUpdate(message, sessionId);

		/*
		 * Loop through the other sessions.
		 */
		for (WebSocketSessionManager otherClient : sessionsMap.values())
		{
			/*
			 * If this isn't me then send them an update.
			 */
			System.out.println(otherClient.getSessionId() + " checking to make sure not "+sessionId);
			if (!otherClient.getSessionId().equals(sessionId))
			{
			    otherClient.sendUpdateToClient(messageUpdate,otherClient.getSessionId());
			}
		}

		/*
		 * Send back a response confirming that we got the request 
		 */
		return OkResponse.reusableInstance;
	}

	public boolean isDisposable()
	{
		return true;
	}
}
