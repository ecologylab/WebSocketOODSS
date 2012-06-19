package ecologylab.standalone.chat;

import java.util.HashMap;
import java.util.Iterator;

import ecologylab.collections.Scope;
import ecologylab.oodss.messages.OkResponse;
import ecologylab.oodss.messages.RequestMessage;
import ecologylab.oodss.messages.ResponseMessage;
import ecologylab.oodss.server.clientsessionmanager.WebSocketSessionManager;
import ecologylab.serialization.annotations.simpl_scalar;
import ecologylab.standalone.TestUpdateMessage;



public class SendChatMessage extends RequestMessage{
	
	public String getName() {
		return name;
	}



	public void setName(String name) {
		this.name = name;
	}



	public String getMessage() {
		return message;
	}



	public void setMessage(String message) {
		this.message = message;
	}


	public SendChatMessage()
	{
	}

	public SendChatMessage(String name, String message) {
		super();
		this.name = name;
		this.message = message;
	}

	@simpl_scalar String name;
	@simpl_scalar String message;
	
	@Override
	public ResponseMessage performService(Scope clientSessionScope) {
		
		System.out.println(name+" send the message:"+message);
		
		TestUpdateMessage testUpdateMessage = new TestUpdateMessage("Hey guys","friendly",501);
		Iterator contextIter = ChatServer.getInstance().GetAllSessions().values().iterator();
		while (contextIter.hasNext())
		{
			HashMap<String, WebSocketSessionManager> allSessions = ChatServer.getInstance().GetAllSessions();

			System.out.println("HEY THIS TIME JUST WENT OFFFF.... there are "+allSessions.size());
//			Iterator contextIter = allSessions.values().iterator();

			
			//HashMap<String, WebSocketSessionManager> allSessions = ChatServer.getInstance().GetAllSessions();

			contextIter = ChatServer.getInstance().GetAllSessions().values().iterator();
			
			WebSocketSessionManager clientSession = (WebSocketSessionManager) contextIter.next();
		    //clientSession.sendUpdateToClient(testUpdateMessage);
			System.out.println("Tag is:"+clientSession.getSessionId().toString());
			ChatServer.getInstance().sendUpdateMessage(clientSession.getSessionId().toString(),new ChatUpdate("You complete me.","Sarcastic",9001));
		}
		
		
		return new OkResponse();
	}

}
