/*
 * Written by Tuomas Kontiainen <https://www.github.com/tutomiko>
 * 
 * Copyright (c) 2023, Tuomas Kontiainen
 */
package com.xahico.boot.net.web.http.model.glass;

import com.xahico.boot.io.Source;
import com.xahico.boot.lang.css.s3.S3Exception;
import com.xahico.boot.lang.css.s3.S3Parser;
import com.xahico.boot.lang.html.HTMLDocument;
import com.xahico.boot.lang.html.HTMLException;
import com.xahico.boot.lang.html.HTMLNode;
import com.xahico.boot.lang.html.HTMLStandardType;
import com.xahico.boot.lang.html.HTMLUtilities;
import com.xahico.boot.lang.javascript.JSBuilder;
import com.xahico.boot.lang.javascript.JSCode;
import com.xahico.boot.lang.javascript.JSEnum;
import com.xahico.boot.lang.javascript.JSFunction;
import com.xahico.boot.lang.javascript.JSNamedObject;
import com.xahico.boot.lang.javascript.JSSingleton;
import com.xahico.boot.lang.javascript.JSType;
import com.xahico.boot.lang.javascript.JSVariable;
import com.xahico.boot.cryptography.SSL;
import com.xahico.boot.lang.html.fx.HTFXParser;
import com.xahico.boot.reflection.MethodNotFoundException;
import com.xahico.boot.net.URIA;
import com.xahico.boot.net.NoSuchParameterException;
import com.xahico.boot.net.web.http.HttpServiceProvider;
import com.xahico.boot.net.web.http.HttpServiceProviderBase;
import com.xahico.boot.net.web.http.HttpStatus;
import com.xahico.boot.lang.jsox.JSOXObject;
import com.xahico.boot.logging.Logger;
import com.xahico.boot.reflection.ClassFactory;
import com.xahico.boot.net.web.http.HttpEvent;
import com.xahico.boot.net.web.http.HttpMethod;
import com.xahico.boot.net.web.http.HttpMimeType;
import com.xahico.boot.net.web.http.HttpUtilities;
import com.xahico.boot.net.web.http.InvalidMethodException;
import com.xahico.boot.net.web.http.URLBuilder;
import com.xahico.boot.pilot.AccessException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;
import javax.net.ssl.SSLContext;
import com.xahico.boot.pilot.ServiceFactorizer;
import com.xahico.boot.pilot.ServiceInitializer;
import com.xahico.boot.reflection.Reflection;
import com.xahico.boot.reflection.ReflectionMethod;
import com.xahico.boot.util.Exceptions;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class GlassServiceProvider extends HttpServiceProviderBase {
	private static final String FILE_NAME_THEME = "theme.ini";
	private static final String GMT_REQUIRE_AUTH = "site-require-auth";
	
	
	
	static Set<GlassControlMethod> collectControlMethods (final Class<?> instanceClass){
		final Set<GlassControlMethod> collection;
		final Reflection<?>           reflection;
		
		reflection = Reflection.of(instanceClass);
		
		collection = new HashSet<>();
		
		for (final ReflectionMethod reflectionMethod : reflection.getMethodsAnnotatedWith(GlassControl.class)) {
			final GlassControl       annotation;
			final String             control;
			final int                expectParams;
			final GlassControlMethod exportMethod;
			final Class[]            paramClasses;
			final Class<?>           requestClass;
			final Class<?>           responseClass;
			
			if (reflectionMethod.isStatic())
				throw new Error(String.format("Invalid export method '%s' is static", reflectionMethod.getName()));
			
			annotation = reflectionMethod.getAnnotation(GlassControl.class);
			
			control = annotation.value();
			
			expectParams = 2;
			paramClasses = reflectionMethod.getParameterClasses();
			
			if (paramClasses.length != expectParams) 
				throw new Error(String.format("Invalid declaration of method for '%s': invalid parameter count (expected %d, was %d)", reflectionMethod.getName(), expectParams, paramClasses.length));
			
			requestClass = paramClasses[0];
			
			responseClass = paramClasses[1];
			
			exportMethod = new GlassControlMethod(reflectionMethod, control, requestClass, responseClass);
			
			collection.add(exportMethod);
		}
		
		return collection;
	}
	
	static Set<GlassRoutingMethod> collectRoutingMethods (final Class<?> instanceClass){
		final Set<GlassRoutingMethod> collection;
		final Reflection<?>           reflection;
		
		reflection = Reflection.of(instanceClass);
		
		collection = new HashSet<>();
		
		for (final ReflectionMethod reflectionMethod : reflection.getMethodsAnnotatedWith(GlassRoute.class)) {
			final GlassRoute         annotation;
			final GlassRoutingMethod exportMethod;
			
			if (reflectionMethod.isStatic())
				throw new Error(String.format("Invalid export method '%s' is static", reflectionMethod.getName()));
			
			annotation = reflectionMethod.getAnnotation(GlassRoute.class);
			
			exportMethod = new GlassRoutingMethod(reflectionMethod, annotation);
			
			collection.add(exportMethod);
		}
		
		return collection;
	}
	
	@ServiceFactorizer
	static GlassServiceProvider createService (final GlassService service, final ClassFactory<? extends GlassServiceBase> classFactory){
		try {
			return new GlassServiceProvider(classFactory.newInstance(), service.sessionClass(), service.eventClass(), service.statusClass());
		} catch (final IOException ex) {
			throw new Error(ex);
		}
	}
	
	static Map<String, String> getEndorsements (final GlassService service){
		final String[]            array;
		final Map<String, String> mappings;
		
		mappings = new HashMap<>();
		
		array = service.endorsements();
		
		if (array.length == 0) {
			return mappings;
		}
		
		for (var i = 0; i < array.length; i++) {
			final int    delimiter;
			final String endorsement;
			final String endorsementKey;
			final String endorsementValue;
			
			endorsement = array[i];
			
			delimiter = endorsement.indexOf(':');
			
			if (delimiter == -1) {
				throw new Error(String.format(""));
			}
			
			endorsementKey = endorsement.substring(0, delimiter).strip();
			endorsementValue = endorsement.substring(delimiter + 1).strip();
			
			mappings.put(endorsementKey, endorsementValue);
		}
		
		return mappings;
	}
	
	@ServiceInitializer
	static void initializeService (final GlassService service, final GlassServiceProvider serviceProvider) throws Throwable {
		serviceProvider.setAuthToken(service.authToken());
		serviceProvider.setBindPort(service.port());
		serviceProvider.setDefaultPath(service.defaultPath());
		serviceProvider.setEndorsements(getEndorsements(service));
		serviceProvider.setEventHandler(service.eventHandler());
		serviceProvider.setFileServer(service.fileServer());
		
		if (service.multithreaded()) {
			serviceProvider.setExecutor(Executors.newCachedThreadPool());
		} else {
			serviceProvider.setExecutor(Executors.newSingleThreadExecutor());
		}
	}
	
	
	
	private String                                     authToken = null;
	private String                                     defaultPath = null;
	private final Map<String, String>                  endorsements = new HashMap<>();
	private final Class<? extends GlassEvent>          eventBaseClass;
	private boolean                                    eventHandler = false;
	private boolean                                    fileServer = false;
	private final GlassNamespace                       globalNamespace = new GlassNamespace();
	private final GlassServiceBase                     instance;
	private final HttpServiceProvider                  internalProvider = new HttpServiceProvider();
	private double                                     pingInterval = 60;
	private final Set<GlassControlMethod>              sessionControls;
	private final ClassFactory<? extends GlassSession> sessionFactory;
	private final Set<GlassRoutingMethod>              sessionRoutes;
	private long                                       sessionTimeout = (30 * 60 * 1000);
	private final GlassDocumentLoader                  shareDocumentLoader = new GlassDocumentLoader() {
		@Override
		public HTMLDocument load (final File file, final boolean accessRestricted) throws IOException {
			return loadDocument(file, accessRestricted);
		}
		
		@Override
		public File lookup (final String path){
			return new File(instance.rootDirectory(), path);
		}
		
		@Override
		public String realize (final HTMLDocument document, final GlassNamespace context){
			return GlassImporter.importString(document.toHTMLString(), context);
		}
	};
	private final Class<? extends Enum>                statusClass;
	private Enum<?>                                    statusMarkerDefault = null;
	private Enum<?>                                    statusMarkerSuccess = null;
	private GlassTheme                                 theme = null;
	
	
	
	GlassServiceProvider (final GlassServiceBase instance, final Class<? extends GlassSession> sessionClass, final Class<? extends GlassEvent> eventBaseClass, final Class<? extends Enum> statusClass) throws IOException {
		super();
		
		this.instance = instance;
		this.sessionFactory = ClassFactory.getClassFactory(sessionClass);
		this.sessionControls = collectControlMethods(sessionClass);
		this.sessionRoutes = collectRoutingMethods(sessionClass);
		this.eventBaseClass = eventBaseClass;
		this.statusClass = statusClass;
		
		for (final var field : statusClass.getDeclaredFields()) {
			if (field.isAnnotationPresent(GlassDefaultMarker.class)) {
				statusMarkerDefault = Enum.valueOf(statusClass, field.getName());
			}
			
			if (field.isAnnotationPresent(GlassSuccessMarker.class)) {
				statusMarkerSuccess = Enum.valueOf(statusClass, field.getName());
			}
		}
	}
	
	
	
	private JSCode buildInterface (final HTMLDocument document){
		final GlassInterfaceBuilder builder;
		
		builder = new GlassInterfaceBuilder();
		builder.createAuth();
		
		if (this.eventHandler) {
			builder.addEventConstants();
			builder.createEventHandler();
		}
		
		if (! this.sessionControls.isEmpty()) {
			builder.addStatusConstants();
			builder.addMethods();
		}
		
		builder.addCustomClasses(document);
		builder.createLoader();
		builder.organize();
		
		//System.out.println(builder.build());
		
		return builder.build();
	}
	
	@Override
	protected void cleanup (){
		this.internalProvider.stop();
		
		this.instance.stopClock();
	}
	
	private GlassControlMethod getControlMethod (final String control) throws MethodNotFoundException {
		for (final var method : this.sessionControls) {
			if (method.getControl().equalsIgnoreCase(control)) {
				return method;
			}
		}
		
		throw new MethodNotFoundException(String.format("No control method found for '%s'", control));
	}
	
	@Override
	public int getPort (){
		return this.internalProvider.getPort();
	}
	
	private GlassRoutingMethod getRoutingMethod (final String path, final boolean accessRestricted) throws MethodNotFoundException {
		for (final var method : this.sessionRoutes) {
			if (accessRestricted) {
				if (!method.isPrivateAccessible()) {
					continue;
				}
			} else {
				if (!method.isPublicAccessible()) {
					continue;
				}
			}
			
			if (method.matches(path)) {
				return method;
			}
		}
		
		throw new MethodNotFoundException(String.format("No routing method found for '%s'", path));
	}
	
	@Override
	protected void initialize () throws Throwable {
		this.globalNamespace.set("this", this.sessionFactory.getProductionClass().getSimpleName());
		this.loadTheme();
		
		this.instance.setExecutor(Executors.newSingleThreadExecutor());
		this.instance.initSessionFactory(this.sessionFactory);
		this.instance.startClock();
		
		this.internalProvider.setBindPort(this.getBindPort());
		this.internalProvider.setExecutor(this.getExecutor());
		this.internalProvider.setLogger(this.getLogger());
		
		this.internalProvider.setInitializeHandler((env, client, exchange) -> {
			final GlassSession session;
			final String       token;
			final String       tokenUsable;
			
			exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
			exchange.getResponseHeaders().add("Access-Control-Allow-Credentials", "true");
			exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET,HEAD,OPTIONS,POST,PUT");
			exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Access-Control-Allow-Headers, Origin,Accept, X-Requested-With, Content-Type, Access-Control-Request-Method, Access-Control-Request-Headers, Authorization");
			
			for (final String endorsementKey : this.endorsements.keySet()) {
				final String endorsementValue;
				
				endorsementValue = this.endorsements.get(endorsementKey);
				
				exchange.getResponseHeaders().add(endorsementKey, endorsementValue);
			}
			
			if (this.instance instanceof GlassServiceBase) {
				if (null != this.authToken) {
					token = exchange.getRequestHeaders().getCookie(this.authToken);
					
					if ((null == token) || token.isBlank() || token.equalsIgnoreCase("null")) 
						tokenUsable = exchange.getRequestHeader("Authorization");
					else {
						tokenUsable = token;
					}
				} else {
					token = tokenUsable = null;
				}
				
				session = this.instance.spawnSession(client, tokenUsable, globalNamespace);
				session.initDocumentLoader(shareDocumentLoader);
			}
		});
		
		if (! this.sessionControls.isEmpty()) {
			this.internalProvider.setActionHandler("*", (env, client, exchange) -> {
				final String             control;
				final Logger.Entry       logEntry;
				final GlassControlMethod method;
				final GlassRequest       request;
				final GlassResponse      response;
				final URIA               target;
				
				exchange.getResponseHeaders().add("Connection", "Close");
				
				if (exchange.getRequestMethod() == HttpMethod.OPTIONS) {
					exchange.sendResponse(HttpStatus.STATUS_OK, 0);
					
					return;
				}
				
				logEntry = this.getLogger().currentEntry(true);
				logEntry.writeLine("SOURCE: %s".formatted(this));
				logEntry.writeLine("TYPE: Transaction/Action");

				try {
					target = exchange.getRequestTarget();
					
					control = target.getQueryParameter("id");

					method = this.getControlMethod(control);
					
					request = (GlassRequest) exchange.readRequestBodyJSOX(method.getRequestClass());

					response = (GlassResponse) JSOXObject.newInstanceOf(method.getResponseClass());

					method.invoke(this.instance.currentSession(), request, response);
					
					logEntry.writeLine("STATUS: success");
					logEntry.writeLine("REQUEST: %s".formatted(request.toJSONString()));
					logEntry.writeLine("RESPONSE: %s".formatted(response.toJSONString()));
					
					exchange.sendResponseJSOX(HttpStatus.STATUS_OK, response);
				} catch (final ExecutionException ex) {
					logEntry.writeLine("STATUS: failure");
					logEntry.writeLine(ex);

					exchange.sendResponseUnauthorized();
				} catch (final MethodNotFoundException ex) {
					final boolean handled;
					
					handled = this.instance.currentSession().onUnhandledAction(exchange);
					
					if (! handled) {
						logEntry.writeLine("STATUS: failure");
						logEntry.writeLine(ex);
						
						exchange.sendResponseNotFound();
					} else {
						logEntry.writeLine("STATUS: success");
					}
				} catch (final NoSuchParameterException ex) {
					logEntry.writeLine("STATUS: failure");
					logEntry.writeLine(ex);
					
					exchange.sendResponseNotFound();
				} finally {
					logEntry.close();
				}
			});
		}
		
		if (this.eventHandler) {
			this.internalProvider.setEventSubscriptionHandler("*", (env, client, exchange) -> {
				final GlassSession session;
				
				session = this.instance.currentSession();
				session.setIdleTimeout(0);
				session.attachEventHandler();
				
				if (! session.isAuthenticated()) {
					exchange.sendResponseUnauthorized();
				} else try {
					exchange.sendResponseEventHandlingBegins();
					
					for (;;) try {
						final HttpEvent    eventHTTP;
						final GlassEvent   eventNUDE;
						final Logger.Entry logEntry;
						
						logEntry = this.getLogger().newEntry(true);
						
						try {
							eventNUDE = session.getEvent((long)(this.pingInterval * 1000.0));
							eventHTTP = new HttpEvent();
							eventHTTP.setData(eventNUDE.toJSONStringCompact());
							eventHTTP.setEvent(eventNUDE.eventCode());

							logEntry.writeLine("SOURCE: %s".formatted(this));
							logEntry.writeLine("TYPE: Subscription/Events");
							logEntry.writeLine("EVENT: %s".formatted(eventNUDE.eventCode()));
							logEntry.writeLine("EVENT_DATA: %s".formatted(eventNUDE.toJSONString()));
							
							exchange.sendEvent(eventHTTP);
							
							session.updateContact();
						} finally {
							logEntry.close();
						}
					} catch (final InterruptedException ex) {
						throw new Error(ex);
					} catch (final TimeoutException ex) {
						final HttpEvent    eventHTTP;
						final GlassEvent   eventNUDE;
						final Logger.Entry logEntry;
						
						logEntry = this.getLogger().currentEntry(true);

						try {
							if (session.isDead()) {
								break;
							}
							
							eventNUDE = new GlassPingEvent();
							eventHTTP = new HttpEvent();
							eventHTTP.setData(eventNUDE.toJSONStringCompact());
							eventHTTP.setEvent(eventNUDE.eventCode());

							logEntry.writeLine("SOURCE: %s".formatted(this));
							logEntry.writeLine("TYPE: Subscription/Events");
							logEntry.writeLine("EVENT: %s".formatted(eventNUDE.eventCode()));
							logEntry.writeLine("EVENT_DATA: %s".formatted(eventNUDE.toJSONString()));
							
							exchange.sendEvent(eventHTTP);
							
							session.updateContact();
						} finally {
							logEntry.close();
						}
					}
				} catch (final IOException ex) {
					Exceptions.ignore(ex);
				} finally {
					exchange.sendResponseEventHandlingEnds();
					
					session.detachEventHandler();
				}
			});
		}

		if (this.fileServer) {
			this.internalProvider.setRoot(this.instance.rootDirectory());
			this.internalProvider.setRoutingHandler((env, client, exchange) -> {
				try {
					final GlassSession session;
					
					exchange.getResponseHeaders().add("Connection", "Close");

					session = this.instance.currentSession();

					switch (exchange.getRequestMethod()) {
						case GET: {
							final GlassRoutingMethod route;
							final URIA               target;
							
							target = exchange.getRequestTarget();
							
							try {
								try {
									final GlassNavigationContext context;
									final File                   file;
									final HttpMimeType           fileType;
									final GlassResource          resource;
									
									route = getRoutingMethod(target.getPath(), session.isAuthenticated());
									
									context = GlassNavigationContext.parseContext(route.getPath(), target.getPath());
									
									if (route.getTarget().isBlank()) {
										file = null;
										fileType = null;
									} else {
										file = env.lookup(route.getTarget(), null);
										fileType = HttpUtilities.getFileHttpType(file);
									}
									
									resource = new GlassResource();
									
									if (null != file) {
										resource.setContentType(fileType);
										
										if (fileType == HttpMimeType.HTML) {
											resource.setContent(prepareDocument(session, file, route.isPrivateAccessible()));
										} else {
											resource.setContent(prepareResource(session, file, fileType));
										}
									}
									
									try {
										if (route.type() == GlassRouteType.LOCATION) {
											session.relocateFirst(target);
										}
										
										route.invoke(session, context, target, resource);
										
										if (!resource.hasContent() || (null == resource.getContentType())) {
											throw new FileNotFoundException();
										} else {
											if (null != resource.getName()) {
												exchange.getResponseHeaders().add("FileName", resource.getName());
											}
											
											if (resource.isAttachment()) {
												exchange.getResponseHeaders().add("Content-Disposition", "attachment; filename=\"%s\"".formatted(resource.getName()));
											}
											
											exchange.sendResponseData(HttpStatus.STATUS_OK, resource.getContent(), resource.getContentType());
										}
										
										if (route.type() == GlassRouteType.LOCATION) {
											session.relocate(target);
										}
									} catch (final ExecutionException ex) {
										if (route.type() == GlassRouteType.LOCATION) {
											session.relocate(null);
										}
										
										if (ex.getCause() instanceof AccessException) {
											exchange.sendResponseUnauthorized();
										} else {
											throw new FileNotFoundException();
										}
									}
								} catch (final MethodNotFoundException ex) {
									final File         file;
									final HttpMimeType fileType;
									
									if (target.isDefault()) {
										throw new FileNotFoundException();
									}
									
									file = env.lookup(target, null);
									fileType = HttpUtilities.getFileHttpType(file);

									if (fileType == HttpMimeType.HTML) {
										for (final var routingMethod : this.sessionRoutes) {
											if (target.getPath().regionMatches(true, 1, routingMethod.getTarget(), 0, routingMethod.getTarget().length())) {
												throw new FileNotFoundException();
											}
										}
										
										exchange.sendResponseData(HttpStatus.STATUS_OK, prepareDocument(session, file, false), fileType);
									} else if ((fileType == HttpMimeType.CSS) || (fileType == HttpMimeType.JAVASCRIPT)|| (fileType == HttpMimeType.JSON)|| (fileType == HttpMimeType.TEXT)) {
										exchange.sendResponseData(HttpStatus.STATUS_OK, prepareResource(session, file, fileType), fileType);
									} else {
										exchange.sendResponseFile(file);
									}
								}
							} catch (final FileNotFoundException ex) {
								if (session.isDefaultPath()) {
									exchange.sendResponseRedirect(session.getDefaultPath());
								} else if (!this.defaultPath.isEmpty()) {
									exchange.sendResponseRedirect(this.defaultPath);
								} else {
									exchange.sendResponseNotFound();
								}
							}

							break;
						}
						case POST: case PUT: {
							exchange.sendResponseUnauthorized();

							break;
						}
						default: {
							exchange.sendResponseUnauthorized();
						}
					}
				} catch (final InvalidMethodException ex) {
					exchange.sendResponseUnauthorized();
				}
			});
		}
		
		if (this.instance instanceof SSL) {
			final SSL        sslInterface;
			final SSLContext sslContext;
			
			sslInterface = (SSL)(this.instance);
			
			sslContext = sslInterface.createSSLContext();
			
			this.internalProvider.setSSL(true);
			this.internalProvider.setSSLContext(sslContext);
		} else {
			this.internalProvider.setSSL(false);
			this.internalProvider.setSSLContext(null);
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
	
	private HTMLDocument loadDocument (final File file, final boolean accessRestricted) throws IOException {
		try {
			final HTMLDocument document;
			HTMLNode           documentHead;
			final HTFXParser   documentLoader;
			final HTMLNode     documentRoot;
			final JSCode       preparedInterface;
			
			documentLoader = new HTFXParser();
			documentLoader.setClassDirectory(this.instance.rootDirectory().getAbsolutePath());
			documentLoader.setSource(Source.wrapFile(file));
			documentLoader.setStyleTranslator((data) -> {
				if (! file.getAbsolutePath().endsWith(".s3.css")) {
					return data;
				}
				
				try {
					final S3Parser parser;
					
					parser = new S3Parser();
					parser.setSource(Source.wrapString(data));
					
					return parser.parse().compile();
				} catch (final IOException | S3Exception ex) {
					throw new Error(ex);
				}
			});
			
			document = documentLoader.parse();
			documentRoot = document.lookupFirst("html");
			documentHead = document.lookupFirst(HTMLStandardType.HEAD);
			
			if ((null == documentHead) && (null != documentRoot)) {
				documentHead = new HTMLNode(HTMLStandardType.HEAD);
				documentRoot.getChildren().add(0, documentHead);
			}
			
			if (null != documentHead) {
				preparedInterface = this.buildInterface(document);
				
				documentHead.addChild(HTMLUtilities.createScript(preparedInterface.toJavaScript()));
				documentHead.addChild(HTMLUtilities.createMeta(GMT_REQUIRE_AUTH, Boolean.toString(accessRestricted)));
			}
			
			document.removeSpecialElements();
			document.removeComments();
			
			return document;
		} catch (final HTMLException ex) {
			throw new Error(ex);
		}
	}
	
	private void loadTheme (){
		final File fileTheme;
		
		fileTheme = new File(this.instance.rootDirectory(), FILE_NAME_THEME);
		
		if (fileTheme.exists()) try {
			this.theme = GlassTheme.loadThemeFromFile(fileTheme);
			this.theme.mapTo(globalNamespace);
		} catch (final IOException ex) {
			Exceptions.ignore(ex);
		}
	}
	
	private String prepareDocument (final GlassSession session, final File file, final boolean accessRestricted) throws IOException {
		return GlassImporter.importString(this.loadDocument(file, accessRestricted).toHTMLString(), session.getNamespace());
	}
	
	private String prepareResource (final GlassSession session, final File file, final HttpMimeType fileType) throws IOException {
		final String data;
		
		data = GlassImporter.importString(Files.readString(file.toPath()), session.getNamespace());
		
		if (fileType == HttpMimeType.CSS) {
			if (file.getAbsolutePath().endsWith(".s3.css")) {
				try {
					final S3Parser parser;

					parser = new S3Parser();
					parser.setSource(Source.wrapString(data));

					return parser.parse().compile();
				} catch (final IOException | S3Exception ex) {
					throw new Error(ex);
				}
			}
		}
		
		return data;
	}
	
	@Override
	protected void run (){
		System.out.println("Glass IP invoked");
		this.internalProvider.start();
		System.out.println("Glass IP did not get stuck as a cock in dock");
	}
	
	public void setAuthToken (final String authToken){
		this.authToken = authToken;
	}
	
	public void setDefaultPath (final String path){
		this.defaultPath = path;
	}
	
	public void setEndorsements (final Map<String, String> endorsements){
		this.endorsements.clear();
		this.endorsements.putAll(endorsements);
	}
	
	public void setEventHandler (final boolean eventHandler){
		this.eventHandler = eventHandler;
	}
	
	public void setFileServer (final boolean fileServer){
		this.fileServer = fileServer;
	}
	
	public void setPingInterval (final double intervalSeconds){
		this.pingInterval = intervalSeconds;
	}
	
	public void setSessionTimeout (final long timeoutMillis){
		this.sessionTimeout = timeoutMillis;
	}
	
	
	
	private final class GlassInterfaceBuilder {
		public static final String RESET_SITE = "/";
		
		
		
		private final Map<String, JSFunction> exportFunctions = new HashMap<>();
		private final List<JSNamedObject>     globals = new ArrayList<>();
		private final List<JSCode>            globalBlocks = new ArrayList<>();
		private final List<JSFunction>        internalFunctions = new ArrayList<>();
		private final List<JSVariable>        internalVariables = new ArrayList<>();
		
		
		
		GlassInterfaceBuilder (){
			super();
		}


		
		public void addCustomClasses (final HTMLDocument document){
			/*
			final Set<GlassClass>  classList;
			final GlassClassLoader classLoader;
			
			classLoader = new GlassClassLoader();
			classList = classLoader.loadImports(document);

			if (classList.size() > 0) {
				for (final var classObject : classList) {
					final JSBuilder    classDefine;
					final HTMLDocument classDocument;
					final File         classDocumentFile;
					final File         classScriptFile;
					final HTMLNode     classStylesheet;
					final File         classStylesheetFile;

					/*
					 * ...
					 *
					classDocumentFile = new File(instance.rootDirectory(), (classObject.path() + ".html"));

					if (! classDocumentFile.exists()) 
						continue;
					
					try {
						classDocument = HTMLParser.parseString(Files.readString(classDocumentFile.toPath()));
						classDocument.removeComments();
						classDocument.setIncludeDef(false);

						classDefine = new JSBuilder();
						classDefine.addLine("class %s extends %s {".formatted(classObject.name(), "GlassElement"));
						classDefine.addLine("constructor (){");
						classDefine.addLine("super(`%s`);".formatted(classDocument.toHTMLStringHumanUnreadable()));
						classDefine.addLine("}");
						classDefine.addLine("}");
						classDefine.addLine();
						classDefine.addLine("window.customElements.define(\"%s\", %s);".formatted(JSOXUtilities.translateJavaFieldToJSONField(classObject.name()), classObject.name()));

						globalBlocks.add(classDefine.build());
					} catch (final HTMLException | IOException ex) {
						throw new Error(ex);
					}
					
					/*
					 * ...
					 *
					classStylesheetFile = new File(instance.rootDirectory(), (classObject.path() + ".css"));
					
					if (classStylesheetFile.exists()) {
						final HTMLNode documentRoot;
						
						documentRoot = document.lookupFirst("head", Integer.MAX_VALUE);
						
						if (null != documentRoot) {
							classStylesheet = new HTMLNode(HTMLStandardType.LINK);
							classStylesheet.setAttribute("rel", "stylesheet");
							classStylesheet.setAttribute("href", ("/" + classObject.path() + ".css"));
							
							documentRoot.addChild(classStylesheet);
						}
					}
					
					/*
					 * ...
					 *
					classScriptFile = new File(instance.rootDirectory(), (classObject.path() + ".js"));
					
					if (classScriptFile.exists()) {
						
					}
				}
				
				globalBlocks.add(0, GlassCore.loadResource("GlassElement"));
			}
			
			/*
			if (false)
			if (null != instance.rootDirectory()) {
				final AtomicInteger counter;
				
				counter = new AtomicInteger(0);
				
				try (final var stream = Files.walk(instance.rootDirectory().toPath(), Integer.MAX_VALUE)) {
					stream.filter(path -> !path.equals(instance.rootDirectory().toPath()))
					      .forEach(path -> {
							final JSBuilder classDefine;
							final HTMLDocument classDocument;
							final String    className;
							final File      file;
							final String    fileExtension;
							final String    fileName;
							
							file = path.toFile();
							
							fileName = file.getName();
							
							fileExtension = FileUtilities.getFileExtension(fileName, 1);
							
							if (".class".equalsIgnoreCase(fileExtension)) try {
								className = FileUtilities.getFileNameWithoutExtensions(fileName);
								
								classDocument = HTMLParser.parseString(Files.readString(path));
								classDocument.removeComments();
								classDocument.setIncludeDef(false);
								
								classDefine = new JSBuilder();
								classDefine.addLine("class %s extends %s {".formatted(className, "GlassElement"));
								classDefine.addLine("constructor (){");
								classDefine.addLine("super(`%s`);".formatted(classDocument.toHTMLStringHumanUnreadable()));
								classDefine.addLine("}");
								classDefine.addLine("}");
								classDefine.addLine();
								classDefine.addLine("window.customElements.define(\"%s\", %s);".formatted(JSOXUtilities.translateJavaFieldToJSONField(className), className));
								
								globalBlocks.add(classDefine.build());
								
								counter.incrementAndGet();
							} catch (final HTMLException | IOException ex) {
								throw new Error(ex);
							}
						}
					);
				} catch (final IOException ex) {
					throw new Error(ex);
				}
				
				if (counter.get() > 0) {
					globalBlocks.add(0, GlassCore.loadResource("GlassElement"));
				}
			}
			*/
		}
		
		public void addEventConstants (){
			final JSEnum enumEvent;

			enumEvent = new JSEnum();
			enumEvent.setName(eventBaseClass.getSimpleName());

			for (final var eventClass : Reflection.collectSubclassesOf(eventBaseClass)) {
				if (! eventClass.isAnnotationPresent(GlassEventMarkup.class)) 
					throw new Error();
				else {
					enumEvent.addValue(eventClass.getSimpleName());
				}
			}

			globals.add(enumEvent);
		}
		
		public void addMethods (){
			final JSFunction function;
			final JSBuilder  functionBody;
			final URLBuilder urlBuilder;
			
			urlBuilder = new URLBuilder();
			urlBuilder.init(internalProvider.getRemoteName());
			urlBuilder.setPath("/action");
			urlBuilder.setQuery("id=${fp}");
			
			functionBody = new JSBuilder();
			functionBody.addLine("var request = {};");
			functionBody.addLine("var xhttpc = null;");
			functionBody.addLine();
			functionBody.addLine("if (datax) {");
			functionBody.addLine("if (datax instanceof SubmitEvent) {");
			functionBody.addLine("const event = datax;");
			functionBody.addLine("const formData = new FormData(event.target);");
			functionBody.addLine();
			functionBody.addLine("event.preventDefault();");
			functionBody.addLine();
			functionBody.addLine("formData.forEach((value, key) => request[key] = value);");
			functionBody.addLine("} else if (datax instanceof HTMLFormElement) {");
			functionBody.addLine("const formData = new FormData(datax);");
			functionBody.addLine();
			functionBody.addLine("formData.forEach((value, key) => request[key] = value);");
			functionBody.addLine("} else if (datax instanceof FormData) {");
			functionBody.addLine("datax.forEach((value, key) => request[key] = value);");
			functionBody.addLine("} else {");
			functionBody.addLine("request = datax;");
			functionBody.addLine("}");
			functionBody.addLine("}");
			functionBody.addLine();
			functionBody.addLine("xhttpc = new XMLHttpRequest();");
			functionBody.addLine("xhttpc.onreadystatechange = () => {");
			functionBody.addLine("if (xhttpc.readyState == XMLHttpRequest.DONE) {");
			functionBody.addLine("var httpOkay = false;");
			functionBody.addLine("var protOkay = false;");
			functionBody.addLine("var response = null;");
			functionBody.addLine();
			functionBody.addLine("httpOkay = (xhttpc.status == 200);");
			functionBody.addLine();
			functionBody.addLine("if (httpOkay) {");
			functionBody.addLine("response = JSON.parse(xhttpc.responseText);");
			functionBody.addLine("} else {");
			functionBody.addLine("response = {'status':%s.%s, 'error-message':xhttpc.statusText};".formatted(statusClass.getSimpleName(), statusMarkerDefault.name()));
			functionBody.addLine("}");
			functionBody.addLine();
			functionBody.addLine("if (callbacks[\"complete\"]) {");
			functionBody.addLine("callbacks[\"complete\"](response);");
			functionBody.addLine("}");
			functionBody.addLine();
			functionBody.addLine("protOkay = (response['status'] === %s.%s);".formatted(statusClass.getSimpleName(), statusMarkerSuccess.name()));
			functionBody.addLine();
			functionBody.addLine("if (protOkay) {");
			
			for (final var method : sessionControls) {
				boolean               callbacks = false;
				final GlassCallback[] functionCallbacks;
				
				functionCallbacks = method.getCallbacks();
				
				for (final var callback : functionCallbacks) {
					if (callback.condition() == GlassCallbackCondition.SUCCESS) {
						callbacks = true;
						
						break;
					}
				}
				
				if (! callbacks) 
					continue;
				
				functionBody.addLine("if (fp.toLowerCase() === \"%s\") {".formatted(method.getControl().toLowerCase()));
				
				for (final var callback : functionCallbacks) {
					if (callback.condition() == GlassCallbackCondition.SUCCESS) {
						functionBody.addLine(callback.method().embed(callback.target()));
					}
				}
				
				functionBody.addLine("}");
				functionBody.addLine();
			}

			functionBody.addLine("if (callbacks[\"success\"]) {");
			functionBody.addLine("callbacks[\"success\"](response);");
			functionBody.addLine("}");
			functionBody.addLine("} else {");

			for (final var method : sessionControls) {
				boolean               callbacks = false;
				final GlassCallback[] functionCallbacks;
				
				functionCallbacks = method.getCallbacks();
				
				for (final var callback : functionCallbacks) {
					if (callback.condition() == GlassCallbackCondition.FAILURE) {
						callbacks = true;
						
						break;
					}
				}
				
				if (! callbacks) 
					continue;
				
				functionBody.addLine("if (fp.toLowerCase() === \"%s\") {".formatted(method.getControl().toLowerCase()));
				
				for (final var callback : functionCallbacks) {
					if (callback.condition() == GlassCallbackCondition.FAILURE) {
						functionBody.addLine(callback.method().embed(callback.target()));
					}
				}
				
				functionBody.addLine("}");
				functionBody.addLine();
			}

			functionBody.addLine("if (callbacks[\"failure\"]) {");
			functionBody.addLine("callbacks[\"failure\"](response);");
			functionBody.addLine("}");
			functionBody.addLine("}");
			functionBody.addLine("}");
			functionBody.addLine("};");
//			functionBody.addLine("xhttpc.open(\"POST\", `%s`, true);".formatted(urlBuilder.buildString()));
			functionBody.addLine("xhttpc.open(\"POST\", `/action?id=${fp}`, true);");
//			functionBody.addLine("xhttpc.withCredentials = true;");
			functionBody.addLine("xhttpc.setRequestHeader(\"%s\", %s());".formatted("Authorization", GlassInterface.GF_AUTH_GET));
			functionBody.addLine("xhttpc.setRequestHeader(\"Content-Type\", \"text/plain; charset=UTF-8\");");
			functionBody.addLine("xhttpc.send(JSON.stringify(request));");

			function = new JSFunction();
			function.addParameter("fp");
			function.addParameter("datax", "null");
			function.addParameter("callbacks", "{}");
			function.setBody(functionBody.build());
			
			this.exportFunctions.put("transact", function);
		}
		
		public void addStatusConstants (){
			final JSEnum enumStatus;

			enumStatus = new JSEnum();
			enumStatus.setName(statusClass.getSimpleName());

			for (final var econ : statusClass.getEnumConstants()) {
				final Enum<?> eval;

				eval = (Enum<?>)(econ);

				enumStatus.addValue(eval.name());
			}

			globals.add(enumStatus);
		}

		public JSCode build (){
			final JSBuilder   builder;
			final JSSingleton singleton;
			final JSBuilder   singletonBody;

			builder = new JSBuilder();
			builder.addLine(GlassCore.load().toJavaScript());
			
			for (final var block : globalBlocks) {
				builder.addLine(block.toJavaScript());

				builder.addLine();
			}
			
			for (final var obj : globals) {
				builder.add(obj);

				builder.addLine();
			}

			// ...
			singletonBody = new JSBuilder();

			for (final var obj : internalVariables) {
				singletonBody.add(obj);
			}

			if (!singletonBody.isEmpty() && internalFunctions.isEmpty()) {
				singletonBody.addLine();
			}

			for (final var obj : internalFunctions) {
				singletonBody.addLine();
				singletonBody.add(obj);
			}

			singletonBody.addLine();
			singletonBody.addLine("document.addEventListener('DOMContentLoaded', %s, false);".formatted(GlassInterface.GF_STATE_INIT));

			singleton = new JSSingleton();
			singleton.setBody(singletonBody.build());
			singleton.setName(sessionFactory.getProductionClass().getSimpleName());

			for (final var obj : exportFunctions.keySet()) {
				singleton.addExport(obj, exportFunctions.get(obj));
			}
			
			builder.add(singleton);
			
			return builder.build();
		}

		public void createAuth (){
			final JSFunction functionCheck;
			final JSBuilder  functionCheckBody;
			final JSFunction functionDelete;
			final JSBuilder  functionDeleteBody;
			final JSFunction functionInitializeTimeout;
			final JSBuilder  functionInitializeTimeoutBody;
			final JSFunction functionRetrieve;
			final JSBuilder  functionRetrieveBody;
			final JSFunction functionStore;
			final JSBuilder  functionStoreBody;
			final JSFunction functionUpdateTimeout;
			final JSBuilder  functionUpdateTimeoutBody;
			final JSVariable timeoutHandler;

			// ...
			functionCheckBody = new JSBuilder();
			functionCheckBody.addLine("return (null !== %s());".formatted(GlassInterface.GF_AUTH_GET));

			functionCheck = new JSFunction();
			functionCheck.setBody(functionCheckBody.build());
			functionCheck.setName(GlassInterface.GF_AUTH_CHECK);

			internalFunctions.add(functionCheck);

			// ...
			functionDeleteBody = new JSBuilder();
			functionDeleteBody.addLine("__document__.clearCookie(\"%s\");".formatted(authToken));

			functionDelete = new JSFunction();
			functionDelete.setBody(functionDeleteBody.build());
			functionDelete.setName(GlassInterface.GF_AUTH_CLEAR);

			internalFunctions.add(functionDelete);

			// ...
			functionStoreBody = new JSBuilder();
			functionStoreBody.addLine("__document__.setCookie(\"%s\", token, %d);".formatted(authToken, sessionTimeout));

			functionStore = new JSFunction();
			functionStore.addParameter("token");
			functionStore.setBody(functionStoreBody.build());
			functionStore.setName(GlassInterface.GF_AUTH_INIT);

			internalFunctions.add(functionStore);

			// ...
			functionRetrieveBody = new JSBuilder();
			functionRetrieveBody.addLine("return __document__.lookupCookie(\"%s\");".formatted(authToken));

			functionRetrieve = new JSFunction();
			functionRetrieve.setBody(functionRetrieveBody.build());
			functionRetrieve.setName(GlassInterface.GF_AUTH_GET);

			internalFunctions.add(functionRetrieve);

			// ...
			timeoutHandler = new JSVariable();
			timeoutHandler.setName(GlassInterface.GV_TIMEOUT_HANDLER);
			timeoutHandler.setType(JSType.OBJECT);
			timeoutHandler.setValue(null);

			internalVariables.add(timeoutHandler);

			// ...
			functionInitializeTimeoutBody = new JSBuilder();
			functionInitializeTimeoutBody.addLine("document.addEventListener(\"keyup\", (e) => %s());".formatted(GlassInterface.GF_SESSION_TIMEOUT_UPDATE));
			functionInitializeTimeoutBody.addLine("document.addEventListener(\"mousemove\", (e) => %s());".formatted(GlassInterface.GF_SESSION_TIMEOUT_UPDATE));
			functionInitializeTimeoutBody.addLine("document.addEventListener(\"scroll\", (e) => %s());".formatted(GlassInterface.GF_SESSION_TIMEOUT_UPDATE));
			functionInitializeTimeoutBody.addLine();
			functionInitializeTimeoutBody.addLine("%s();".formatted(GlassInterface.GF_SESSION_TIMEOUT_UPDATE));

			functionInitializeTimeout = new JSFunction();
			functionInitializeTimeout.setBody(functionInitializeTimeoutBody.build());
			functionInitializeTimeout.setName(GlassInterface.GF_SESSION_TIMEOUT_INIT);

			internalFunctions.add(functionInitializeTimeout);

			// ...
			functionUpdateTimeoutBody = new JSBuilder();
			functionUpdateTimeoutBody.addLine("%s(%s());".formatted(GlassInterface.GF_AUTH_INIT, GlassInterface.GF_AUTH_GET));
			functionUpdateTimeoutBody.addLine();
			functionUpdateTimeoutBody.addLine("if (%s) {".formatted(GlassInterface.GV_TIMEOUT_HANDLER));
			functionUpdateTimeoutBody.addLine("clearTimeout(%s);".formatted(GlassInterface.GV_TIMEOUT_HANDLER));
			functionUpdateTimeoutBody.addLine();
			functionUpdateTimeoutBody.addLine("%s = null;".formatted(GlassInterface.GV_TIMEOUT_HANDLER));
			functionUpdateTimeoutBody.addLine("}");
			functionUpdateTimeoutBody.addLine();
			functionUpdateTimeoutBody.addLine("%s = setTimeout(() => {".formatted(GlassInterface.GV_TIMEOUT_HANDLER));
			functionUpdateTimeoutBody.addLine("%s();".formatted(GlassInterface.GF_AUTH_CLEAR));
			functionUpdateTimeoutBody.addLine();
			functionUpdateTimeoutBody.addLine("__window__.prompt(null, \"%s\", {".formatted("Your session has timed out."));
			functionUpdateTimeoutBody.addLine("\"Ok\": function() {");
			functionUpdateTimeoutBody.addLine("__window__.travelTo(\"%s\");".formatted(RESET_SITE));
			functionUpdateTimeoutBody.addLine("}");
			functionUpdateTimeoutBody.addLine("}, \"Ok\");");
			functionUpdateTimeoutBody.addLine("}, %d);".formatted(sessionTimeout));
			functionUpdateTimeout = new JSFunction();
			functionUpdateTimeout.setBody(functionUpdateTimeoutBody.build());
			functionUpdateTimeout.setName(GlassInterface.GF_SESSION_TIMEOUT_UPDATE);

			internalFunctions.add(functionUpdateTimeout);
		}

		public void createEventHandler (){
			final JSVariable connected;
			final JSFunction function;
			final JSBuilder  functionBody;
			final JSVariable stream;
			final JSVariable statusChecker;
			final JSVariable statusChecks;
			
			connected = new JSVariable();
			connected.setName("connected");
			connected.setType(JSType.OBJECT);
			connected.setValue("false");
			
			statusChecker = new JSVariable();
			statusChecker.setName("statusChecker");
			statusChecker.setType(JSType.OBJECT);
			statusChecker.setValue(null);
			
			statusChecks = new JSVariable();
			statusChecks.setName("statusChecks");
			statusChecks.setType(JSType.NUMBER);
			statusChecks.setValue("0");
			
			stream = new JSVariable();
			stream.setName(GlassInterface.GV_EVENT_STREAM);
			stream.setType(JSType.OBJECT);
			stream.setValue(null);

			internalVariables.add(stream);

			functionBody = new JSBuilder();
			functionBody.addLine("%s".formatted(connected.toJavaScript()));
			functionBody.addLine("%s".formatted(statusChecker.toJavaScript()));
			functionBody.addLine("%s".formatted(statusChecks.toJavaScript()));
			functionBody.addLine();
			functionBody.addLine("%s = new EventSource(\"%s\");".formatted(GlassInterface.GV_EVENT_STREAM, new URLBuilder().setPath("/event").setQuery("id=*").buildString()));
			functionBody.addLine("%s.onerror = (e) => {".formatted(GlassInterface.GV_EVENT_STREAM));
			functionBody.addLine("if (navigator.onLine) {");
			
			functionBody.addLine("} else {");
			functionBody.addLine("__document__.reload();");
			functionBody.addLine("}");
			functionBody.addLine();
//			functionBody.addLine("if (%s) {".formatted(connected.getName()));
			//functionBody.addLine("__document__.reload();");
			functionBody.addLine("};");
//			functionBody.addLine("}");
			
			for (final var eventClass : Reflection.collectSubclassesOf(eventBaseClass)) {
				final GlassCallback[]  callbacks;
				final GlassEventMarkup markup;
				
				markup = eventClass.getAnnotation(GlassEventMarkup.class);
				
				if (eventClass.isAnnotationPresent(GlassCallbacks.class)) 
					callbacks = eventClass.getAnnotation(GlassCallbacks.class).value();
				else {
					callbacks = new GlassCallback[]{eventClass.getAnnotation(GlassCallback.class)};
				}
				
				if ((callbacks.length == 0) || ((callbacks.length == 1) && (null == callbacks[0]))) 
					continue;
				
				functionBody.addLine("%s.addEventListener(%s.%s, (event) => {".formatted(GlassInterface.GV_EVENT_STREAM, eventBaseClass.getSimpleName(), eventClass.getSimpleName()));
				
				for (var i = 0; i < callbacks.length; i++) {
					final GlassCallback callback;
					
					callback = callbacks[i];
					
					if (callback.condition() != GlassCallbackCondition.NONE) {
						throw new Error();
					}
					
					functionBody.addLine(callback.method().embed(callback.target()));
					
					if ((i + 1) < callbacks.length) {
						functionBody.addLine();
					}
				}
				
				functionBody.addLine("});");
			}
			
			functionBody.addLine();
			functionBody.addLine("window.onbeforeunload = () => {");
			functionBody.addLine("%s.close();".formatted(GlassInterface.GV_EVENT_STREAM));
			functionBody.addLine("};");
			
			functionBody.addLine();
			functionBody.addLine("__document__.signalReady();");
			
			/*
			functionBody.addLine("if (true) __document__.signalReady(); else");
			functionBody.addLine("%s = setInterval(() => {".formatted(statusChecker.getName()));
			functionBody.addLine("if (%s < 100) {".formatted(statusChecks.getName()));
			functionBody.addLine("%s++;".formatted(statusChecks.getName()));
			functionBody.addLine();
			functionBody.addLine("if (%s.readyState == 1) {".formatted(GlassInterface.GV_EVENT_STREAM));
			functionBody.addLine("%s = true;".formatted(connected.getName()));
			functionBody.addLine();
			functionBody.addLine("clearInterval(%s);".formatted(statusChecker.getName()));
			functionBody.addLine();
			functionBody.addLine("__document__.signalReady();");
			functionBody.addLine("}");
			functionBody.addLine("} else {");
			functionBody.addLine("if (navigator.onLine) {");
			functionBody.addLine("%s();".formatted("clearAuth"));
			functionBody.addLine("}");
			functionBody.addLine();
			functionBody.addLine("clearInterval(%s);".formatted(statusChecker.getName()));
			functionBody.addLine();
			functionBody.addLine("if (initial && %s()) {".formatted(GlassInterface.GF_AUTH_CHECK));
			functionBody.addLine("%s(false);".formatted(GlassInterface.GF_EVENT_INIT));
			functionBody.addLine("} else {");
			functionBody.addLine("__document__.reload();");
			functionBody.addLine("}");
			functionBody.addLine("}");
			functionBody.addLine("}, 100)");
			*/
			
			function = new JSFunction();
			function.addParameter("initial", "true");
			function.setBody(functionBody.build());
			function.setName(GlassInterface.GF_EVENT_INIT);
			
			internalFunctions.add(function);
			
			this.createEventManipulationMethods();
		}
		
		private void createEventManipulationMethods (){
			final JSFunction functionAdd;
			final JSBuilder  functionAddBody;
			final JSFunction functionRemove;
			final JSBuilder  functionRemoveBody;
			
			functionAddBody = new JSBuilder();
			functionAddBody.addLine("%s.addEventListener(eventId, (e) => handler(JSON.parse(e.data)));".formatted(GlassInterface.GV_EVENT_STREAM));
			
			functionAdd = new JSFunction();
			functionAdd.addParameter("eventId");
			functionAdd.addParameter("handler");
			functionAdd.setBody(functionAddBody.build());
			
			this.exportFunctions.put("addEventListener", functionAdd);
			
			/*
			functionRemoveBody = new JSBuilder();
			functionRemoveBody.addLine("%s.removeEventListener(handler);".formatted(GlassInterface.GV_EVENT_STREAM));
			
			functionRemove = new JSFunction();
			functionRemove.addParameter("handler");
			functionRemove.setBody(functionRemoveBody.build());
			
			this.exportFunctions.put("removeEventListener", functionRemove);
			*/
		}
		
		public void createLoader (){
			final JSFunction  loader;
			final JSBuilder   loaderBody;

			loaderBody = new JSBuilder();
			loaderBody.addLine("var czRequireAuth = false;");
			loaderBody.addLine();
			loaderBody.addLine("czRequireAuth = __document__.lookupMeta(\"%s\", \"boolean\");".formatted(GMT_REQUIRE_AUTH));
			loaderBody.addLine();
			loaderBody.addLine("if (czRequireAuth && !%s()) {".formatted(GlassInterface.GF_AUTH_CHECK, GlassInterface.GF_AUTH_CHECK));
			loaderBody.addLine("__window__.travelTo(\"%s\");".formatted(RESET_SITE));
			loaderBody.addLine("}");
			loaderBody.addLine();
			loaderBody.addLine("if (czRequireAuth && %s()) {".formatted(GlassInterface.GF_AUTH_CHECK));
			loaderBody.addLine("%s();".formatted(GlassInterface.GF_EVENT_INIT));
			loaderBody.addLine();
			loaderBody.addLine("%s();".formatted(GlassInterface.GF_SESSION_TIMEOUT_INIT));
			loaderBody.addLine("} else {");
			loaderBody.addLine("__document__.signalReady();");
			loaderBody.addLine("}");

			loader = new JSFunction();
			loader.setBody(loaderBody.build());
			loader.setName(GlassInterface.GF_STATE_INIT);

			internalFunctions.add(loader);
		}

		public void organize (){
			this.internalFunctions.sort((o1, o2) -> o1.getName().compareTo(o2.getName()));
			this.internalVariables.sort((o1, o2) -> o1.getName().compareTo(o2.getName()));
		}
	}
}