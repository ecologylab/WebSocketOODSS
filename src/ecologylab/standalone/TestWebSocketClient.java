package ecologylab.standalone;

import java.io.ByteArrayOutputStream;

import org.jwebsocket.api.WebSocketClientEvent;
import org.jwebsocket.api.WebSocketClientTokenListener;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.client.token.BaseTokenClient;
import org.jwebsocket.kit.WebSocketException;
import org.jwebsocket.token.BaseToken;
import org.jwebsocket.token.Token;

import ecologylab.oodss.messages.PingRequest;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.SimplTypesScope;
import ecologylab.serialization.formatenums.Format;
import ecologylab.serialization.formatenums.StringFormat;

public class TestWebSocketClient implements WebSocketClientTokenListener{

	BaseTokenClient client;
	TestWebSocketClient()
	{
		client = new BaseTokenClient();
		client.addListener(this);	
		
			try {
				client.open("ws://localhost:8787/;unid=admin_ui_1");
				System.out.println("Client now connected...");
			} catch (WebSocketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			try {
				
				PingRequest p = new PingRequest();
				
				ByteArrayOutputStream outStream = new ByteArrayOutputStream();
				try {
					//p.serialize(outStream, FORMAT.JSON);
					SimplTypesScope.serialize(p, outStream, Format.JSON);
					
				} catch (SIMPLTranslationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				String pJSON = new String(outStream.toByteArray());
				System.out.println("pJSON:"+pJSON);
				client.sendText("5",pJSON);
				//client.sendText("5", new String("{\"ns\":\"org.jWebSocket.plugins.system\",\"utid\":25,\"data\":\"LALAlaaaalaLAAL\",\"targetId\":\"*\",\"type\":\"send\",\"sourceId\":\"51262\"}"));
			} catch (WebSocketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

	
	public static void main(String args[])
	{
		TestWebSocketClient me = new TestWebSocketClient();
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void processOpened(WebSocketClientEvent aEvent) {
		// TODO Auto-generated method stub
		System.out.println("TestWebSocketClient:");
		
	}

	@Override
	public void processPacket(WebSocketClientEvent aEvent,
			WebSocketPacket aPacket) {
		// TODO Auto-generated method stub
		System.out.println("TestWebSocketClient:processPacket+"+aPacket.getUTF8());
		
		
	}

	@Override
	public void processClosed(WebSocketClientEvent aEvent) {
		// TODO Auto-generated method stub
		System.out.println("TestWebSocketClient:processClosed");
		
	}

	@Override
	public void processToken(WebSocketClientEvent aEvent, Token aToken) {
		// TODO Auto-generated method stub
		System.out.println("TestWebSocketClient:processToken");
		////aToken.
	}
	
}
