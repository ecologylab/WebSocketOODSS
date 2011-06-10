/**
 * 
 */
package ecologylab.oodss.distributed.impl;

import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.nio.channels.SelectionKey;

import ecologylab.collections.Scope;
import ecologylab.net.NetTools;
import ecologylab.oodss.distributed.common.SessionObjects;
import ecologylab.oodss.distributed.server.AIOServerDataReader;
import ecologylab.oodss.distributed.server.clientsessionmanager.BaseSessionManager;
import ecologylab.oodss.messages.InitConnectionRequest;
import ecologylab.oodss.server.clientsessionmanager.NewClientSessionManager;
import ecologylab.serialization.TranslationScope;

/**
 * Provides access to an NIOServerIOThread, which handles the details of network connections.
 * Subclasses extend and provide functionality for actually processing messages.
 * 
 * @author Zachary O. Toups (zach@ecologylab.net)
 */
public abstract class AbstractAIOServer<S extends Scope> extends Manager implements
		AIOServerDataReader, Runnable, SessionObjects
{
	private AIOServerIOThread		backend;

	protected TranslationScope	translationScope;

	protected S									applicationObjectScope;

	/**
	 * Creates an instance of an NIOServer of some flavor. Creates the backend using the information
	 * in the arguments.
	 * 
	 * Registers itself as the MAIN_START_AND_STOPPABLE in the object registry.
	 * 
	 * @param portNumber
	 * @param inetAddress
	 * @param translationScope
	 * @param objectRegistry
	 * @throws IOException
	 * @throws BindException
	 */
	protected AbstractAIOServer(int portNumber, InetAddress[] inetAddress,
			TranslationScope requestTranslationSpace, S objectRegistry, int idleConnectionTimeout,
			int maxMessageLength) throws IOException, BindException
	{
		backend = this.generateBackend(portNumber, inetAddress, composeTranslations(portNumber,
				inetAddress[0], requestTranslationSpace), objectRegistry, idleConnectionTimeout,
				maxMessageLength);

		debug("setting up NIO Server...");

		// we get these from the backend, because it ensures that they are
		// configured if they are passed in null
		this.translationScope = backend.translationScope;
		this.applicationObjectScope = (S) backend.objectRegistry;

		this.applicationObjectScope.put(MAIN_START_AND_STOPPABLE, this);
		this.applicationObjectScope.put(MAIN_SHUTDOWNABLE, this);
	}

	static final Class[]	OUR_TRANSLATIONS	=
																					{ InitConnectionRequest.class, };

	public static TranslationScope composeTranslations(int portNumber, InetAddress inetAddress,
			TranslationScope requestTranslationSpace)
	{
		return composeTranslations(OUR_TRANSLATIONS, "nio_server_base: ", portNumber, inetAddress
				.toString(), requestTranslationSpace);
	}

	public static TranslationScope composeTranslations(Class[] newTranslations, String prefix,
			int portNumber, String inetAddress, TranslationScope requestTranslationSpace)
	{
		return TranslationScope.get(prefix + inetAddress.toString() + ":" + portNumber,
				requestTranslationSpace, newTranslations);
	}

	/**
	 * Creates an instance of an NIOServer of some flavor. Creates the backend using the information
	 * in the arguments.
	 * 
	 * Registers itself as the MAIN_START_AND_STOPPABLE in the object registry.
	 * 
	 * @param portNumber
	 * @param inetAddress
	 * @param translationScope
	 * @param objectRegistry
	 * @throws IOException
	 * @throws BindException
	 */
	protected AbstractAIOServer(int portNumber, InetAddress inetAddress,
			TranslationScope requestTranslationSpace, S objectRegistry, int idleConnectionTimeout,
			int maxMessageLength) throws IOException, BindException
	{
		this(portNumber, NetTools.wrapSingleAddress(inetAddress), requestTranslationSpace,
				objectRegistry, idleConnectionTimeout, maxMessageLength);
	}

	protected AIOServerIOThread generateBackend(int portNumber, InetAddress[] inetAddresses,
			TranslationScope requestTranslationSpace, S objectRegistry, int idleConnectionTimeout,
			int maxMessageLength) throws BindException, IOException
	{
		return AIOServerIOThread.getInstance(portNumber, inetAddresses, this, requestTranslationSpace,
				objectRegistry, idleConnectionTimeout, maxMessageLength);
	}

	protected abstract NewClientSessionManager generateContextManager(String sessionId, SelectionKey sk,
			TranslationScope translationScope, Scope globalScope);

	/**
	 * @see ecologylab.generic.StartAndStoppable#start()
	 */
	public void start()
	{
		try
		{
			backend.openSelector();
			backend.registerAcceptWithSelector();
			backend.start();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * @see ecologylab.generic.StartAndStoppable#stop()
	 */
	public void stop()
	{
		backend.stop();
	}

	/**
	 * @return the backend
	 */
	public AIOServerIOThread getBackend()
	{
		return backend;
	}

	/**
	 * @return the global scope for this server
	 */
	public Scope getGlobalScope()
	{
		return applicationObjectScope;
	}

	/**
	 * @return the translationScope
	 */
	public TranslationScope getTranslationSpace()
	{
		return translationScope;
	}
}
