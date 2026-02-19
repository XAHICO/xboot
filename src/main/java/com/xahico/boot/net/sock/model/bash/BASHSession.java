/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.net.sock.model.bash;

import com.xahico.boot.io.IOByteBuffer;
import com.xahico.boot.reflection.Reflection;
import com.xahico.boot.reflection.ReflectionField;
import com.xahico.boot.io.IOSocketChannel;
import com.xahico.boot.logging.Logger;
import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.channels.SocketChannel;
import static java.nio.charset.StandardCharsets.UTF_8;
import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.Executor;
import com.xahico.boot.net.NetSession;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.List;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public abstract class BASHSession extends NetSession {
	private static final ReflectionField bufferField;
	
	
	
	static {
		try {
			final Reflection<BASHSession> reflection;
			
			reflection = Reflection.of(BASHSession.class);
			
			bufferField = reflection.getField("buffer");
		} catch (final IllegalAccessException | NoSuchFieldException ex) {
			throw new InternalError(ex);
		}
	}
	
	
	
	protected final IOByteBuffer    buffer = null;
	private IOSocketChannel         channel = null;
	private boolean                 disconnected = false;
	private Executor                executor = null;
	private Logger                  logger = null;
	private IOByteBuffer            medium = null;
	private final List<BASHMessage> messageQueue = Collections.synchronizedList(new LinkedList<BASHMessage>());
	
	
	
	protected BASHSession (){
		super();
	}
	
	
	
	@Override
	public final void disconnect (){
		this.markDisconnected();
	}
	
	final SocketChannel channel (){
		return this.channel.wrappedChannel();
	}
	
	final void destroy (){
		this.channel.close();
	}
	
	private void dispatch (final BASHMessage message) throws IOException {
		try (final var stream = message.stream()) {
			this.channel.write(stream, this.medium);
		} finally {
			message.fireCallback();
		}
	}
	
	final void dispatchAll () throws IOException {
		for (;;) {
			final BASHMessage message;
			
			synchronized (this.messageQueue) {
				if (this.messageQueue.isEmpty()) {
					break;
				}
				
				message = this.messageQueue.get(0);
				
				if (null != message) {
					this.messageQueue.remove(0);
				}
			}
			
			if (null == message) {
				break;
			}
			
			this.dispatch(message);
		}
	}
	
	public final Logger getLogger (){
		return this.logger;
	}
	
	public final InetAddress getRemoteAddress (){
		try {
			return ((InetSocketAddress)this.channel.wrappedChannel().getRemoteAddress()).getAddress();
		} catch (final IOException ex) {
			throw new Error(ex);
		}
	}
	
	final void initialize (final Executor executor, final SocketChannel channel){
		this.executor = executor;
		this.channel = IOSocketChannel.wrap(channel);
	}
	
	final void initializeBuffers (final IOByteBuffer buffer){
		bufferField.set(this, buffer);
		
		this.buffer.charset(UTF_8);
		this.buffer.order(ByteOrder.BIG_ENDIAN);
		
		this.medium = new IOByteBuffer(buffer.capacity());
	}
	
	final void initializeLogger (final Logger logger){
		this.logger = logger;
	}
	
	@Override
	public final boolean isDisconnected (){
		return this.disconnected;
	}
	
	final void listen () throws IOException {
		final int bytesRead;
		
		bytesRead = this.buffer.read(this.channel);
		
		if (bytesRead > 0) {
			try {
				this.buffer.rewind();
				
				this.onMessage();
			} finally {
				this.buffer.head();
			}
		}
	}
	
	final void markDisconnected (){
		this.disconnected = true;
	}
	
	@Override
	protected void onConnect (){
		
	}
	
	@Override
	protected void onCreate (){
		
	}
	
	@Override
	protected void onDestroy (){
		
	}
	
	@Override
	protected void onDisconnect (){
		
	}
	
	protected abstract void onMessage () throws IOException;
	
	public final void post (final BASHMessage message){
		this.messageQueue.add(message);
	}
}