/*
 * Created on May 3, 2006
 */
package ecologylab.oodss.distributed.impl;

import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

//import sun.misc.BASE64Encoder;
import ecologylab.collections.Scope;
import ecologylab.generic.ObjectOrHashMap;
import ecologylab.oodss.distributed.common.ServerConstants;
import ecologylab.oodss.distributed.server.AIOServerDataReader;
import ecologylab.oodss.exceptions.BadClientException;
import ecologylab.serialization.TranslationScope;

/**
 * The backend portion of the NIO Server, which handles low-level communication with clients.
 * 
 * Re-written based on the Rox Java NIO Tutorial
 * (http://rox-xmlrpc.sourceforge.net/niotut/index.html).
 * 
 * @author Zachary O. Toups (zach@ecologylab.net)
 * 
 */
public class AIOServerIOThread extends AIONetworking implements ServerConstants
{
	static AIOServerIOThread getInstance(int portNumber, InetAddress[] hostAddresses,
			AIOServerDataReader sAP, TranslationScope requestTranslationSpace, Scope<?> objectRegistry,
			int idleSocketTimeout, int maxMessageLength) throws IOException, BindException
	{
		return new AIOServerIOThread(portNumber, hostAddresses, sAP, requestTranslationSpace,
				objectRegistry, idleSocketTimeout, maxMessageLength);
	}

	private AIOServerDataReader																	sAP;

	private int																									idleSocketTimeout;

	private Map<SelectionKey, Long>															keyActivityTimes					= new HashMap<SelectionKey, Long>();

	private Map<String, ObjectOrHashMap<String, SelectionKey>>	ipToKeyOrKeys							= new HashMap<String, ObjectOrHashMap<String, SelectionKey>>();

	private boolean																							acceptEnabled							= false;

	private MessageDigest																				digester;

	private long																								dispensedTokens;

	private InetAddress[]																				hostAddresses;


	protected AIOServerIOThread(int portNumber, InetAddress[] hostAddresses, AIOServerDataReader sAP,
			TranslationScope requestTranslationSpace, Scope<?> objectRegistry, int idleSocketTimeout,
			int maxMessageLength) throws IOException, BindException
	{
		//super("NIOServer", portNumber, requestTranslationSpace, objectRegistry, maxMessageLength);

		this.construct(hostAddresses, sAP, idleSocketTimeout);
	}

	private void construct(InetAddress[] newHostAddresses, AIOServerDataReader newFrontend,
			int newIdleSocketTimeout) throws IOException
	{
		this.hostAddresses = newHostAddresses;

		this.sAP = newFrontend;

		this.idleSocketTimeout = newIdleSocketTimeout;

		try
		{
			digester = MessageDigest.getInstance("SHA-256");
		}
		catch (NoSuchAlgorithmException e)
		{
			weird("This can only happen if the local implementation does not include the given hash algorithm.");
			e.printStackTrace();
		}
	}

	/**
	 * Gets all host addresses associated with this server.
	 * 
	 * @return
	 */
	public InetAddress[] getHostAddresses()
	{
		return hostAddresses;
	}

	/**
	 * Checks all of the current keys to see if they have been idle for too long and drops them if
	 * they have.
	 * 
	 */
	/**
	 * Attempts to bind all of the ports in the hostAddresses array. If a port cannot be bound, it is
	 * removed from the hostAddresses array.
	 * 
	 * @throws IOException
	 */
	void registerAcceptWithSelector() throws IOException
	{
	//	boundAddresses.clear();
		//incomingConnectionSockets.clear();

		for (int i = 0; i < hostAddresses.length; i++)
		{
			debug("setting up accept on " + hostAddresses[i]);

			// acquire the static ServerSocketChannel object
			//ServerSocketChannel channel = ServerSocketChannel.open();

			// disable blocking
			//channel.configureBlocking(false);

			//try
			//{
				//ServerSocket newSocket = channel.socket();
				// get the socket associated with the channel

				// bind to the port for this server
				//newSocket.bind(new InetSocketAddress(hostAddresses[i], portNumber));

				//newSocket.setReuseAddress(true);

				//channel.register(this.selector, SelectionKey.OP_ACCEPT);

//				this.incomingConnectionSockets.add(newSocket);
				//this.boundAddresses.add(hostAddresses[i]);
			//}
			/*catch (BindException e)
			{
				debug("Unable to bind " + hostAddresses[i]);
				debug(e.getMessage());
				e.printStackTrace();
			}
			catch (SocketException e)
			{
				System.err.println(e.getMessage());
			}*/
		}

//		if (this.boundAddresses.size() == 0)
	//	{
	//		throw new BindException("Server was unable to bind to any addresses.");
		//}

		// register the channel with the selector to look for incoming
		// accept requests
		acceptEnabled = true;
	}

	/**
	 * Generates a unique identifier String for the given socket, based upon actual ports used and ip
	 * addresses with a hash. Called by the server at accept() time, and used to identify the
	 * connection thereafter.
	 * 
	 * @param incomingSocket
	 * @return
	 */
	protected String generateSessionToken(Socket incomingSocket)
	{
		// clear digester
		digester.reset();

		// we make a string consisting of the following:
		// time of initial connection (when this method is called),
		// client ip, client actual port
		digester.update(String.valueOf(System.currentTimeMillis()).getBytes());
		// digester.update(String.valueOf(System.nanoTime()).getBytes());
		// digester.update(this.incomingConnectionSockets[0].getInetAddress()
		// .toString().getBytes());
		digester.update(incomingSocket.getInetAddress().toString().getBytes());
		digester.update(String.valueOf(incomingSocket.getPort()).getBytes());

		digester.update(String.valueOf(this.dispensedTokens).getBytes());

		dispensedTokens++;

		// convert to normal characters and return as a String
		return new String(digester.digest());// new String((new BASE64Encoder()).encode(digester.digest()));
	}



	@Override
	protected void processReadData(Object sessionToken, SelectionKey sk, ByteBuffer bytes,
			int bytesRead) throws BadClientException
	{
		this.sAP.processRead(sessionToken, this, sk, bytes, bytesRead);
		this.keyActivityTimes.put(sk, System.currentTimeMillis());
	}

	/**
	 * @param socket
	 * @param permanent
	 */
	public void setPendingInvalidate(SocketChannel socket, boolean permanent)
	{
		//this.setPendingInvalidate(socket.keyFor(selector), permanent);
	}


	@Override
	protected void acceptKey(SelectionKey key) {
		// TODO Auto-generated method stub
		
	}

}
