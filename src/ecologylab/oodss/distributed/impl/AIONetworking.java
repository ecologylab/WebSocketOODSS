/*
 * Created on Mar 2, 2007
 */
package ecologylab.oodss.distributed.impl;

import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import javax.naming.OperationNotSupportedException;

import ecologylab.collections.Scope;
import ecologylab.generic.Debug;
import ecologylab.io.ByteBufferPool;
import ecologylab.oodss.exceptions.BadClientException;
import ecologylab.oodss.exceptions.ClientOfflineException;
import ecologylab.oodss.messages.DefaultServicesTranslations;
import ecologylab.serialization.TranslationScope;

/**
 * Handles backend, low-level communication between distributed programs, using NIO. This is the
 * basis for servers for handling network communication.
 * 
 * @author Zachary O. Toups (toupsz@cs.tamu.edu)
 */
public abstract class AIONetworking<S extends Scope> extends Debug //extends AIOCore
{
	/**
	 * ByteBuffer that holds all incoming communication temporarily, immediately after it is read.
	 */

	/**
	 * Maps SocketChannels (connections) to their write Queues of ByteBuffers. Whenever a
	 * SocketChannel is marked for writing, and comes up for writing, the server will write the set of
	 * ByteBuffers to the socket.
	 */
	private Map<SelectionKey, Queue<ByteBuffer>>	pendingWrites		= new HashMap<SelectionKey, Queue<ByteBuffer>>();

	protected boolean															shuttingDown		= false;

	/**
	 * Space that defines mappings between xml names, and Java class names, for request messages.
	 */
	protected TranslationScope										translationScope;

	/** Provides a context for request processing. */
	protected S																		objectRegistry;

	protected int																	connectionCount	= 0;

	protected ByteBufferPool											byteBufferPool;


	/**
	 * Queue up bytes to send on a particular socket. This method is typically called by some outside
	 * context manager, that has produced an encoded message to send out.
	 * 
	 * @param socketKey
	 * @param data
	 */
	public void enqueueBytesForWriting(SelectionKey socketKey, ByteBuffer data)
	{
		// queue data to write
		synchronized (this.pendingWrites)
		{
			Queue<ByteBuffer> dataQueue = pendingWrites.get(socketKey);

			if (dataQueue == null)
			{
				dataQueue = new LinkedList<ByteBuffer>();
				pendingWrites.put(socketKey, dataQueue);
			}

			dataQueue.offer(data);
		}

	//	this.queueForWrite(socketKey);

	}

 

	/**
	 * Writes the bytes from pendingWrites that belong to key.
	 * 
	 * @param key
	 * @throws IOException
	 */
	protected void writeKey(SelectionKey key) throws IOException
	{
		SocketChannel sc = (SocketChannel) key.channel();

		synchronized (this.pendingWrites)
		{
			Queue<ByteBuffer> writes = pendingWrites.get(key);

			while (!writes.isEmpty())
			{ // write everything
				ByteBuffer bytes = writes.poll();

				bytes.flip();
				

				while (bytes.remaining() > 0)
				{ // the socket's buffer filled up!; should go out again next time
					//debug("unable to write all data to client; will try again shortly.");
					sc.write(bytes);
				}
				
				bytes = this.byteBufferPool.release(bytes);
			}
		}
	}

	/**
	 * Optional operation.
	 * 
	 * Called when a key has been marked for accepting. This method should be implemented by servers,
	 * but clients should leave this blank, unless they are also acting as servers (accepting incoming
	 * connections).
	 * 
	 * @param key
	 * @throws OperationNotSupportedException
	 */
	protected abstract void acceptKey(SelectionKey key);

	/**
	 * Remove the argument passed in from the set of connections we know about.
	 */
	protected void connectionTerminated()
	{
		connectionCount--;
		// When thread close by unexpected way (such as client just crashes),
		// this method will end the service gracefully.
		terminationAction();
	}

	/**
	 * This method is called whenever bytes have been read from a socket. There is no guaranty that
	 * the bytes will be a valid or complete message, nor is there a guaranty about what said bytes
	 * encode. Implementations should be prepared to handle incomplete messages, multiple messages, or
	 * malformed messages in this method.
	 * 
	 * @param sessionToken
	 *          the id being use for this session.
	 * @param sc
	 *          the SocketChannel from which the bytes originated.
	 * @param bytes
	 *          the bytes read from the SocketChannel.
	 * @param bytesRead
	 *          the number of bytes in the bytes array.
	 * @throws BadClientException
	 *           if the client from which the bytes were read has transmitted something inappropriate,
	 *           such as data too large for a buffer or a possibly malicious message.
	 */
	protected abstract void processReadData(Object sessionToken, SelectionKey sk, ByteBuffer bytes,
			int bytesRead) throws BadClientException;

	/**
	 * This defines the actions that server needs to perform when the client ends unexpected way.
	 * Detail implementations will be in subclasses.
	 */
	protected void terminationAction()
	{

	}

	/**
	 * Retrieves a ByteBuffer object from this's pool of ByteBuffers. Typically used by a
	 * ContextManager to store bytes that will be later enqueued to write (and thus released by that
	 * method).
	 * 
	 * @return
	 */
	public ByteBuffer acquireByteBufferFromPool()
	{
		return this.byteBufferPool.acquire();
	}
}
