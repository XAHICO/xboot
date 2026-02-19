/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.net.sock.model.fts;

import com.xahico.boot.analytics.DataMass;
import com.xahico.boot.io.IOByteBuffer;
import com.xahico.boot.reflection.Reflection;
import com.xahico.boot.reflection.ReflectionField;
import com.xahico.boot.io.IOSocketChannel;
import com.xahico.boot.logging.Logger;
import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.channels.SocketChannel;
import static java.nio.charset.StandardCharsets.UTF_8;
import com.xahico.boot.net.NetSession;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public abstract class FTSSession extends NetSession {
	private static final int UPLOAD_KEY_LENGTH = UUID.randomUUID().toString().length();
	
	
	
	private static final ReflectionField securityProviderField;
	
	
	
	static {
		try {
			final Reflection<FTSSession> reflection;
			
			reflection = Reflection.of(FTSSession.class);
			
			securityProviderField = reflection.getField("securityProvider");
		} catch (final IllegalAccessException | NoSuchFieldException ex) {
			throw new InternalError(ex);
		}
	}
	
	
	
	private FTSUpload                 activeUpload = null;
	private String                    activeUploadKey = null;
	private boolean                   authenticateRecv = false;
	private boolean                   authenticateSent = false;
	protected IOByteBuffer            bufferIn = null;
	protected IOByteBuffer            bufferOut = null;
	private IOSocketChannel           channel = null;
	private final FTSCryptor          cryptor = new FTSCryptor();
	private boolean                   disconnected = false;
	private Logger                    logger = null;
	private final FTSSecurityProvider securityProvider = null;
	private List<FTSUpload>           uploadsShared = null;
	
	
	
	protected FTSSession (){
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
	
	final void initializeBuffers (final int size){
		this.bufferIn = new IOByteBuffer(size);
		this.bufferIn.charset(UTF_8);
		this.bufferIn.order(ByteOrder.BIG_ENDIAN);
		
		this.bufferOut = new IOByteBuffer(size);
		this.bufferOut.charset(UTF_8);
		this.bufferOut.order(ByteOrder.BIG_ENDIAN);
	}
	
	final void initializeChannel (final SocketChannel channel){
		this.channel = IOSocketChannel.wrap(channel);
	}
	
	final void initializeLogger (final Logger logger){
		this.logger = logger;
	}
	
	final void initializeSecurityProvider (final FTSSecurityProvider securityProvider){
		securityProviderField.set(this, securityProvider);
	}
	
	final void initializeUploadsShared (final List<FTSUpload> uploadsShared){
		this.uploadsShared = uploadsShared;
	}
	
	@Override
	public final boolean isDisconnected (){
		return this.disconnected;
	}
	
	final void listen () throws IOException {
		if (this.authenticateSent) {
			int bytesRead;
			
			this.bufferIn.head();

			bytesRead = this.bufferIn.read(this.channel);
			
			if (bytesRead > 0) {
				this.bufferIn.rewind();
				
				this.process();
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
		this.bufferIn.clear();
		this.bufferOut.clear();
		
		if (null != this.activeUpload) {
			this.activeUpload.markAbandoned(true);
			this.activeUpload.markAccepted(false);
			
			System.out.println("Client abandoned upload with %d bytes remaining (position: %d)".formatted(this.activeUpload.remaining(), this.activeUpload.position()));
		}
	}
	
	@Override
	protected void onDisconnect (){
		
	}
	
	final void process () throws IOException {
		try {
			this.bufferIn.rewind();
			
			if (! this.authenticateRecv) {
				if (this.bufferIn.remaining() >= FTSSecurityProvider.PACKET_SIZE) {
					final byte[]       cipher;
					final IOByteBuffer cipher_buffer;
					final byte[]       cipher_encrypted;
					final byte[]       crypt_iv;
					final byte[]       crypt_key;
					
					cipher_encrypted = this.bufferIn.getBytes(FTSSecurityProvider.PACKET_SIZE);
					
					cipher = this.securityProvider.decrypt(cipher_encrypted);
					
					if (null == cipher) {
						throw new IOException();
					}
					
					this.bufferIn.compact();
					
					cipher_buffer = IOByteBuffer.wrap(ByteBuffer.wrap(cipher));
					cipher_buffer.rewind();
					
					crypt_iv = cipher_buffer.getBytes(FTSCryptor.ALGORITHM_IV_LENGTH);
					crypt_key = cipher_buffer.getBytes(FTSCryptor.ALGORITHM_KEY_LENGTH);
					
					this.cryptor.init(crypt_iv, crypt_key);
					
					this.authenticateRecv = true;
					
					this.getLogger().log("[%s] Received CIPHER -- key exchange completed".formatted(this));
				}
			}
			
			if (this.authenticateRecv && (this.bufferIn.remaining() > 0)) {
				if (null == this.activeUpload) {
					if (this.bufferIn.remaining() >= (UPLOAD_KEY_LENGTH + Long.BYTES)) {
						this.activeUploadKey = new String(this.bufferIn.getBytes(UPLOAD_KEY_LENGTH), UTF_8);
						
						this.bufferIn.compact();
						
						this.getLogger().log("[%s] Has announced its intent to upload into '%s'".formatted(this, this.activeUploadKey));
						
						synchronized (this.uploadsShared) {
							final Iterator<FTSUpload> it;
							
							it = this.uploadsShared.iterator();
							
							while (it.hasNext()) {
								final FTSUpload upload;
								
								upload = it.next();
								
								if (upload.key().equals(this.activeUploadKey)) {
									this.activeUpload = upload;
									
									if (this.activeUpload.isAbandoned()) {
										final long uploadSize;
										
										this.activeUpload.markAbandoned(false);
										
										uploadSize = this.bufferIn.getLong();
										
										this.bufferIn.compact();
										
										if (uploadSize != upload.size()) {
											// RESET
										}
									}
									
									break;
								}
							}
						}
						
						if (null == this.activeUpload) {
							// Remember to discard the client requested upload size (8 bytes) from buffer
							this.bufferIn.discard(Long.BYTES);
						}
					}
				}
				
				if (null != this.activeUpload) {
					if (! this.activeUpload.isInitialized()) {
						if (this.bufferIn.remaining() >= Long.BYTES) {
							final long uploadSize;
							
							uploadSize = this.bufferIn.getLong();
							
							this.bufferIn.compact();
							
							this.activeUpload.initialize(uploadSize);
							
							this.getLogger().log("[%s] Is preparing to upload %s data into '%s'".formatted(this, new DataMass(this.activeUpload.remaining()), this.activeUpload.key()));
						}
					}
					
					if (this.activeUpload.isInitialized() && this.activeUpload.canWrite()) {
						while ((this.activeUpload.remaining() > 0)) {
							byte[] block;
							int    blockSize;
							
							this.bufferIn.rewind();
							
							if (this.bufferIn.remaining() < FTSCryptor.ALGORITHM_BLOCK_LENGTH) {
								break;
							}
							
							if (this.activeUpload.remaining() >= FTSCryptor.ALGORITHM_BLOCK_LENGTH) {
								blockSize = FTSCryptor.ALGORITHM_BLOCK_LENGTH;
							} else {
								blockSize = (int) this.activeUpload.remaining();
							}
							
							block = this.bufferIn.getBytes(FTSCryptor.ALGORITHM_BLOCK_LENGTH);
							
							this.bufferIn.compact();
							
							block = this.cryptor.decrypt(block);
							
							this.activeUpload.write(block, blockSize);
						}
						
						if (this.activeUpload.remaining() == 0) {
							this.getLogger().log("[%s] Has finished uploading %s (%d bytes exact) data into '%s'".formatted(this, new DataMass(this.activeUpload.size()), this.activeUpload.size(), this.activeUpload.key()));
							
							this.activeUploadKey = null;
							
							this.activeUpload.close();
							this.activeUpload.markCompleted();
							this.activeUpload = null;
							
							if (this.bufferIn.remaining() > 0) {
								this.process();
							}
						}
					}
				}
			}
		} finally {
			this.bufferIn.head();
		}
	}
	
	final void talk () throws IOException {
		if (! this.authenticateSent) {
			final byte[] key;
			
			key = this.securityProvider.transferable();
			
			this.bufferOut.clear();
			this.bufferOut.rewind();
			this.bufferOut.putInteger(key.length);
			this.bufferOut.putBytes(key);
			this.bufferOut.rewind();
			
			do {
				this.channel.write(this.bufferOut);
				
				this.bufferOut.compact();
			} while (this.bufferOut.remaining() > 0);
			
			this.authenticateSent = true;
			
			this.getLogger().log("[%s] Sent PUBLIC_KEY (%d bytes)".formatted(this, key.length));
		}
		
		if (this.authenticateRecv) {
			if ((null == this.activeUpload) && (null != this.activeUploadKey)) {
				this.bufferOut.clear();
				this.bufferOut.rewind();
				this.bufferOut.putLong(-1);
				
				this.bufferOut.rewind();
				
				do {
					this.channel.write(this.bufferOut);
					
					this.bufferOut.compact();
				} while (this.bufferOut.remaining() > 0);
					
				this.activeUploadKey = null;
			}
			
			if (null != this.activeUpload) {
				if (! this.activeUpload.isAccepted()) {
					this.bufferOut.clear();
					this.bufferOut.rewind();
					this.bufferOut.putLong(this.activeUpload.position());
					
					this.bufferOut.rewind();
					
					do {
						this.channel.write(this.bufferOut);

						this.bufferOut.compact();
					} while (this.bufferOut.remaining() > 0);
					
					this.activeUpload.markAccepted(true);
					
					if (! this.activeUpload.canWrite()) {
						this.activeUpload.open();
					}
					
					System.out.println("Sent position %d to client and opened stream for writing (response to '%s')".formatted(this.activeUpload.position(), this.activeUpload.key()));
				}
			}
		}
	}
}