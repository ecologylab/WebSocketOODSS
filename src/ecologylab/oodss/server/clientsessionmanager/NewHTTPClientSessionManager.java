package ecologylab.oodss.server.clientsessionmanager;

import java.nio.channels.SelectionKey;

import ecologylab.collections.Scope;
import ecologylab.net.ParsedURL;
import ecologylab.oodss.distributed.impl.AIOServerIOThread;
import ecologylab.oodss.distributed.impl.NIOServerIOThread;
import ecologylab.oodss.distributed.server.AIOServerProcessor;
import ecologylab.oodss.messages.RequestMessage;
import ecologylab.oodss.messages.ResponseMessage;
import ecologylab.oodss.messages.UpdateMessage;
import ecologylab.serialization.TranslationScope;

public abstract class NewHTTPClientSessionManager<S extends Scope> extends NewTCPClientSessionManager<S>
{

	static final String	HTTP_VERSION							= "HTTP/1.1";

	static final String	HTTP_RESPONSE_HEADERS			= HTTP_VERSION + " 303 See Other" + "\r\n";

	static final String	HTTP_APPEND								= " " + HTTP_VERSION;

	static final int		HTTP_APPEND_LENGTH				= HTTP_APPEND.length();

	static final String	HTTP_CONTENT_TYPE					= "Content-Type: text/plain; charset=US-ASCII\r\n";

	protected boolean		ALLOW_HTTP_STYLE_REQUESTS	= true;

	public NewHTTPClientSessionManager(String sessionId, int maxPacketSize, AIOServerIOThread server,
			AIOServerProcessor frontend, SelectionKey socket, TranslationScope translationScope,
			Scope<?> registry)
	{
		super(sessionId, maxPacketSize, server, frontend, socket, translationScope, registry);
	}

	/**
	 * @see ecologylab.oodss.distributed.server.clientsessionmanager.ClientSessionManager#clearOutgoingMessageHeaderBuffer(java.lang.StringBuilder)
	 */
	@Override
	protected void clearOutgoingMessageHeaderBuffer(StringBuilder outgoingMessageHeaderBuf)
	{
		outgoingMessageHeaderBuf.delete(0, outgoingMessageHeaderBuf.length());
	}

	/**
	 * @see ecologylab.oodss.distributed.server.clientsessionmanager.ClientSessionManager#createHeader(java.lang.StringBuilder,
	 *      java.lang.StringBuilder, RequestMessage, ResponseMessage)
	 */
	@Override
	protected void createHeader(int messageSize, StringBuilder outgoingMessageHeaderBuf,
			RequestMessage incomingRequest, ResponseMessage outgoingResponse, long uid)
	{
		boolean isOK = outgoingResponse.isOK();
		ParsedURL responseUrl = isOK ? incomingRequest.okRedirectUrl(localScope) : incomingRequest
				.errorRedirectUrl(localScope);

		if (responseUrl != null)
		{
			debugA("responseUrl: " + responseUrl);

			if (responseUrl != null)
			{
				outgoingMessageHeaderBuf.append(HTTP_RESPONSE_HEADERS
						+ HTTP_CONTENT_TYPE
						+ "Location: "
						+ responseUrl.toString());
			}

			debugA("Server sending response!!!\n" + outgoingMessageHeaderBuf.toString());
		}
		else
			warning("isOK=" + isOK + " but responseUrl=null. Can't send redirect response.");
	}

	protected void makeUpdateHeader(int messageSize, StringBuilder headerBufOutgoing,
			UpdateMessage<?> update)
	{

	}

	/**
	 * @see ecologylab.oodss.distributed.server.clientsessionmanager.TCPClientSessionManager#clearOutgoingMessageBuffer(java.lang.StringBuilder)
	 */
	@Override
	protected void clearOutgoingMessageBuffer(StringBuilder outgoingMessageBuf)
	{
	}

	/**
	 * @see ecologylab.oodss.distributed.server.clientsessionmanager.TCPClientSessionManager#prepareBuffers(java.lang.StringBuilder,
	 *      java.lang.StringBuilder, java.lang.StringBuilder)
	 */
	@Override
	protected void prepareBuffers(StringBuilder outgoingMessageHeaderBuf)
	{
	}

	/**
	 * @see ecologylab.oodss.distributed.server.clientsessionmanager.BaseSessionManager#isInitialized()
	 */
	@Override
	public boolean isInitialized()
	{
		return true;
	}
}
