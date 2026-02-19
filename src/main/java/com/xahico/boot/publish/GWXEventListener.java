/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.publish;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.ScheduledFuture;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class GWXEventListener {
	private GWXEventCapture              capture = null;
	private final Channel                channel;
	private final GWXAPIInterface.Handle handle;
	private GWXInstance                  instance = null;
	private final GWXResourceManager     rcm;
	private GWXSession                   session = null;
	private GWXEventSubscription         subscription = null;
	private final GWXPath                target;
	private ScheduledFuture<?>           task = null;
	private final Type                   type;
	
	
	
	public GWXEventListener (final GWXResourceManager rcm, final Channel channel, final Type type, final GWXAPIInterface.Handle handle, final GWXPath target){
		super();
		
		this.rcm = rcm;
		this.channel = channel;
		this.type = type;
		this.handle = handle;
		this.target = target;
	}
	
	
	
	public void attach (final GWXSession session){
		this.session = session;
		
		if (null == this.subscription) {
			this.instance = new GWXInstance(rcm, handle.version(), session);
			
			this.subscription = rcm.listen(session, this.target, (path, source, event) -> {
				//System.out.println("EventListener: detected an event");
				if (this.session.checkAccess(path, GWXPermission.OBSERVE)) {
					//System.out.println("And now it can look @ it");
					this.call(source, event);
				} else {
					//System.out.println("BUT DID NOT HAVE OBSERVE RIGHTS TO '%s'".formatted(path));
				}
			});
			
			this.task = this.channel.eventLoop().scheduleAtFixedRate(() -> {
				if (this.channel.isActive()) {
					this.ping();
				}
			}, 10, 10, TimeUnit.SECONDS);
			
			this.capture = GWXEventCapture.createEventCapture(session, this.rcm, this);
		}
	}
	
	void call (final GWXObject source, final GWXEvent event){
		final GWXContext                     eventContext;
		final GWXAPIInterface.EventProcessor eventInterface;
		final GWXEventObject                 eventReady;
		final GWXPath                        eventPath;
		
		eventPath = GWXPath.create(event.path());
		
		//System.out.println("Emitting Event");
		
		//System.out.println("EventListener@call " + event.path() + " to " + source);
		if (this.session.canObserve(source) && (event.version <= this.handle.version())) {
			eventInterface = handle.lookupEventProcessor(eventPath);
			//System.out.println("EventIface: "+eventInterface);
			
			if (null != eventInterface) try {
				eventContext = rcm.buildContext(this.session, eventInterface.pattern, eventPath, GWXPermission.OBSERVE);

				eventReady = eventInterface.call(eventContext, instance, event);
				
				if (null != eventReady) {
					//System.out.println("Event Ready: " + eventReady.toJSONString());
					this.dispatch(event, eventReady);
				}
			} catch (final GWXException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	final GWXEventCapture capture (){
		return this.capture;
	}
	
	public void detach (){
		if (null != this.subscription) {
			this.subscription.cancel();
			this.subscription = null;
		}
		
		if (null != this.task) {
			this.task.cancel(false);
			this.task = null;
		}
		
		if (null != this.handle) {
			this.handle.release();
		}
	}
	
	private void dispatch (final GWXEvent eventInternal, final GWXEventObject eventExternal){
		channel.eventLoop().execute(() -> {
			switch (this.type) {
				case SSE: {
					final StringBuilder msg;
					
					msg = new StringBuilder();
					msg.append("data:");
					msg.append(eventExternal.toJSONStringCompact());
					msg.append("\n\n");
					
					this.channel.writeAndFlush(new DefaultHttpContent(Unpooled.copiedBuffer(msg.toString(), StandardCharsets.UTF_8))).addListener(future -> {
						if (! future.isSuccess()) {
							this.capture.log(eventInternal);
						} else {
							this.session.onBroadcast(eventInternal);
						}
					});
					
					break;
				}
				case WS: {
					this.channel.writeAndFlush(new TextWebSocketFrame(eventExternal.toJSONStringCompact())).addListener(future -> {
						if (! future.isSuccess()) {
							this.capture.log(eventInternal);
						} else {
							this.session.onBroadcast(eventInternal);
						}
					});
					
					break;
				}
			}
		});
	}
	
	public boolean listensTo (final GWXPath path){
		return this.target.equals(path);
	}
	
	public GWXPath path (){
		return this.target;
	}
	
	public void ping (){
		//System.out.println("Pinging Event Listener");
		
		switch (this.type) {
			case SSE: {
				final String msg;
				
				msg = ": ping\n\n";
				
				this.channel.writeAndFlush(new DefaultHttpContent(Unpooled.copiedBuffer(msg, StandardCharsets.UTF_8)));
				
				break;
			}
			case WS: {
				this.channel.writeAndFlush(new PingWebSocketFrame());
				
				break;
			}
		}
		
		this.session.updateLastContact();
	}
	
	public Type type (){
		return this.type;
	}
	
	
	
	public static enum Type {
		SSE,
		WS,
	}
}