/**
 * 
 */
package ecologylab.oodss.server.clientsessionmanager;

import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import ecologylab.collections.Scope;
import ecologylab.generic.StringTools;
import ecologylab.oodss.distributed.common.ServerConstants;
import ecologylab.oodss.distributed.common.SessionObjects;
import ecologylab.oodss.distributed.impl.MessageWithMetadata;
import ecologylab.oodss.distributed.impl.MessageWithMetadataPool;
import ecologylab.oodss.distributed.server.clientsessionmanager.SessionHandle;
import ecologylab.oodss.exceptions.BadClientException;
import ecologylab.oodss.messages.RequestMessage;
import ecologylab.oodss.messages.ResponseMessage;
import ecologylab.oodss.messages.UpdateMessage;
import ecologylab.serialization.ElementState.FORMAT;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.TranslationScope;


/**
 * The base class for all ContextManagers, objects that track the state and respond to clients on a
 * server. There is a one-to-one correspondence between connected clients and ContextManager
 * instances.
 * 
 * AbstractContextManager handles all encoding and decoding of messages, as well as translating
 * them. Hook methods provide places where subclasses may modify behavior for specific purposes.
 * 
 * Typical usage is to have the context manager's request queue be filled by a network thread, while
 * it is emptied by a working thread.
 * 
 * The normal cycle for filling the queue is to call acquireIncomingSequenceBuf() to clear and get
 * the incomingCharBuffer, then fill it externally (normally passing it as an argument to a
 * CharsetDecoder.decode call), then calling processIncomingSequenceBufToQueue() to release it and
 * let the ContextManager store the characters, converting messages into objects as they become
 * available.
 * 
 * For a complete, basic implementation (which is suitable for most uses), see
 * {@link ecologylab.oodss.distributed.server.clientsessionmanager.ClientSessionManager
 * ContextManager}.
 * 
 * @author Zachary O. Toups (zach@ecologylab.net)
 * 
 */
public abstract class NewTCPClientSessionManager<S extends Scope> extends NewBaseSessionManager<S>
		implements ServerConstants
{

	/**
	 * Stores the key-value pairings from a parsed HTTP-like header on an incoming message.
	 */

	
	protected final HashMap<String, String>															headerMap									= new HashMap<String, String>();

	protected int																												startReadIndex						= 0;

	/** Stores outgoing header character data. *///
	protected final StringBuilder																				headerBufOutgoing					= new StringBuilder(
																																																		MAX_HTTP_HEADER_LENGTH);

	protected final StringBuilder																				startLine									= new StringBuilder(
																																																		MAX_HTTP_HEADER_LENGTH);

	/**
	 * The network communicator that will handle all the reading and writing for the socket associated
	 * with this ContextManager
	 */
	//protected AIOServerIOThread																					server;

	/**
	 * The maximum message length allowed for clients that connect to this session manager. Note that
	 * most of the buffers used by AbstractClientManager are mutable in size, and will dynamically
	 * reallocate as necessary if they were initialized to be too small.
	 */
	protected int																												maxMessageSize;

	/** Used to translate incoming message XML strings into RequestMessages. */
	protected TranslationScope																					translationScope;

	/**
	 * stores the sequence of characters read from the header of an incoming message, may need to
	 * persist across read calls, as the entire header may not be sent at once.
	 */
	private final StringBuilder																					currentHeaderSequence			= new StringBuilder();

	/**
	 * stores the sequence of characters read from the header of an incoming message and identified as
	 * being a key for a header entry; may need to persist across read calls.
	 */
	private final StringBuilder																					currentKeyHeaderSequence	= new StringBuilder();

	/**
	 * Tracks the number of bad transmissions from the client; used for determining if a client is
	 * bad.
	 */
	private int																													badTransmissionCount;

	private int																													endOfFirstHeader					= -1;

	/**
	 * Counts how many characters still need to be extracted from the incomingMessageBuffer before
	 * they can be turned into a message (based upon the HTTP header). A value of -1 means that there
	 * is not yet a complete header, so no length has been determined (yet).
	 */
	private int																													contentLengthRemaining		= -1;

	/**
	 * Specifies whether or not the current message uses compression.
	 */
	private String																											contentEncoding						= "identity";

	/**
	 * Set of encoding schemes that the client supports
	 */
	private Set<String>																									availableEncodings				= new HashSet<String>();

	/**
	 * Stores the first XML message from the incomingMessageBuffer, or parts of it (if it is being
	 * read over several invocations).
	 */
	private StringBuilder																								persistentMessageBuffer		= null;

	private long																												contentUid								= -1;

	private Inflater																										inflater									= new Inflater();

	private Deflater																										deflater									= new Deflater();

	/**
	 * A queue of the requests to be performed by this ContextManager. Subclasses may override
	 * functionality and not use requestQueue.
	 */
	protected final Queue<MessageWithMetadata<RequestMessage, Object>>	requestQueue							= new LinkedBlockingQueue<MessageWithMetadata<RequestMessage, Object>>();

	protected final MessageWithMetadataPool<RequestMessage, Object>			reqPool										= new MessageWithMetadataPool<RequestMessage, Object>(
																																																		2,
																																																		4);

	protected CharsetDecoder																						decoder										= CHARSET
																																																		.newDecoder();

	protected CharsetEncoder																						encoder										= CHARSET
																																																		.newEncoder();

	private static final String																					POST_PREFIX								= "POST ";

	private static final String																					GET_PREFIX								= "GET ";

	/**
	 * Creates a new ContextManager.
	 * 
	 * @param sessionId
	 * @param clientSessionScope
	 *          TODO
	 * @param maxMessageSizeIn
	 * @param server2
	 * @param frontend
	 * @param socket
	 * @param translationScope
	 * @param registry
	 */
	public NewTCPClientSessionManager(String sessionId, int maxMessageSizeIn,/* AIOServerIOThread server2
			AIOServerProcessor frontend,*/ SelectionKey socket, TranslationScope translationScope,
			Scope<?> baseScope)
	{
		super(sessionId, /*frontend,*/ socket, baseScope);

	//	this.server = server2;
		this.translationScope = translationScope;

		// set up session id
		this.sessionId = sessionId;

		this.maxMessageSize = maxMessageSizeIn;

		//this.handle = new SessionHandle(this);
		this.localScope.put(SessionObjects.SESSION_HANDLE, this.handle);

		this.prepareBuffers(headerBufOutgoing);
	}


	/**
	 * Calls processRequest(RequestMessage) on each queued message as they are acquired through
	 * getNextRequest() and finishing when isMessageWaiting() returns false.
	 * 
	 * The functionality of processAllMessagesAndSendResponses() may be overridden by overridding the
	 * following methods: isMessageWaiting(), processRequest(RequestMessage), getNextRequest().
	 * 
	 * @throws BadClientException
	 */
	public final void processAllMessagesAndSendResponses() throws BadClientException
	{
		while (isMessageWaiting())
		{
			this.processNextMessageAndSendResponse();
		}
	}

	/**
	 * Sets the SelectionKey, and sets the new SelectionKey to have the same attachment (session id)
	 * as the old one.
	 * 
	 * @param socket
	 *          the socket to set
	 */
	public void setSocket(SelectionKey socket)
	{
		String sessionId = (String) this.socketKey.attachment();

		this.socketKey = socket;

		this.socketKey.attach(sessionId);
	}

	protected abstract void clearOutgoingMessageBuffer(StringBuilder outgoingMessageBuf);

	protected abstract void clearOutgoingMessageHeaderBuffer(StringBuilder outgoingMessageHeaderBuf);

	protected abstract void createHeader(int messageSize, StringBuilder outgoingMessageHeaderBuf,
			RequestMessage incomingRequest, ResponseMessage outgoingResponse, long uid);

	protected abstract void makeUpdateHeader(int messageSize, StringBuilder headerBufOutgoing,
			UpdateMessage<?> update);

	/**
	 * Parses the header of an incoming set of characters (i.e. a message from a client to a server),
	 * loading all of the HTTP-like headers into the given headerMap.
	 * 
	 * If headerMap is null, this method will throw a null pointer exception.
	 * 
	 * @param allIncomingChars
	 *          - the characters read from an incoming stream.
	 * @param headerMap
	 *          - the map into which all of the parsed headers will be placed.
	 * @return the length of the parsed header, or -1 if it was not yet found.
	 */
	protected int parseHeader(int startChar, StringBuilder allIncomingChars)
	{
		// indicates that we might be at the end of the header
		boolean maybeEndSequence = false;

		// true if the start line has been found, or if a key has been found
		// instead
		boolean noMoreStartLine = false;

		char currentChar;

		synchronized (currentHeaderSequence)
		{
			StringTools.clear(currentHeaderSequence);
			StringTools.clear(currentKeyHeaderSequence);

			int length = allIncomingChars.length();

			for (int i = 0; i < length; i++)
			{
				currentChar = allIncomingChars.charAt(i);

				switch (currentChar)
				{
				case (':'):
					/*
					 * we have the end of a key; move the currentHeaderSequence into the
					 * currentKeyHeaderSequence and clear it
					 */
					currentKeyHeaderSequence.append(currentHeaderSequence);

					StringTools.clear(currentHeaderSequence);

					noMoreStartLine = true;

					break;
				case ('\r'):
					/*
					 * we have the end of a line; if there's a CRLF, then we have the end of the value
					 * sequence or the end of the header.
					 */
					if (allIncomingChars.charAt(i + 1) == '\n')
					{
						if (!maybeEndSequence)
						{
							if (noMoreStartLine)
							{ // load the key/value pair
								headerMap.put(currentKeyHeaderSequence.toString().toLowerCase(),
										currentHeaderSequence.toString().trim());
							}
							else
							{ // we potentially have data w/o a key-value pair; this
								// is the start-line of an HTTP header
								StringTools.clear(startLine);
								this.startLine.append(currentHeaderSequence);

								noMoreStartLine = true;
							}

							StringTools.clear(currentKeyHeaderSequence);
							StringTools.clear(currentHeaderSequence);

							i++; // so we don't re-read that last character
						}
						else
						{ // end of the header
							return i + 2;
						}

						maybeEndSequence = true;
					}
					break;
				default:
					currentHeaderSequence.append(currentChar);
					maybeEndSequence = false;
					break;
				}
			}

			// if we got here, we didn't finish the header
			return -1;
		}
	}

	protected abstract void prepareBuffers(StringBuilder outgoingMessageHeaderBuf);

	protected abstract void translateResponseMessageToStringBufferContents(
			RequestMessage requestMessage, ResponseMessage responseMessage, StringBuilder messageBuffer)
			throws SIMPLTranslationException;

	/**
	 * Translates the given XML String into a RequestMessage object.
	 * 
	 * translateStringToRequestMessage(String) may be overridden to provide specific functionality,
	 * such as a ContextManager that does not use XML Strings.
	 * 
	 * @param messageCharSequence
	 *          - an XML String representing a RequestMessage object.
	 * @return the RequestMessage created by translating messageString into an object.
	 * @throws SIMPLTranslationException
	 *           if an error occurs when translating from XML into a RequestMessage.
	 * @throws UnsupportedEncodingException
	 *           if the String is not encoded properly.
	 */
	protected RequestMessage translateStringToRequestMessage(CharSequence messageCharSequence)
			throws SIMPLTranslationException, UnsupportedEncodingException
	{
		String startLineString = null;
		
//		debug("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
//		debug(this.startLine);
		
		if (this.startLine == null || (startLineString = startLine.toString()).equals(""))
		{ // normal case
			return translateOODSSRequest(messageCharSequence, startLineString);
		}
		else if (startLineString.startsWith(GET_PREFIX))
		{ // get case
//			debug("GET case!");
			return this.translateGetRequest(messageCharSequence, startLineString);
		}
		else if (startLineString.startsWith(POST_PREFIX))
		{ // post case
			return translatePostRequest(messageCharSequence, startLineString);
		}
		else
		{ // made of fail case
			return translateOtherRequest(messageCharSequence, startLineString);
		}
	}

	/**
	 * Translates an incoming character sequence identified to be an OODSS request message (not a GET
	 * or POST request).
	 * 
	 * @param messageCharSequence
	 * @param startLineString TODO
	 * @return The request message contained in the message.
	 * @throws SIMPLTranslationException
	 */
	protected RequestMessage translateOODSSRequest(CharSequence messageCharSequence, String startLineString)
			throws SIMPLTranslationException
	{
		return (RequestMessage) translationScope.deserializeCharSequence(messageCharSequence);	
	}

	
	public /*protected*/ RequestMessage translateOODSSRequestJSON(CharSequence messageCharSequence)
	throws SIMPLTranslationException
	{
		System.out.println(1.1);
		RequestMessage rm =  (RequestMessage) translationScope.deserializeCharSequence(messageCharSequence, FORMAT.JSON);
		System.out.println(1.2);
		return rm;//deserializeCharSequence(messageCharSequence);	
	}
	
	/**
	 * Translates an incoming character sequence identified to be a GET request.
	 * 
	 * This implementation returns null.
	 * 
	 * @param messageCharSequence
	 * @param startLineString TODO
	 * @return null.
	 */
	protected RequestMessage translateGetRequest(CharSequence messageCharSequence, String startLineString)
			throws SIMPLTranslationException
	{
		return null;
	}

	/**
	 * Translates an incoming character sequence identified to be a POST request.
	 * 
	 * This implementation expects the POST request to contain a nested OODSS request.
	 * 
	 * @param messageCharSequence
	 * @param startLineString TODO
	 * @return
	 * @throws SIMPLTranslationException
	 */
	protected RequestMessage translatePostRequest(CharSequence messageCharSequence, String startLineString)
			throws SIMPLTranslationException
	{
		String messageString = messageCharSequence.toString();

		if (!messageString.startsWith("<"))
			messageString = messageString.substring(messageString.indexOf('=') + 1);

		return this.translateOODSSRequest(messageString, startLineString);
	}

	/**
	 * Translates an incoming character sequence that cannot be identified. Called when the first line
	 * of the request is not empty, not GET, and not POST.
	 * 
	 * This implementation returns null.
	 * @param startLineString TODO
	 * 
	 * @return null.
	 */
	protected RequestMessage translateOtherRequest(CharSequence messageCharSequence, String startLineString)
			throws SIMPLTranslationException
	{
		return (RequestMessage) null;
	}

	/**
	 * Adds the given request to this's request queue.
	 * 
	 * enqueueRequest(RequestMessage) is a hook method for ContextManagers that need to implement
	 * other functionality, such as prioritizing messages.
	 * 
	 * If enqueueRequest(RequestMessage) is overridden, the following methods should also be
	 * overridden: isMessageWaiting(), getNextRequest().
	 * 
	 * @param request
	 */
	protected void enqueueRequest(MessageWithMetadata<RequestMessage, Object> request)
	{
		messageWaiting = this.requestQueue.offer(request);
	}

	/**
	 * Returns the next message in the request queue.
	 * 
	 * getNextRequest() may be overridden to provide specific functionality, such as a priority queue.
	 * In this case, it is important to override the following methods: isMessageWaiting(),
	 * enqueueRequest().
	 * 
	 * @return the next message in the requestQueue.
	 */
	protected MessageWithMetadata<RequestMessage, Object> getNextRequest()
	{
		synchronized (requestQueue)
		{
			int queueSize = requestQueue.size();

			if (queueSize == 1)
			{
				messageWaiting = false;
			}

			// return null if none left, or the next Request otherwise
			return requestQueue.poll();
		}
	}

	/**
	 * Calls processRequest(RequestMessage) on the result of getNextRequest().
	 * 
	 * In order to override functionality processRequest(RequestMessage) and/or getNextRequest()
	 * should be overridden.
	 * 
	 */
	private final void processNextMessageAndSendResponse()
	{
		this.processRequest(this.getNextRequest());
	}

	/**
	 * Calls performService(requestMessage), then converts the resulting ResponseMessage into a
	 * String, adds the HTTP-like headers, and passes the completed String to the server backend for
	 * sending to the client.
	 * 
	 * @param request
	 *          - the request message to process.
	 */
	protected final ResponseMessage processRequest(
			MessageWithMetadata<RequestMessage, Object> requestWithMetadata)
	{
		RequestMessage request = requestWithMetadata.getMessage();

		ResponseMessage response = super.processRequest(request,
				((SocketChannel) this.socketKey.channel()).socket().getInetAddress());

		if (response != null)
		{ // if the response is null, then we do
			// nothing else
			//sendResponseToClient(requestWithMetadata, response, request);
			System.out.println("HEEURRRRRRRRRRR I SHLOUD HAVE USED sendResponseToClient but did not hurrr!!!");
		}
		else
		{
			debug("context manager did not produce a response message.");
		}

		requestWithMetadata = reqPool.release(requestWithMetadata);

		return response;
	}
	
	public final ResponseMessage processRequest(RequestMessage request)
	{
		//RequestMessage request = requestWithMetadata.getMessage();

		ResponseMessage response = super.processRequest(request);//,((SocketChannel) this.socketKey.channel()).socket().getInetAddress());

		/*if (response != null)
		{ // if the response is null, then we do
			// nothing else
			sendResponseToClient(requestWithMetadata, response, request);
		}
		else
		{
			debug("context manager did not produce a response message.");
		}

		requestWithMetadata = reqPool.release(requestWithMetadata);
        */
		return response;
	}


	public synchronized void sendUpdateToClient(UpdateMessage<?> update)
	{
		//inherent to 
		
	}
	
	

	private void compress(StringBuilder src, ByteBuffer dest) throws DataFormatException
	{
		

	
	}

	
	public final void /*not void later */ processStringJSON(CharSequence incomingMessage, long incomingUid)
	{
		Exception failReason = null;
		RequestMessage request = null;
		try
		{
			request = this.translateOODSSRequestJSON(incomingMessage);//this.translateStringToRequestMessage(incomingMessage);
	        
		}
		catch (SIMPLTranslationException e)
		{
			// drop down to request == null, below
			failReason = e;
		}

		if (request == null)
		{
			if (incomingMessage.length() > 100)
			{
				debug("ERROR; incoming message could not be translated: " + incomingMessage.toString());

				debug("HEADERS:");
				debug(headerMap.toString());

				if (failReason != null)
				{
					debug("EXCEPTION: " + failReason.getMessage());
					failReason.printStackTrace();
				}
			}
			else
			{
				debug("ERROR; incoming message could not be translated: " + incomingMessage.toString());

				debug("HEADERS:");
				debug(headerMap.toString());

				if (failReason != null)
				{
					debug("EXCEPTION: " + failReason.getMessage());
					failReason.printStackTrace();
				}
			}
			// else
		}
		else
		{
			/* pretty sure this is an error case.
			badTransmissionCount = 0;

			MessageWithMetadata<RequestMessage, Object> pReq = this.reqPool.acquire();

			pReq.setMessage(request);
			pReq.setUid(incomingUid);

			synchronized (requestQueue)
			{
				this.enqueueRequest(pReq);
			}
			*/
			System.out.println("!!!!Null response here.... :(");
		}

	}
	/**
	 * Takes an incoming message in the form of an XML String and converts it into a RequestMessage
	 * using translateStringToRequestMessage(String). Then places the RequestMessage on the
	 * requestQueue using enqueueRequest().
	 * 
	 * @param incomingMessage
	 * @param headerMap2
	 * @throws BadClientException
	 */
	private final void processString(CharSequence incomingMessage, long incomingUid)
			throws BadClientException
	{
		Exception failReason = null;
		RequestMessage request = null;
		try
		{
			request = this.translateStringToRequestMessage(incomingMessage);
	        
		}
		catch (SIMPLTranslationException e)
		{
			// drop down to request == null, below
			failReason = e;
		}
		catch (UnsupportedEncodingException e)
		{
			// drop down to request == null, below
			failReason = e;
		}

		if (request == null)
		{
			if (incomingMessage.length() > 100)
			{
				debug("ERROR; incoming message could not be translated: " + incomingMessage.toString());

				debug("HEADERS:");
				debug(headerMap.toString());

				if (failReason != null)
				{
					debug("EXCEPTION: " + failReason.getMessage());
					failReason.printStackTrace();
				}
			}
			else
			{
				debug("ERROR; incoming message could not be translated: " + incomingMessage.toString());

				debug("HEADERS:");
				debug(headerMap.toString());

				if (failReason != null)
				{
					debug("EXCEPTION: " + failReason.getMessage());
					failReason.printStackTrace();
				}
			}
			if (++badTransmissionCount >= MAXIMUM_TRANSMISSION_ERRORS)
			{
				throw new BadClientException(((SocketChannel) this.socketKey.channel()).socket()
						.getInetAddress().getHostAddress(), "Too many Bad Transmissions: "
						+ badTransmissionCount);
			}
			// else
			error("translation failed: badTransmissionCount=" + badTransmissionCount);
		}
		else
		{
			badTransmissionCount = 0;

			MessageWithMetadata<RequestMessage, Object> pReq = this.reqPool.acquire();

			pReq.setMessage(request);
			pReq.setUid(incomingUid);

			synchronized (requestQueue)
			{
				this.enqueueRequest(pReq);
			}
		}
	}

	public InetSocketAddress getAddress()
	{
		return (InetSocketAddress) ((SocketChannel) getSocketKey().channel()).socket()
				.getRemoteSocketAddress();

	}

	public SessionHandle getHandle()
	{
		return handle;
	}
}