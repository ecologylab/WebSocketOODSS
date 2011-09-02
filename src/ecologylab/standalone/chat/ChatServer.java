package ecologylab.standalone.chat;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.Timer;

import ecologylab.collections.Scope;
import ecologylab.generic.HashMapArrayList;
import ecologylab.oodss.distributed.server.OODSSWebSocketServer;
import ecologylab.oodss.messages.DefaultServicesTranslations;
import ecologylab.oodss.server.clientsessionmanager.WebSocketSessionManager;
//import ecologylab.oodss.server.clientsessionmanager.NewClientSessionManager;
//import ecologylab.oodss.server.clientsessionmanager.NewTCPClientSessionManager;
import ecologylab.serialization.TranslationScope;

public class ChatServer extends OODSSWebSocketServer<Scope> {
	
	public static TranslationScope getTranslationScope()
	{
		TranslationScope ts = DefaultServicesTranslations.get();
		ts.addTranslation(SendChatMessage.class);
		ts.addTranslation(ChatUpdate.class);
		return ts;
	}
	

	Timer timer;
	public ChatServer(TranslationScope requestTranslationScope,
			Scope applicationObjectScope, int idleConnectionTimeout,
			int maxPacketSize) throws BindException, IOException {
		//super(portNumber, allInetAddressesForLocalhost,requestTranslationScope,applicationObjectScope,idleConnectionTimeout,maxPacketSize);
        super(requestTranslationScope,applicationObjectScope,idleConnectionTimeout,maxPacketSize);
		timer = new Timer(100000, new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				
				
				HashMap<String, WebSocketSessionManager> allSessions = GetAllSessions();

				System.out.println("HEY THIS TIME JUST WENT OFFFF.... there are "+allSessions.size());
				Iterator contextIter = allSessions.values().iterator();

				// process all of the messages in the queues
				ChatUpdate testUpdateMessage = new ChatUpdate("Hey guys","friendly",501);
			    
				while (contextIter.hasNext())
				{
					WebSocketSessionManager clientSession = (WebSocketSessionManager) contextIter.next();
				    //clientSession.sendUpdateToClient(testUpdateMessage);
					System.out.println("Tag is:"+clientSession.getSessionId().toString());
					sendUpdateMessage(clientSession.getSessionId().toString(),new ChatUpdate("You complete me.","Sarcastic",9001));
				}
				
			    printNumberOfConnectedClients();
			}
		});
		timer.start();
	}

	
	
}
