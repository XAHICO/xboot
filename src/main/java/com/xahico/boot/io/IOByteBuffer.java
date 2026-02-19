/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.io;

import com.xahico.boot.lang.jsox.JSOXException;
import com.xahico.boot.lang.jsox.JSOXObject;
import com.xahico.boot.lang.jsox.JSOXUtilities;
import com.xahico.boot.lang.jsox.JSOXVariant;
import com.xahico.boot.util.StringUtilities;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import static java.nio.charset.StandardCharsets.UTF_16;
import static java.nio.charset.StandardCharsets.UTF_16BE;
import static java.nio.charset.StandardCharsets.UTF_8;
import java.util.Arrays;
import org.json.JSONObject;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public class IOByteBuffer implements IOBuffer {
	public static IOByteBuffer wrap (final ByteBuffer buffer){
		return new IOByteBuffer(buffer);
	}
	
	
	
	protected ByteBuffer     buffer;
	private Charset          charset = DEFAULT_CHARSET;
	private int              checkpoint = 0;
	private int              length = 0;
	private final ByteBuffer nativeBuffer = ByteBuffer.allocate(32);
	
	
	
	private IOByteBuffer (final ByteBuffer buffer){
		super();
		
		this.buffer = buffer;
		this.buffer.rewind();
		
		this.nativeBuffer.rewind();
	}
	
	public IOByteBuffer (final int initialSize){
		this(ByteBuffer.allocate(initialSize));
	}
	
	
	
	public final byte[] array (){
		return this.buffer.array();
	}
	
	public IOByteBuffer back (final int count){
		this.position(this.position() - count);
		
		return IOByteBuffer.this;
	}
	
	@Override
	public boolean canRead (){
		return true;
	}
	
	@Override
	public boolean canWrite (){
		return true;
	}
	
	public int capacity (){
		return this.buffer.capacity();
	}
	
	@Override
	public Charset charset (){
		return this.charset;
	}
	
	public IOByteBuffer charset (final Charset charset){
		this.charset = charset;
		
		return IOByteBuffer.this;
	}
	
	@Override
	public IOByteBuffer clear (){
		this.buffer.clear();
		this.nativeBuffer.clear();
		this.length = 0;
		
		return IOByteBuffer.this;
	}
	
	/**
	 * {@link #discard(int) discards} data up to the current position.
	 * 
	 * @return 
	 * TBD.
	**/
	public IOByteBuffer compact (){
		if (this.position() > 0) {
			this.discard(this.position());
		}
		
		return IOByteBuffer.this;
	}
	
	public final void cut (){
		this.length = this.position();
	}
	
	@Override
	public final IOByteBuffer discard (final int count){
		final byte[] copy;
		
//		copy = Arrays.copyOfRange(this.buffer.array(), this.position(), this.capacity());
		if ((this.length - count) > 0) 
			copy = Arrays.copyOfRange(this.buffer.array(), this.buffer.position(), this.length);
		else {
			copy = null;
		}
		
		this.buffer.clear();
		this.buffer.rewind();
		
		if (null != copy) {
			this.buffer.put(copy);
			this.buffer.rewind();
		}
		
		if ((this.length - count) > 0) 
			this.length -= count;
		else {
			this.length = 0;
		}
		
		return IOByteBuffer.this;
	}
	
	@Override
	public int find (final int b, final int fromIndex) throws IndexOutOfBoundsException {
		for (var i = fromIndex; i < this.remaining(); i++) {
			if (this.buffer.array()[i] == b) {
				return i;
			}
		}
		
		return -1;
	}
	
	public int getByte (){
		return this.buffer.get();
	}
	
	public byte[] getBytes (final int count){
		final byte[] array;

		array = new byte[count];

		for (var i = 0; i < array.length; i++) {
			array[i] = (byte) this.getByte();
		}

		return array;
	}
	
	@Override
	public final char getChar (){
		return this.getChar(charset);
	}
	
	@Override
	public final char getChar (final Charset charset){
		if (charset == UTF_8) {
			return (char) this.getByte();
		} else if ((charset == UTF_16) || (charset == UTF_16BE)) {
			return this.buffer.getChar();
		} else {
			throw new UnsupportedOperationException("not supported yet: char conversion to %s not implemented".formatted(charset));
		}
	}
	
	@Override
	public final double getDouble (){
		return this.buffer.getDouble();
	}
	
	@Override
	public final int getInteger (){
		return this.buffer.getInt();
	}
	
	@Override
	public final long getLong (){
		return this.buffer.getLong();
	}
	
	public final JSOXVariant getObject () throws JSOXException {
		return JSOXUtilities.decodeObject(this);
	}
	
	public final <T extends JSOXObject> T getObject (final Class<T> jclass) throws JSOXException {
		return JSOXObject.newInstanceOf(jclass, this.getObject());
	}
	
	@Override
	public final String getString (){
		return this.getString(this.charset);
	}
	
	@Override
	public final String getString (final Charset charset){
		final int          charSize;
		final IOByteBuffer helper;
		final String       string;
		int                stringLength = 0;
		
		charSize = StringUtilities.charSize(charset);
		
		helper = new IODynamicByteBuffer();
		helper.order(this.order());
		
		for (var i = 1;; i++) {
			final int  b;
			final char c;
			
			b = this.getByte();
			
			helper.putByte(b);
			
			if ((i % charSize) != 0) {
				continue;
			}
			
			c = (char)(b);
			
			if (c == '\0') {
				break;
			} else {
				stringLength++;
			}
		}
		
		string = new String(helper.buffer.array(), 0, (stringLength * charSize), charset);
		
		return string;
	}
	
	public IOByteBuffer head (){
		this.position(this.length());
		
		return IOByteBuffer.this;
	}
	
	@Override
	public boolean isFull (){
		return (this.length == this.capacity());
	}
	
	public IOByteBuffer jump (final int count){
		this.position(this.position() + count);
		
		return IOByteBuffer.this;
	}
	
	@Override
	public final int length (){
		return this.length;
	}
	
	public final void length (final int newLength){
		this.length = newLength;
	}
	
	public final void mark (){
		this.checkpoint = this.position();
	}
	
	public final ByteOrder order (){
		return this.buffer.order();
	}
	
	public final IOByteBuffer order (final ByteOrder order){
		this.buffer.order(order);
		this.nativeBuffer.order(order);
		
		return IOByteBuffer.this;
	}
	
	public int position (){
		return this.buffer.position();
	}
	
	public IOByteBuffer position (final int newPosition){
		this.buffer.position(newPosition);
		
		return IOByteBuffer.this;
	}
	
	public final IOByteBuffer put (final ByteBuffer buffer){
		throw new UnsupportedOperationException("Not supported yet.");
	}
	
	public final IOByteBuffer put (final ByteBuffer buffer, final int offset, final int count){
		for (var i = offset; i < count; i++) {
			this.putByte(buffer.array()[i]);
		}
		
		return IOByteBuffer.this;
	}
	
	public final IOByteBuffer put (final IOByteBuffer buffer){
		return this.put(buffer.buffer, 0, buffer.length());
	}
	
	@Override
	public IOByteBuffer putByte (final int val){
		this.buffer.put((byte) val);
		
		this.length++;
		
		return IOByteBuffer.this;
	}
	
	@Override
	public IOByteBuffer putBytes (final byte[] val){
		this.buffer.put(val);
		
		this.length += val.length;
		
		return IOByteBuffer.this;
	}
	
	@Override
	public IOByteBuffer putBytes (final byte[] val, final int offset, final int count){
		this.buffer.put(val, offset, count);
		
		this.length += val.length;
		
		return IOByteBuffer.this;
	}
	
	@Override
	public final IOBuffer putChar (final char val){
		if (this.charset() == UTF_8) {
			return this.putByte((byte) val);
		} else if ((this.charset() == UTF_16) || (this.charset() == UTF_16BE)) {
			this.nativeBuffer.rewind();
			this.nativeBuffer.putChar(val);
			
			return this.put(this.nativeBuffer, 0, Character.BYTES);
		} else {
			throw new UnsupportedOperationException("not supported yet: char conversion to %s not implemented".formatted(this.charset()));
		}
	}
	
	@Override
	public final IOByteBuffer putDouble (final double val){
		this.nativeBuffer.rewind();
		this.nativeBuffer.putDouble(val);
		
		return this.put(this.nativeBuffer, 0, Integer.BYTES);
	}
	
	@Override
	public final IOByteBuffer putInteger (final int val){
		this.nativeBuffer.rewind();
		this.nativeBuffer.putInt(val);
		
		return this.put(this.nativeBuffer, 0, Integer.BYTES);
	}
	
	@Override
	public final IOByteBuffer putLong (final long val){
		this.nativeBuffer.rewind();
		this.nativeBuffer.putLong(val);
		
		return this.put(this.nativeBuffer, 0, Long.BYTES);
	}
	
	public final IOByteBuffer putObject (final JSONObject object) throws JSOXException {
		return this.putObject(new JSOXVariant(object));
	}
	
	public final IOByteBuffer putObject (final JSOXObject object) throws JSOXException {
		this.putBytes(JSOXUtilities.encodeObject(object, this.charset()));
		
		return IOByteBuffer.this;
	}
	
	public final IOByteBuffer putObject (final JSOXVariant object) throws JSOXException {
		this.putBytes(JSOXUtilities.encodeObject(object, this.charset()));
		
		return IOByteBuffer.this;
	}
	
	@Override
	public final IOByteBuffer putString (final String val){
		this.putBytes(val.getBytes(this.charset()));
		this.putChar('\0');
		
		return IOByteBuffer.this;
	}
	
	@Override
	public final IOByteBuffer putString (final String val, final Charset charset){
		this.putBytes(val.getBytes(charset));
		this.putChar('\0');
		
		return IOByteBuffer.this;
	}
	
	@Override
	public int read (final SocketChannel channel) throws IOException {
		final int bytesRead;
		
		bytesRead = channel.read(this.buffer);
		
		if (bytesRead == -1) {
			throw new IOException("connection terminated in the middle of a read operation");
		}
		
		this.tell();
		
		return bytesRead;
	}
	
	public final IOByteBuffer reallocate (final int newSize){
		return this.reallocate(newSize, false);
	}
	
	public final IOByteBuffer reallocate (final int newSize, final boolean discard){
		final ByteBuffer newBuffer;
		final int        newLength;
		final int        newPosition;
		
		newBuffer = ByteBuffer.allocate(newSize);
		newBuffer.order(this.order());
		
		if (! discard) {
			if (newSize < this.capacity()) {
				newBuffer.put(Arrays.copyOfRange(this.buffer.array(), 0, newSize));
				newPosition = newSize;
				newLength = newSize;
			} else {
				newBuffer.put(this.buffer.array());
				newPosition = this.position();
				newLength = this.length;
			}
			
			newBuffer.position(newPosition);
		} else {
			newLength = this.length;
		}
		
		this.buffer = newBuffer;
		
		this.length = newLength;
		
		return IOByteBuffer.this;
	}
	
	public int recall (){
		this.position(this.checkpoint);
		
		return this.checkpoint;
	}
	
	public int remaining (){
		return (this.length() - this.position());
		//return this.buffer.remaining();
	}
	
	public IOByteBuffer rewind (){
		this.buffer.rewind();
		
		return IOByteBuffer.this;
	}
	
	@Override
	public String substring (final int offset, final int length, final Charset charset){
		return new String(this.toString().substring(offset, length).getBytes(charset));
	}
	
	public int tell (){
		final int limit;
		final int position;
		
		limit = this.buffer.limit();
		position = this.buffer.position();
		
		try {
			this.buffer.flip();
			
			return (this.length = this.buffer.limit());
		} finally {
			this.buffer.position(position);
			this.buffer.limit(limit);
		}
	}
	
	@Override
	public byte[] toByteArray (final Charset charset){
		return Arrays.copyOf(this.buffer.array(), this.length());
	}
	
	@Override
	public String toString (){
		return this.toString(this.charset);
	}
	
	@Override
	public String toString (final Charset charset){
		final int charSize;
		final int position;
		final int realLength;
		
		charSize = StringUtilities.charSize(charset);
		
		position = this.position();
		
		try {
			this.buffer.rewind();
			
			realLength = (this.length() / charSize);
			
			return charset.decode(this.buffer).subSequence(0, realLength).toString();
		} finally {
			this.buffer.position(position);
		}
	}
	
	@Override
	public int write (final SocketChannel channel) throws IOException {
		final int limit;
		final int position;
		
		limit = this.buffer.limit();
		position = this.buffer.position();
		
		try {
			int       send;
			int       sendRemaining;
			final int sendTotal;
			
			sendRemaining = sendTotal = this.tell();
			
			while (sendRemaining > 0) {
				this.buffer.flip();
				
				send = channel.write(this.buffer);
				
				if (send == -1) 
					throw new IOException();
				
				this.buffer.compact();
				
				sendRemaining -= send;
			}
			
			return sendTotal;
		} finally {
			this.buffer.position(position);
			this.buffer.limit(limit);
		}
	}
}