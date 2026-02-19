/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.publish.handlers;

import com.xahico.boot.publish.GWXEventListener;
import com.xahico.boot.publish.GWXServiceConfiguration;
import com.xahico.boot.publish.GWXServiceContext;
import com.xahico.boot.publish.GWXSession;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.Attribute;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class GWXSessionLifecycleHandler extends GWXHandlerAdapter {
	public GWXSessionLifecycleHandler (final GWXServiceContext context, final GWXServiceConfiguration config){
		super(context, config);
	}
	
	
	
	@Override
	public void channelInactive (final ChannelHandlerContext ctx) throws Exception {
		final GWXEventListener            listener;
		final Attribute<GWXEventListener> listenerAttrib;
		final GWXSession                  session;
		final Attribute<GWXSession>       sessionAttrib;

		sessionAttrib = ctx.channel().attr(context.keySession);

		if (null != sessionAttrib) {
			session = sessionAttrib.get();

			if (null != session) {
				listenerAttrib = ctx.channel().attr(context.keyListener);

				if (null != listenerAttrib) {
					listener = listenerAttrib.get();
				} else {
					listener = null;
				}

				if (null != listener) {
					session.detachEventListener(listener);
				} else if (!session.isAuthenticated()) {
					destroyInstance(session);
				}

				if (! session.isEventListenerPresent()) {
					dispatchIdleCountdown(session);
				}
			}
		}

		super.channelInactive(ctx);
	}
	
	private void destroyInstance (final GWXSession session){
		session.destroy();
	}
	
	private void dispatchIdleCountdown (final GWXSession session){
		context.executor.execute(new Runnable() {
			final long dispatchedFor = session.getLastContact();
			
			@Override
			public void run (){
				if (session.getLastContact() == dispatchedFor) {
					if (session.lastContactSecondsElapsed() < context.sessionTimeout) {
						context.executor.execute(this);
					} else if (! session.isEventListenerPresent()) {
						destroyInstance(session);
					}
				}
			}
		});
	}
}