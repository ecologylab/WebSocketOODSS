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
import ecologylab.oodss.distributed.server.DoubleThreadedAIOServer;
import ecologylab.oodss.server.clientsessionmanager.NewClientSessionManager;
import ecologylab.oodss.server.clientsessionmanager.NewTCPClientSessionManager;
import ecologylab.serialization.TranslationScope;

public class NewExtendedServer extends DoubleThreadedAIOServer<Scope> {

	Timer timer;
	public NewExtendedServer(int portNumber,
			InetAddress[] allInetAddressesForLocalhost,
			TranslationScope requestTranslationScope,
			Scope applicationObjectScope, int idleConnectionTimeout,
			int maxPacketSize) throws BindException, IOException {
		// TODO Auto-generated constructor stub
		super(portNumber, allInetAddressesForLocalhost,requestTranslationScope,applicationObjectScope,idleConnectionTimeout,maxPacketSize);

		timer = new Timer(10000, new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				
				
				HashMap<String, NewClientSessionManager> allSessions = GetAllSessions();

				System.out.println("HEY THIS TIME JUST WENT OFFFF.... there are "+allSessions.size());
				Iterator contextIter = allSessions.values().iterator();

				// process all of the messages in the queues
				TestUpdateMessage testUpdateMessage = new TestUpdateMessage("Hey guys","friendly",501);
			    
				while (contextIter.hasNext())
				{
					NewTCPClientSessionManager clientSession = (NewTCPClientSessionManager) contextIter.next();
				    //clientSession.sendUpdateToClient(testUpdateMessage);
					System.out.println("Tag is:"+clientSession.getHandle().getSessionId().toString());
					sendUpdateMessage(clientSession.getHandle().getSessionId().toString(),new TestUpdateMessage("You complete me.","Sarcastic",9001));
				}
				
			    printNumberOfConnectedClients();
			}
		});
		timer.start();
	}

	public static NewExtendedServer getInstance(int portNumber,
			InetAddress[] allInetAddressesForLocalhost,
			TranslationScope requestTranslationScope,
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
