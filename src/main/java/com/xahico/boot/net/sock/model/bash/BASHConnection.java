/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.net.sock.model.bash;

import com.xahico.boot.dev.Helper;
import com.xahico.boot.net.SocketChannelFactory;
import com.xahico.boot.net.inet.InetEndpoint;
import com.xahico.boot.reflection.ClassFactory;
import com.xahico.boot.util.Exceptions;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class BASHConnection implements AutoCloseable {
	private static final int                          DEFAULT_RECONNECT_INTERVAL = 10000;
	
	private static final ClassFactory<BASHConnection> CLASS_FACTORY = ClassFactory.getClassFactory(BASHConnection.class);
	
	
	
	public static BASHConnection newConnection (){
		return CLASS_FACTORY.newInstance();
	}
	
	
	
	private BASHCallback               callbackOnComplete = (__) -> {};
	private BASHCallback               callbackOnConnect = (__) -> {};
	private BASHCallback               callbackOnDisconnect = (__) -> {};
	private BASHCallback               callbackOnMessage = (__) -> {};
	
	private volatile boolean           abortCalled = false;
	private boolean                    autoReconnect = false;
	private int                        autoReconnectInterval = DEFAULT_RECONNECT_INTERVAL;
	private volatile boolean           closed = false;
	private boolean                    closeUponCompletion = false;
	private volatile boolean           completed = false;
	private volatile boolean           connected = false;
	private long                       connectScheduled = -1;
	private BASHConnectionErrorHandler errorHandler = null;
	private final BASHExchange         exchange = new BASHExchange(this);
	private Executor                   executor = null;
	private InetSocketAddress          hostname = null;
	private Mode                       mode = null;
	private final Deque<Future<Void>>  scheduledQueue = new LinkedList<>();
	private Selector                   selector = null;
	private SocketChannel              socket = null;
	private SocketChannelFactory       socketFactory = () -> SocketChannel.open();
	
	
	
	public BASHConnection (){
		super();
	}
	
	
	
	public void abort (){
		this.abortCalled = true;
	}
	
	public void call (final Runnable routine){
		if (! this.abortCalled) {
			this.executor.execute(routine);
		}
	}
	
	private void cancelStack (){
		try {
			for (final var future : this.scheduledQueue) {
				future.cancel(true);
			}
		} finally {
			this.scheduledQueue.clear();
		}
	}
	
	@Override
	public void close (){
		try {
			this.call(() -> this.handleClose());
		} finally {
			this.closed = true;
		}
	}
	
	protected Executor createDefaultExecutor (){
		return Executors.newSingleThreadExecutor();
	}
	
	private void ensureReady (){
		if (null == this.mode) {
			throw new Error("mode not set");
		}
		
		if (null == this.hostname) {
			throw new Error("host not set");
		}
	}
	
	public InetSocketAddress getHost (){
		return this.hostname;
	}
	
	public Mode getMode (){
		return this.mode;
	}
	
	private void handleClose (){
		try {
			this.cancelStack();
			
			exchange.reset();
			
			if (this.isConnected()) {
				this.handleDisconnect();
			}
			
			if (null != this.socket) {
				this.socket.close();
			}
		} catch (final IOException ex) {
			Exceptions.ignore(ex);
		} finally {
			this.socket = null;
			
			this.closed = true;
		}
	}
	
	private void handleComplete (){
		this.completed = true;
		
		this.callbackOnComplete.call(exchange);
		
		if (this.closeUponCompletion) {
			this.handleClose();
		}
	}
	
	private void handleConnect (final boolean first){
		int     options = 0;
		boolean permitted = false;
		
		try {
			if (! first) {
				if (this.connectScheduled == -1) {
					this.connectScheduled = System.currentTimeMillis();
				}
				
				if ((System.currentTimeMillis() - this.connectScheduled) >= this.autoReconnectInterval) {
					this.connectScheduled = -1;

					permitted = true;
				}
			}
			
			if (first || permitted) {
				this.selector = Selector.open();

				this.socket = this.socketFactory.newInstance();
				this.socket.connect(this.hostname);
				this.socket.configureBlocking(false);
				
				if (this.isReadEnabled()) {
					options |= SelectionKey.OP_READ;
				}
				
				if (this.isWriteEnabled()) {
					options |= SelectionKey.OP_WRITE;
				}
				
				this.socket.register(this.selector, options);
				
				this.connected = true;
				
				this.callbackOnConnect.call(exchange);

				this.sweep(true);

				this.connectScheduled = -1;
			}
		} catch (final IOException ex) {
			this.call(() -> handleError(ex));
		}
		
		if (!this.connected && this.autoReconnect) {
			this.call(() -> handleConnect(false));
		}
	}
	
	private void handleDisconnect (){
		try {
			this.cancelStack();
			
			exchange.reset();
			
			this.callbackOnDisconnect.call(exchange);
		} finally {
			this.connected = false;
		}
	}
	
	private void handleError (final Throwable throwable){
		this.errorHandler.call(throwable);
	}
	
	public boolean isAbortCalled (){
		return this.abortCalled;
	}
	
	public boolean isClosed (){
		return this.closed;
	}
	
	public boolean isCompleted (){
		return this.completed;
	}
	
	public boolean isConnected (){
		return this.connected;
	}
	
	public boolean isReadEnabled (){
		switch (this.mode) {
			case FREE:
			case LISTEN: 
			case TRANSACT:
			case TRANSACT_REVERSED:
				return true;
			case TALK: 
				return false;
			default: {
				throw new InternalError("unknown mode '%s'".formatted(this.mode));
			}
		}
	}
	
	public boolean isWriteEnabled (){
		switch (this.mode) {
			case LISTEN: 
				return false;
			case FREE:
			case TALK: 
			case TRANSACT:
			case TRANSACT_REVERSED:
				return true;
			default: {
				throw new InternalError("unknown mode '%s'".formatted(this.mode));
			}
		}
	}
	
	public void onComplete (final BASHCallback callback){
		this.callbackOnComplete = callback;
	}
	
	public void onConnect (final BASHCallback callback){
		this.callbackOnConnect = callback;
	}
	
	public void onDisconnect (final BASHCallback callback){
		this.callbackOnDisconnect = callback;
	}
	
	public void onError (final BASHConnectionErrorHandler callback){
		this.errorHandler = callback;
	}
	
	public void onMessage (final BASHCallback callback){
		this.callbackOnMessage = callback;
	}
	
	public void open (){
		if (null == this.executor) {
			this.executor = createDefaultExecutor();
		}
		
		this.ensureReady();
		
		this.call(() -> this.handleConnect(true));
	}
	
	public void require (final BASHCallback callback){
		final AtomicBoolean done;
		final Future<Void>  future;
		final Object        mutex;
		
		mutex = new Object();
		
		done = new AtomicBoolean(false);
		
		future = new Future<>() {
			boolean cancelled = false;

			@Override
			public boolean cancel (final boolean mayInterruptIfRunning){
				this.cancelled = true;

				return true;
			}

			@Override
			public Void get () throws ExecutionException, InterruptedException {
				synchronized (mutex) {
					mutex.wait();
				}

				return null;
			}

			@Override
			public Void get (final long timeout, final TimeUnit unit) throws ExecutionException, InterruptedException, TimeoutException {
				synchronized (mutex) {
					mutex.wait(unit.toMillis(timeout));

					if (! done.get()) {
						throw new TimeoutException();
					}
				}

				return null;
			}

			@Override
			public boolean isCancelled (){
				return this.cancelled;
			}

			@Override
			public boolean isDone (){
				return done.get();
			}
		};
		
		this.executor.execute(() -> {
			try {
				if (this.isConnected() && !this.isClosed() && !future.isCancelled()) try {
					this.sweep(false);
					
					if (this.isConnected() && !this.isClosed()) {
						callback.call(exchange);
					}
				} finally {
					done.set(true);
				}
			} finally {
				scheduledQueue.remove(future);
			}
		});
		
		scheduledQueue.addLast(future);
	}
	
	public void setAutoReconnect (final boolean enabled){
		this.autoReconnect = enabled;
	}
	
	public void setAutoReconnectInterval (final int timeMillis){
		this.autoReconnectInterval = timeMillis;
	}
	
	public void setCloseUponCompletion (final boolean enabled){
		this.closeUponCompletion = enabled;
	}
	
	public void setExecutor (final Executor executor){
		this.executor = executor;
	}
	
	@Helper
	public void setHost (final InetEndpoint hostname){
		this.setHost(hostname.getAddressString(), hostname.getPort());
	}
	
	public void setHost (final InetSocketAddress hostname){
		this.hostname = hostname;
	}
	
	@Helper
	public void setHost (final String hostname){
		this.setHost(InetEndpoint.getByName(hostname));
	}
	
	@Helper
	public void setHost (final String address, final int port){
		this.setHost(new InetSocketAddress(address, port));
	}
	
	public void setMode (final Mode mode){
		this.mode = mode;
	}
	
	public void setSocketFactory (final SocketChannelFactory socketFactory){
		this.socketFactory = socketFactory;
	}
	
	private void sweep (final boolean recourse){
		switch (this.mode) {
			case TRANSACT: 
			case TRANSACT_REVERSED: {
				if (exchange.isReadComplete() && exchange.isWriteComplete()) {
					this.handleComplete();
				}
				
				break;
			}
		}
		
		if (! this.isCompleted()) try {
			final Iterator<SelectionKey> it;
			
			selector.selectNow();
			
			it = selector.selectedKeys().iterator();
			
			while (it.hasNext()) {
				final SelectionKey key;
				
				if (this.isClosed() || this.isAbortCalled()) {
					break;
				}
				
				key = it.next();
				
				it.remove();
				
				if (key.isReadable()) {
					exchange.bufferIn.read(this.socket);
				}
				
				if (key.isWritable() && (exchange.bufferOut.length() > 0)) {
					exchange.bufferOut.head();
					exchange.bufferOut.write(this.socket);
					exchange.bufferOut.compact();
				}
			}
			
			if (this.isConnected() && !this.isClosed() && !this.isAbortCalled()) {
				if ((exchange.available() > 0)) {
					exchange.bufferIn.rewind();

					this.callbackOnMessage.call(exchange);

					exchange.bufferIn.head();
				}
				
				if (recourse) {
					this.call(() -> this.sweep(true));
				}
			}
		} catch (final IOException ex) {
			if (this.connected) {
				handleError(ex);
				handleDisconnect();

				if (this.autoReconnect) {
					this.call(() -> handleConnect(false));
				}
			}
		}
	}
	
	
	
	public static enum Mode {
		/**
		 * Free-form. 
		 * 
		 * Read and write permitted without a predetermined state for 
		 * completion.
		**/
		FREE,
		
		/**
		 * Listens to the host without a predetermined state for completion.
		 * 
		 * Writing is disabled.
		**/
		LISTEN,
		
		/**
		 * Talks to the host without a predetermined state for completion.
		 * 
		 * Reading is disabled.
		**/
		TALK,
		
		/**
		 * Traditional transaction, where we request and the host responds.
		 * 
		 * A state of completion is achieved after both parties have sent 
		 * and received their respective communications.
		**/
		TRANSACT,
		
		/**
		 * Reversed variant of a {@link #TRANSACT transaction}; 
		 * the host requests and we respond. 
		 * 
		 * A state of completion is achieved after both parties have sent 
		 * and received their respective communications.
		**/
		TRANSACT_REVERSED,
	}
}