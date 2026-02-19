/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.net.sock.model.trax;

import com.xahico.boot.io.IOByteBuffer;
import com.xahico.boot.io.IOSocketChannel;
import com.xahico.boot.io.IOStringBuffer;
import com.xahico.boot.lang.jsox.JSOXObject;
import com.xahico.boot.lang.jsox.JSOXVariant;
import com.xahico.boot.logging.Logger;
import com.xahico.boot.reflection.MethodNotFoundException;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import com.xahico.boot.net.NetSession;
import com.xahico.boot.util.Exceptions;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public abstract class TRAXSession extends NetSession {
	static final Charset PROTO_CHARSET = StandardCharsets.UTF_8;
	static final char    TERMINATOR = '\n';
	
	
	
	private IOStringBuffer            bufferIn = null;
	private IOByteBuffer              bufferOut = null;
	private IOSocketChannel           channel = null;
	private boolean                   disconnected = false;
	private Executor                  executor = null;
	private Logger                    logger = null;
	private Set<TRAXMethod>           methods = null;
	private final Deque<TRAXResponse> queue = new LinkedList<>();
	
	
	
	protected TRAXSession (){
		super();
	}
	
	
	
	final SocketChannel channel (){
		return this.channel.wrappedChannel();
	}
	
	final void destroy (){
		this.channel.close();
	}
	
	@Override
	public final void disconnect (){
		this.markDisconnected();
	}
	
	private void dispatch (final TRAXResponse message) throws IOException {
		final String body;
		final int    bodyLength;
		final String head;
		
		body = message.toVariant().toString();
		bodyLength = body.length();
		
		head = (Integer.toString(bodyLength) + TERMINATOR);
		
		this.channel.write(head.getBytes(PROTO_CHARSET), this.bufferOut);
		this.channel.write(body.getBytes(PROTO_CHARSET), this.bufferOut);
	}
	
	final void dispatchAll () throws IOException {
		for (;;) {
			final TRAXResponse message;
			
			message = this.queue.pollFirst();
			
			if (null == message) {
				break;
			}
			
			this.dispatch(message);
		}
	}
	
	public final Logger getLogger (){
		return this.logger;
	}
	
	private TRAXMethod getMethod (final String control) throws MethodNotFoundException {
		for (final var method : this.methods) {
			if (method.getControl().equalsIgnoreCase(control)) {
				return method;
			}
		}
		
		throw new MethodNotFoundException(String.format("No method found for '%s'", control));
	}
	
	protected TRAXResponse handleError (final Throwable throwable){
		return null;
	}
	
	final void initialize (final Executor executor, final SocketChannel channel, final Set<TRAXMethod> methods){
		this.executor = executor;
		this.channel = IOSocketChannel.wrap(channel);
		this.methods = methods;
	}
	
	final void initializeBuffers (final int bufferSize){
		this.bufferIn = new IOStringBuffer(bufferSize).charset(PROTO_CHARSET);
		this.bufferOut = new IOByteBuffer(bufferSize).charset(PROTO_CHARSET);
	}
	
	final void initializeLogger (final Logger logger){
		this.logger = logger;
	}
	
	@Override
	public final boolean isDisconnected (){
		return this.disconnected;
	}
	
	final void listen () throws IOException {
		final int    delimiter;
		final String body;
		final int    bodySize;
		final String head;
		final int    headSize;
		final int    packetSize;
		
		this.channel.read(this.bufferIn);
		
		delimiter = this.bufferIn.find(TERMINATOR);
		
		if (delimiter > -1) try {
			head = this.bufferIn.substring(0, delimiter);
			headSize = head.length();
			bodySize = Integer.parseInt(head);
			packetSize = (headSize + 1 + bodySize);
			
			if (this.bufferIn.length() >= packetSize) try {
				body = this.bufferIn.substring((delimiter + 1), packetSize);
				
				this.process(new JSOXVariant(body));
			} finally {
				this.bufferIn.discard(packetSize);
			}
		} catch (final NumberFormatException ex) {
			Exceptions.ignore(ex);
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
	
	private void process (final JSOXVariant requestJSOX) throws IOException {
		try (final var logEntry = this.getLogger().newEntry()) {
			logEntry.writeLine("CLASS: %s (%s)".formatted(this.getClass(), TRAXSession.class));
			logEntry.writeLine("SOURCE: %s".formatted(this.channel.wrappedChannel().getRemoteAddress().toString()));
			logEntry.writeLine("TYPE: exchange");
			
			try {
				final String       control;
				final TRAXMethod   method;
				final TRAXRequest  request;
				final TRAXResponse response;
				
				control = requestJSOX.getString("control");

				method = this.getMethod(control);

				request = (TRAXRequest) JSOXObject.newInstanceOf(method.getRequestClass(), requestJSOX);

				response = (TRAXResponse) JSOXObject.newInstanceOf(method.getResponseClass());

				method.invoke(this, request, response);

				logEntry.writeLine("STATUS: success");
				logEntry.write("REQUEST:");
				logEntry.writeLine(request.toVariant().toJSONStringCompact());
				logEntry.write("RESPONSE:");
				logEntry.writeLine(response.toVariant().toJSONStringCompact());

				this.queue.addLast(response);
			} catch (final ExecutionException ex) {
				final TRAXResponse response;
				
				response = handleError(ex.getCause());
				
				logEntry.writeLine("STATUS: failure (%s)".formatted(this, ex.getCause()));
				logEntry.write("REQUEST:");
				logEntry.writeLine(requestJSOX.toJSONStringCompact());
				logEntry.write("RESPONSE:");
				logEntry.writeLine((null != response) ? response.toVariant().toJSONStringCompact() : "<null>");
				
				this.queue.addLast(response);
			} catch (final MethodNotFoundException ex) {
				final TRAXResponse response;

				response = handleError(ex);
				
				logEntry.writeLine("STATUS: failure (%s)".formatted(this, ex));
				logEntry.write("REQUEST:");
				logEntry.writeLine(requestJSOX.toString());
				logEntry.write("RESPONSE:");
				logEntry.writeLine((null != response) ? response.toVariant().toJSONStringCompact() : "<null>");
				
				this.queue.addLast(response);
			}
		}
	}
}