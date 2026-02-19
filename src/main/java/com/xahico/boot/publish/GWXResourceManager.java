/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.xahico.boot.publish;

import com.xahico.boot.reflection.Reflection;
import com.xahico.boot.util.Filter;
import com.xahico.boot.util.OrderedConsumer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class GWXResourceManager {
	private static final GWXPathResolver globalResolver;
	private static final GWXNodeTree     globals = new GWXNodeTree();
	
	
	
	static {
		globalResolver = new GWXPathResolver(globals);
	}
	
	
	
	private static Set<Class<? extends GWXObject>> collectResourcesClassesFor (final Class<? extends GWXSession> serviceClass){
		final Set<Class<? extends GWXObject>> collection;
		
		collection = new HashSet<>();
		
		for (final var jclass : Reflection.collectClassesAnnotatedWith(GWXResource.class)) {
			final GWXResource resource;
			
			resource = jclass.getAnnotation(GWXResource.class);
			
			if (resource.owner() == serviceClass) {
				collection.add((Class<? extends GWXNode>)jclass);
			}
		}
		
		return collection;
	}
	
	public static <T extends GWXObject> T lookupRoot (final Class<T> jclass){
		final GWXResource resource;
		
		resource = jclass.getAnnotation(GWXResource.class);
		
		if (null == resource) {
			return null;
		}
		
		return (T) lookupRoot(resource.root());
	}
	
	public static GWXObject lookupRoot (final String name){
		return globals.lookupRoot(name);
	}
	
	
	
	private final Set<Class<? extends GWXObject>> resourceClassList;
	private final Class<? extends GWXSession>     serviceClass;
	private final GWXServiceProvider              serviceProvider;
	
	
	
	GWXResourceManager (final GWXServiceProvider serviceProvider, final Class<? extends GWXSession> serviceClass){
		super();
		
		this.serviceProvider = serviceProvider;
		this.serviceClass = serviceClass;
		this.resourceClassList = collectResourcesClassesFor(serviceClass);
		
		//System.out.println("Building Global Root");
		
		this.buildRoot(globalResolver, globals, (resource) -> !resource.local(), (object) -> {
			try {
				object.initialize();
			} catch (final Throwable ex) {
				throw new Error(ex);
			}
		});
		
		//System.out.println("Finished building the Global Root");
	}
	
	
	
	public GWXContext buildContext (final GWXSession session, final GWXPath.Pattern pattern, final GWXPath path){
		return GWXContext.buildContext(this, session, pattern, path, null);
	}
	
	public GWXContext buildContext (final GWXSession session, final GWXPath.Pattern pattern, final GWXPath path, final GWXPermission mode){
		return GWXContext.buildContext(this, session, pattern, path, mode);
	}
	
	private void buildRoot (final GWXPathResolver resolver, final GWXNodeTree tree, final Filter<GWXResource> filter, final Consumer<GWXObject> callback){
		final List<Class<? extends GWXObject>> classList;
		final List<GWXObject>                  singletons;

		classList = new ArrayList<>(this.resourceClassList.size());
		classList.addAll(this.resourceClassList);

		singletons = new ArrayList<>(classList.size());

		// FIXED: Add max iterations to prevent infinite loop
		int maxIterations = classList.size() * classList.size(); // Allow multiple passes for dependency resolution
		int iterationCount = 0;

		while (! classList.isEmpty()) {
			final Iterator<Class<? extends GWXObject>> it;
			boolean madeProgress = false; // Track if we made any progress this iteration

			// FIXED: Safety check to prevent infinite loop
			if (++iterationCount > maxIterations) {
				System.err.println("WARNING: buildRoot() exceeded max iterations. Remaining unresolved resources:");
				for (final var unresolved : classList) {
					final GWXResource resource = unresolved.getAnnotation(GWXResource.class);
					System.err.println("  - " + unresolved.getSimpleName() + " (root: " + resource.root() + ")");
				}
				break; // Exit to prevent freeze
			}

			it = classList.iterator();

			while (it.hasNext()) {
				final GWXResource                     resource;
				final Class<? extends GWXObject>      resourceClass;
				final String                          resourceName;
				final String                          resourcePath;
				final Reflection<? extends GWXObject> resourceReflection;
				final String                          resourceRoot;
				final int                             resourceDelimiter;
				GWXObject                             resourceParent;
				final GWXObject                       resourceSingleton;

				resourceParent = tree.rootNode();

				resourceClass = it.next();

				resource = resourceClass.getAnnotation(GWXResource.class);

				resourcePath = resource.root();

				if (resourcePath.isBlank()) {
					it.remove();
					madeProgress = true;
					continue;
				}

				if (! filter.accept(resource)) {
					it.remove();
					madeProgress = true;
					continue;
				}

				resourceDelimiter = resourcePath.lastIndexOf('/');

				if (resourceDelimiter == -1) {
					resourceName = resourcePath;
				} else {
					resourceRoot = resourcePath.substring(0, resourceDelimiter);

					resourceName = resourcePath.substring(resourceDelimiter + 1);

					resourceParent = resolver.lookup(resourceRoot);

					if (null == resourceParent) {
						// FIXED: Don't remove yet - parent might be created in next iteration
						// But we need to check if we're making progress
						continue;
					}
				}

				resourceReflection = Reflection.of(resourceClass);

				resourceSingleton = resourceReflection.newInstanceOrDefault();

				resourceSingleton.name(resourceName);

				resourceSingleton.link(resourceParent);

				tree.root().put(resourcePath, resourceSingleton);

				System.out.println("Registered '%s' = [%s]".formatted(resourceName, resourceSingleton.path()));

				it.remove();
				madeProgress = true;

				singletons.add(resourceSingleton);
			}

			// FIXED: If we didn't make any progress, we're stuck in a dependency cycle
			if (!madeProgress && !classList.isEmpty()) {
				System.err.println("ERROR: Circular dependency or missing parent detected. Unresolved resources:");
				
				for (final var unresolved : classList) {
					final GWXResource resource = unresolved.getAnnotation(GWXResource.class);
					System.err.println("  - " + unresolved.getSimpleName() + " (root: " + resource.root() + ")");
				}
				
				break; // Exit to prevent infinite loop
			}
		}

		for (final var singleton : singletons) {
			callback.accept(singleton);
		}
	}
	
	void injectLocals (final GWXSession session, final GWXNodeTree locals){
		final GWXPathResolver resolver;
		
		resolver = new GWXPathResolver(locals, globals);
		
		this.buildRoot(resolver, locals, (resource) -> resource.local(), (object) -> {
			try {
				object.initialize(session);
			} catch (final Throwable ex) {
				throw new Error(ex);
			}
		});
	}
	
	public void emit (final GWXEvent eventObject){
		eventObject.target.fireEvent(eventObject);
	}
	
	public void execute (final Runnable routine){
		this.serviceProvider.getExecutor().execute(routine);
	}
	
	GWXEventSubscription listen (final GWXSession session, final GWXPath target, final GWXEventAdapter handler){
		return GWXEventSubscription.createSubscription(session, this, target, handler);
	}
	
	public GWXObject lookup (final GWXSession session, final GWXPath path){
		final GWXPathResolver resolver;
		
		if (null == session) {
			resolver = globalResolver;
		} else {
			resolver = new GWXPathResolver(session.locals, globals);
		}
		
		return resolver.lookup(path.withoutExtension());
	}
	
	public GWXObject lookup (final GWXSession session, final String path){
		return this.lookup(session, GWXPath.create(path));
	}
	
	public GWXObject[] resolve (final GWXSession session, final GWXPath path){
		final GWXPathResolver resolver;
		
		if (null == session) {
			resolver = globalResolver;
		} else {
			resolver = new GWXPathResolver(session.locals, globals);
		}
		
		return resolver.resolve(path.withoutExtension());
	}
	
	public void select (final GWXSession session, final GWXPath path, final OrderedConsumer<GWXObject> consumer){
		this.select(session, path.withoutExtension(), consumer);
	}
	
	public void select (final GWXSession session, final String path, final OrderedConsumer<GWXObject> consumer){
		final GWXPathResolver resolver;
		
		if (null == session) {
			resolver = globalResolver;
		} else {
			resolver = new GWXPathResolver(session.locals, globals);
		}
		
		resolver.select(path, (__, next) -> consumer.accept(next));
	}
}