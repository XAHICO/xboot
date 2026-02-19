/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.net.web.http;

import com.xahico.boot.cryptography.SSL;
import com.xahico.boot.reflection.ClassFactory;
import com.xahico.boot.net.InvalidTokenException;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import com.xahico.boot.pilot.ServiceFactorizer;
import com.xahico.boot.pilot.ServiceInitializer;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public class HttpProxyServiceProvider extends HttpServiceProviderBase {
	private static final Executor SHARED_EXECUTOR = Executors.newSingleThreadExecutor();
	
	
	
	@ServiceFactorizer
	private static HttpProxyServiceProvider createService (final HttpProxyService service, final ClassFactory<? extends HttpProxyServiceBase> classFactory){
		return new HttpProxyServiceProvider(classFactory.newInstance());
	}
	
	@ServiceInitializer
	private static void initializeService (final HttpProxyService service, final HttpProxyServiceProvider serviceProvider) throws Throwable {
		serviceProvider.setAuthToken(service.authToken());
		serviceProvider.setBindPort(service.port());
		serviceProvider.setDiscloseToken(service.discloseToken());
	}
	
	
	
	private String                     authToken = null;
	private boolean                    discloseToken = false;
	private final HttpProxyServiceBase instance;
	private final HttpServiceProvider  serviceProvider = new HttpServiceProvider();
	
	
	
	HttpProxyServiceProvider (final HttpProxyServiceBase instance){
		super();
		
		this.instance = instance;
	}
	
	
	
	@Override
	protected void cleanup (){
		this.serviceProvider.stop();
	}
	
	@Override
	public int getPort (){
		return this.serviceProvider.getPort();
	}
	
	@Override
	protected void initialize () throws Throwable {
		this.serviceProvider.setLogger(this.getLogger());
		this.serviceProvider.setBindPort(this.getBindPort());
		this.serviceProvider.setExecutor(SHARED_EXECUTOR);
		this.serviceProvider.setRoutingHandler((env, client, exchange) -> {
			try {
				final String[] ignoredHeaders;
				final String   token;
				final URL      url;
				
				token = exchange.getRequestHeader(this.authToken);
				
				if (null == token) {
					throw new InvalidTokenException();
				}
				
				url = instance.redirect(token, exchange.getRequestTarget());
				
				if (this.discloseToken) 
					ignoredHeaders = new String[0];
				else {
					ignoredHeaders = new String[]{this.authToken};
				}
				
				exchange.getResponseHeaders().add("Connection", "close");
				
				HttpUtilities.transactTunneled(url, exchange, false, ignoredHeaders);
			} catch (final InvalidTokenException ex) {
				exchange.sendResponseNotFound();
			} catch (final IOException ex) {
				this.getLogger().log(ex);
			} finally {
				exchange.getResponseBody().close();
			}
		});
		
		if (SSL.class.isAssignableFrom(instance.getClass())) {
			this.serviceProvider.setSSL(true);
			this.serviceProvider.setSSLContext(((SSL)instance).createSSLContext());
		} else {
			this.serviceProvider.setSSL(false);
			this.serviceProvider.setSSLContext(null);
		}
	}
	
	@Override
	public boolean isIdle (){
		return false;
	}
	
	@Override
	public boolean isStepper (){
		return false;
	}
	
	@Override
	protected void run (){
		this.serviceProvider.start();
	}
	
	public void setAuthToken (final String authToken){
		this.authToken = authToken;
	}
	
	public void setDiscloseToken (final boolean discloseToken){
		this.discloseToken = discloseToken;
	}
}