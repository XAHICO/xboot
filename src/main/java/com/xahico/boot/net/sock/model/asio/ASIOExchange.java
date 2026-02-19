/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.net.sock.model.asio;

import com.xahico.boot.io.IOByteBuffer;
import com.xahico.boot.reflection.Reflection;
import com.xahico.boot.reflection.ReflectionField;
import com.xahico.boot.io.IOSocketChannel;
import com.xahico.boot.logging.Logger;
import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import com.xahico.boot.net.NetSession;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public abstract class ASIOExchange extends NetSession {
	private static final int DEFAULT_PING_INTERVAL = (1000 * 10);		// 10-seconds
	private static final int DEFAULT_PING_INTERVAL_TIMEOUT = (1000 * 30);	// 30-seconds
	
	private static final ReflectionField bufferField;
	
	
	
	static {
		try {
			final Reflection<ASIOExchange> reflection;
			
			reflection = Reflection.of(ASIOExchange.class);
			
			bufferField = reflection.getField("buffer");
		} catch (final IllegalAccessException | NoSuchFieldException ex) {
			throw new InternalError(ex);
		}
	}
	
	
	
	private boolean                 authenticationCompleted = false;
	private boolean                 authenticationRequested = false;
	private final IOByteBuffer      buffer = null;
	private IOSocketChannel         channel = null;
	private ASIOCryptor             cryptor = null;
	private boolean                 disconnected = false;
	private Logger                  logger = null;
	private IOByteBuffer            medium = null;
	private final List<ASIOMessage> messageQueue = Collections.synchronizedList(new LinkedList<>());
	private boolean                 pingActive = false;
	private long                    pingContactReceived = System.currentTimeMillis();
	private long                    pingContactSent = System.currentTimeMillis();
	private int                     pingInterval = DEFAULT_PING_INTERVAL;
	private int                     pingTimeout = DEFAULT_PING_INTERVAL_TIMEOUT;
	
	
	
	protected ASIOExchange (){
		super();
	}
	
	
	
	@Override
	public final void disconnect (){
		this.markDisconnected();
	}
	
	final SocketChannel channel (){
		return this.channel.wrappedChannel();
	}
	
	final boolean completeAuth (final ASIOSecurityProvider securityProvider, final ASIOOption[] options) throws IOException {
		final ASIOOption[]     acceptedOptions;
		final ASIOAuthResponse response;
		
		response = new ASIOAuthResponse();
		
		this.buffer.rewind();
		
		do {
			final long bytesRead;
			
			bytesRead = this.channel.read(this.buffer);
			
			if (bytesRead <= 0) {
				return false;
			}
			
			this.pingContactReceived = System.currentTimeMillis();
		} while (!response.decode(this.buffer.array()));
		
		if (null != securityProvider) {
			final byte[]       cipher;
			final IOByteBuffer cipherBuffer;
			final byte[]       cipherEncrypted;
			final byte[]       cryptorIV;
			final byte[]       cryptorKey;
			
			cipherEncrypted = response.cipherBytes;
			
			cipher = securityProvider.decrypt(cipherEncrypted);
			
			cipherBuffer = IOByteBuffer.wrap(ByteBuffer.wrap(cipher));
			cipherBuffer.rewind();
			
			cryptorIV = cipherBuffer.getBytes(ASIOCryptor.ALGORITHM_IV_LENGTH);
			cryptorKey = cipherBuffer.getBytes(ASIOCryptor.ALGORITHM_KEY_LENGTH);
			
			this.cryptor = new ASIOCryptor();
			this.cryptor.init(cryptorIV, cryptorKey);
		}
		
		this.buffer.compact();
		
		acceptedOptions = ASIOOption.splitOptions(response.options);
		
		this.authenticationCompleted = true;
		
		return ASIOOption.match(options, acceptedOptions);
	}
	
	void destroy (){
		this.channel.close();
	}
	
	final void dispatch  () throws IOException {
		synchronized (this.messageQueue) {
			if (! this.messageQueue.isEmpty()) {
				final ASIOMessage message;
				
				message = this.messageQueue.get(0);
				
				this.messageQueue.remove(0);
				
				this.dispatch(message);
			}
		}
	}
	
	private void dispatch (final ASIOMessage message) throws IOException {
		try (final var stream = message.stream()) {
			final byte[] packetBody;
			final byte[] packetHead;
			
			this.medium.clear();
			this.medium.head();
			this.medium.putInteger(message.size());
			this.medium.rewind();
			
			if (null != this.cryptor) {
				packetHead = this.cryptor.encrypt(this.medium.getBytes(Integer.BYTES));
			} else {
				packetHead = this.medium.getBytes(Integer.BYTES);
			}
			
			this.channel.write(packetHead, this.medium);
			
			if (message.size() > 0) {
				if (null != this.cryptor) {
					packetBody = this.cryptor.encrypt(stream.readAllBytes());
				} else {
					packetBody = stream.readAllBytes();
				}

				this.channel.write(packetBody, this.medium);
			}
		} finally {
			message.fireCallback();
		}
	}
	
	public final Logger getLogger (){
		return this.logger;
	}
	
	public final InetAddress getRemoteAddress (){
		try {
			return ((InetSocketAddress) this.channel.wrappedChannel().getRemoteAddress()).getAddress();
		} catch (final IOException ex) {
			throw new Error(ex);
		}
	}
	
	final void initialize (final SocketChannel channel){
		this.channel = IOSocketChannel.wrap(channel);
	}
	
	final void initializeBuffers (final IOByteBuffer buffer){
		bufferField.set(this, buffer);
		
		this.buffer.charset(StandardCharsets.UTF_8);
		this.buffer.order(ByteOrder.BIG_ENDIAN);
		
		this.medium = new IOByteBuffer(buffer.capacity());
		this.medium.charset(StandardCharsets.UTF_8);
		this.medium.order(ByteOrder.BIG_ENDIAN);
	}
	
	final void initializeLogger (final Logger logger){
		this.logger = logger;
	}
	
	public final boolean isAuthenticationCompleted (){
		return this.authenticationCompleted;
	}
	
	public final boolean isAuthenticationRequested (){
		return this.authenticationRequested;
	}
	
	@Override
	public final boolean isDisconnected (){
		return this.disconnected;
	}
	
	public final boolean isPingWaiting (){
		return this.pingActive;
	}
	
	public final boolean isSuspect (){
		return ((System.currentTimeMillis() - this.pingContactSent) > this.pingInterval);
	}
	
	public final boolean isTimedOut (){
		return ((System.currentTimeMillis() - this.pingContactReceived) > (this.pingInterval + this.pingTimeout));
	}
	
	final void listen () throws IOException {
		final int bytesAvailable;
		
		this.buffer.head();

		bytesAvailable = this.buffer.read(this.channel);
		
		if (bytesAvailable > 0) {
			this.buffer.rewind();
			
			do {
				final byte[] packetBody;
				final int    packetBodySize;
				final byte[] packetHead;
				final int    packetHeadSize;

				if (null != this.cryptor) {
					packetHeadSize = ASIOCryptor.ALGORITHM_BLOCK_LENGTH;
					
					if (this.buffer.remaining() < packetHeadSize) {
						break;
					}

					packetHead = this.cryptor.decrypt(this.buffer.getBytes(packetHeadSize));
				} else {
					packetHeadSize = Integer.BYTES;
					
					if (this.buffer.remaining() < packetHeadSize) {
						break;
					}
					
					packetHead = this.buffer.getBytes(packetHeadSize);
				}
				
				this.medium.clear();
				this.medium.putBytes(packetHead);
				this.medium.rewind();
				
				packetBodySize = this.medium.getInteger();
				
				if (packetBodySize == 0) {
					this.pingContactReceived = System.currentTimeMillis();
					
					this.pingActive = false;
					
					this.buffer.compact();
					
					continue;
				}
				
				if (null != this.cryptor) {
					final int packetBodySizePadded;
					
					packetBodySizePadded = ASIOUtilities.calculatePadding(packetBodySize);
					
					if (this.buffer.remaining() < packetBodySizePadded) {
						break;
					}
					
					packetBody = this.cryptor.decrypt(this.buffer.getBytes(packetBodySizePadded));
				} else {
					if (this.buffer.remaining() < packetBodySize) {
						break;
					}

					packetBody = this.buffer.getBytes(packetBodySize);
				}
				
				this.onMessage(packetBody, packetBodySize);

				this.buffer.compact();
			} while (this.buffer.remaining() > 0);
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
	
	protected abstract void onMessage (final byte[] buffer, final int available) throws IOException;
	
	final void ping (){
		final ASIOMessage message;
		
		if (! this.pingActive) {
			this.pingActive = true;
			
			message = ASIOMessage.wrapBytes(new byte[0]);
			
			/*
			message.setCallback(() -> {
				System.out.println("PING DELIVERED ");
				System.out.println("!".repeat(1000));
			});
			*/
			this.pingContactSent = System.currentTimeMillis();
			
			this.post(message);
		}
	}
	
	public final void post (final ASIOMessage message){
		messageQueue.add(message);
	}
	
	final void requestAuth (final ASIOSecurityProvider securityProvider, final ASIOOption[] options) throws IOException {
		final ASIOAuthRequest request;
		
		if (null != securityProvider) {
			final byte[] key;
			
			key = securityProvider.transferable();
			
			request = new ASIOAuthRequest();
			request.options = ASIOOption.mergeOptions(options);
			request.pingInterval = this.pingInterval;
			request.pingTimeout = this.pingTimeout;
			request.keyLength = key.length;
			request.keyBytes = key;

			this.channel.write(request.encode());

			this.authenticationRequested = true;
		} else {
			request = new ASIOAuthRequest();
			request.options = ASIOOption.mergeOptions(options);
			request.pingInterval = this.pingInterval;
			request.pingTimeout = this.pingTimeout;
			request.keyLength = 0;
			
			System.out.println("Sending AUTH %d byte(s)".formatted(request.encode().length));
			
			this.medium.clear();
			this.medium.head();
			
			this.channel.write(request.encode(), medium);

			this.authenticationRequested = true;
		}
		
		this.pingContactSent = System.currentTimeMillis();
	}
}