/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.net.sock;

import com.xahico.boot.dev.Helper;
import com.xahico.boot.lang.jsox.JSOXVariant;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.Charset;
import static java.nio.charset.StandardCharsets.UTF_8;
import javax.net.ssl.SSLContext;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public class TCPSocket extends TCPSocketConnectionBase {
	private static final SocketConnector DEFAULT_CONNECTOR = (socket, hostname, timeout) -> socket.connect(hostname, timeout);
	
	
	
	public static TCPSocket wrap (final Socket socket) throws IOException {
		final TCPSocket instance;
		
		instance = new TCPSocket(socket);
		instance.openChannels(socket);
		
		return instance;
	}
	
	
	
	private SSLContext   sslContext = null;
	private InputStream  in = null;
	private OutputStream out = null;
	
	
	
	public TCPSocket (){
		super();
		
		this.setConnector(DEFAULT_CONNECTOR);
	}
	
	private TCPSocket (final Socket socket){
		super(socket);
		
		this.setConnector(DEFAULT_CONNECTOR);
	}
	
	
	
	@Override
	protected Socket createSocket () throws IOException {
		if (this.isSecure()) {
			return this.sslContext.getSocketFactory().createSocket();
		} else {
			return super.createSocket();
		}
	}
	
	public SSLContext getSSLContext (){
		return this.sslContext;
	}
	
	public boolean isSecure (){
		return (null != this.sslContext);
	}
	
	@Override
	protected void openChannels (final Socket socket) throws IOException {
		this.in = socket.getInputStream();
		this.out = socket.getOutputStream();
	}
	
	public final int receive () throws IOException {
		return in.read();
	}
	
	public final char receiveChar () throws IOException {
		final int b;
		
		b = this.receive();
		
		if (b == -1) 
			throw new IOException();
		
		return ((char) b);
	}
	
	@Helper
	public final byte[] receive (final byte[] buffer) throws IOException {
		return this.receive(buffer, 0, buffer.length);
	}
	
	@Helper
	public final byte[] receive (final byte[] buffer, final int count) throws IOException {
		return this.receive(buffer, 0, count);
	}
	
	public byte[] receive (final byte[] buffer, final int offset, final int count) throws IOException {
		in.read(buffer, offset, count);
		
		return buffer;
	}
	
	public JSOXVariant receiveObjectUTF8 () throws IOException {
		final byte[] dataBytes;
		final int    dataSize;
		
		dataSize = Integer.parseInt(this.receiveStringUTF8());
		
		dataBytes = this.receive(new byte[dataSize]);
		
		return new JSOXVariant(new String(dataBytes, UTF_8));
	}
	
	public String receiveStringUTF8 () throws IOException {
		final StringBuilder buffer;
		
		buffer = new StringBuilder();
		
		for (;;) {
			final char c;
			
			c = this.receiveChar();
			
			if (c == '\n') {
				break;
			} else {
				buffer.append(c);
			}
		}
		
		return buffer.toString();
	}
	
	@Helper
	public final void send (final byte[] packet) throws IOException {
		this.send(packet, 0, packet.length);
	}
	
	public final void send (final SocketTransferable obj) throws IOException {
		this.send(obj.pack());
	}
	
	public void send (final byte[] buffer, final int offset, final int count) throws IOException {
		out.write(buffer, offset, count);
		out.flush();
	}
	
	public void sendObjectUTF8 (final JSOXVariant object) throws IOException {
		final byte[] data;
		
		data = object.toJSONStringCompact().getBytes(UTF_8);
		
		this.sendStringUTF8(Integer.toString(data.length));
		this.send(data);
	}
	
	public void sendString (final String string, final Charset charset) throws IOException {
		this.send(string.getBytes(charset));
		this.send("\n".getBytes(charset));
	}
	
	public void sendStringUTF8 (final String string) throws IOException {
		this.send(string.getBytes(UTF_8));
		this.send("\n".getBytes(UTF_8));
	}
	
	public void setSSLContext (final SSLContext sslContext){
		this.sslContext = sslContext;
	}
}