package ecologylab.standalone;

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
import ecologylab.oodss.server.clientsessionmanager.WebSocketSessionManager;
//import ecologylab.oodss.server.clientsessionmanager.NewClientSessionManager;
//import ecologylab.oodss.server.clientsessionmanager.NewTCPClientSessionManager;
import ecologylab.serialization.SimplTypesScope;

public class NewExtendedServer extends OODSSWebSocketServer<Scope> {

	Timer timer;
	public NewExtendedServer(int portNumber,
			InetAddress[] allInetAddressesForLocalhost,
			SimplTypesScope requestTranslationScope,
			Scope applicationObjectScope, int idleConnectionTimeout,
			int maxPacketSize) throws BindException, IOException {
		// TODO Auto-generated constructor stub
		super(requestTranslationScope,applicationObjectScope,idleConnectionTimeout,maxPacketSize);

		timer = new Timer(100000, new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				
				
				HashMap<String, WebSocketSessionManager> allSessions = GetAllSessions();

				System.out.println("HEY THIS TIME JUST WENT OFFFF.... there are "+allSessions.size());
				Iterator contextIter = allSessions.values().iterator();

				// process all of the messages in the queues
				TestUpdateMessage testUpdateMessage = new TestUpdateMessage("Hey guys","friendly",501);
			    
				while (contextIter.hasNext())
				{
					WebSocketSessionManager clientSession = (WebSocketSessionManager) contextIter.next();
				    //clientSession.sendUpdateToClient(testUpdateMessage);
					System.out.println("Tag is:"+clientSession.getSessionId().toString());
					sendUpdateMessage(clientSession.getSessionId().toString(),new TestUpdateMessage("You complete me.","Sarcastic",9001));
				}
				
			    printNumberOfConnectedClients();
			}
		});
		timer.start();
	}

	public static NewExtendedServer getInstance(int portNumber,
			InetAddress[] allInetAddressesForLocalhost,
			SimplTypesScope requestTranslationScope,
			Scope applicationObjectScope, int idleConnectionTimeout,
			int maxPacketSize) throws BindException, IOException {
		// TODO Auto-generated method stub
		
		
		return new NewExtendedServer(portNumber,
				allInetAddressesForLocalhost,
				requestTranslationScope,
				applicationObjectScope,
				idleConnectionTimeout,
				maxPacketSize);
	}

	
}
