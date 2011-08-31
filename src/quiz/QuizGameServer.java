package quiz;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.Timer;

import ecologylab.collections.Scope;
import ecologylab.generic.HashMapArrayList;
import ecologylab.oodss.distributed.server.DoubleThreadedAIOServer;
import ecologylab.oodss.server.clientsessionmanager.WebSocketSessionManager;
//import ecologylab.oodss.server.clientsessionmanager.NewClientSessionManager;
//import ecologylab.oodss.server.clientsessionmanager.NewTCPClientSessionManager;
import ecologylab.serialization.TranslationScope;
import ecologylab.standalone.TestUpdateMessage;

public class QuizGameServer extends DoubleThreadedAIOServer<Scope> {

	public static ArrayList<Player> players = new ArrayList<Player>();
	//Small todo list:
	//parse file to make 
	
	//make a java client that gives a person a name via request message in a given session
//Done
	//make a players update message lets one know when someone entered the room and what their score and status is
	//make an update message for chat
	//make a game start update message
	//make a player vote message
	//make a next round update message
	//make an end of game message
	
	//public static void 
	
	Timer timer;
	public QuizGameServer(int portNumber,
			InetAddress[] allInetAddressesForLocalhost,
			TranslationScope requestTranslationScope,
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
	
	public void UpdatePlayers()
	{
		//((QuizGameServer)clientSessionScope.get("server")).send//.sendUpdateMessage(clientId, updateMessage);
		HashMap<String, WebSocketSessionManager> allSessions = GetAllSessions();
		Iterator contextIter = allSessions.values().iterator();
		// process all of the messages in the queues
		TestUpdateMessage testUpdateMessage = new TestUpdateMessage("Hey guys","friendly",501);
		while (contextIter.hasNext())
		{
			WebSocketSessionManager clientSession = (WebSocketSessionManager) contextIter.next();
			sendUpdateMessage(clientSession.getSessionId().toString(),new PlayersAndScoresUpdateMessage(players));
		}
		    
	}

	public static QuizGameServer getInstance(int portNumber,
			InetAddress[] allInetAddressesForLocalhost,
			TranslationScope requestTranslationScope,
			Scope applicationObjectScope, int idleConnectionTimeout,
			int maxPacketSize) throws BindException, IOException {
		// TODO Auto-generated method stub
		
		
		
		QuizGameServer temp = new QuizGameServer(portNumber,
				allInetAddressesForLocalhost,
				requestTranslationScope,
				applicationObjectScope,
				idleConnectionTimeout,
				maxPacketSize);
		applicationObjectScope.put("server", temp);
		return temp;
	}

	
}
