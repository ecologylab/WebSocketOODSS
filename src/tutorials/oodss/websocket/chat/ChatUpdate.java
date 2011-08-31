package tutorials.oodss.websocket.chat;

import ecologylab.collections.Scope;
import ecologylab.oodss.distributed.server.clientsessionmanager.SessionHandle;
import ecologylab.oodss.messages.UpdateMessage;

/**
 * Implements a message that will be sent to chat clients to indicate that
 * another client has posted a message
 * 
 * @author bill
 */
public class ChatUpdate extends UpdateMessage
{
	@simpl_scalar
	private String	message;

	@simpl_scalar
	private String	id;


	/**
	 * Constructor used on client. Fields populated automatically by
	 * ecologylab.serialization
	 */
	public ChatUpdate()
	{
	}

	/**
	 * Constructor used on server
	 * 
	 * @param message
	 *           the chat message
	 * 
	 * @param handle
	 *           handle of originating client
	 */
	public ChatUpdate(String message, String id)
	{
		this.message = message;
		this.id = id;
		//this.host = handle.getInetAddress().toString();
		//this.port = handle.getPortNumber();
	}

	/**
	 * Called automatically by OODSS on client
	 */
	@Override
	public void processUpdate(Scope appObjScope)
	{
		System.out.println("processUpdate has indeed been called we kind of win!!!");
		/* get the chat listener */
		ChatUpdateListener listener = (ChatUpdateListener) appObjScope
				.get(ChatUpdateListener.CHAT_UPDATE_LISTENER);

		/* report incoming update */
		if (listener != null)
		{
			listener.recievedUpdate(this);
		}
		else
		{
			warning("Listener not set in application scope. Can't display message from\n"
					+ id + ": " + message);
		}
	}

	public String getMessage()
	{
		return message;
	}

	public String getId()
	{
		return id;
	}

}
