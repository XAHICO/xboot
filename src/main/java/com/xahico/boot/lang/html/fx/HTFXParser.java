/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.lang.html.fx;

import com.xahico.boot.io.Source;
import com.xahico.boot.lang.html.HTMLDocument;
import com.xahico.boot.lang.html.HTMLElement;
import com.xahico.boot.lang.html.HTMLException;
import com.xahico.boot.lang.html.HTMLNode;
import com.xahico.boot.lang.html.HTMLParser;
import com.xahico.boot.lang.html.HTMLSpecialElement;
import com.xahico.boot.lang.html.HTMLStandardType;
import com.xahico.boot.lang.html.HTMLUtilities;
import com.xahico.boot.lang.javascript.JSBuilder;
import com.xahico.boot.lang.javascript.JSCode;
import com.xahico.boot.lang.jsox.JSOXUtilities;
import com.xahico.boot.util.CollectionUtilities;
import com.xahico.boot.util.StringUtilities;
import java.io.IOException;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class HTFXParser {
	private static final String CLASS_REFERENCE_SELF = "this";
	
	private static final String FX_ATTRIB_CLASS = "fxclass";
	private static final String FX_ATTRIB_INJECTION_ID = "injectionId";
	private static final String FX_ATTRIB_LINKING = "dcaLink";
	private static final String FX_CLASS_PLATFORM = "HTFXPlatform";
	private static final String FX_CLASS_ROOT = "HTFXElement";
	private static final String FX_GLOBAL_CLASSES = "__FXClassList__";
	private static final String FX_GLOBAL_DOCUMENT = "HTFXDocument";
	private static final String FX_GLOBAL_ELEMENT = "HTFXElement";
	private static final String FX_GLOBAL_PLATFORM = "HTFXPlatform";
	private static final String FX_OPERAND_IMPORT = "import";
	private static final String FX_PREFIX_INJECT = "fx";
	private static final String FX_PREFIX_INTERNAL = "$__";
	private static final String FX_PREFIX_REFER = ":";
	private static final String FX_SUFFIX_APPEND = ":after";
	private static final String FX_SUFFIX_PREPEND = ":before";
	
	
	
	private static boolean containsDocumentClassAttributeSelfReference (final String value){
		for (var i = 0; i < value.length(); i++) {
			final char c;

			if (i < (CLASS_REFERENCE_SELF.length() - 1)) {
				continue;
			}

			c = value.charAt(i);

			if (c != CLASS_REFERENCE_SELF.charAt(CLASS_REFERENCE_SELF.length() - 1)) {
				continue;
			}
			
			if (value.regionMatches(false, (i - (CLASS_REFERENCE_SELF.length() - 1)), CLASS_REFERENCE_SELF, 0, CLASS_REFERENCE_SELF.length())) {
				if (((i + 1) < value.length()) && Character.isAlphabetic(value.charAt(i + 1))) {
					continue;
				}
				
				return true;
			}
		}
		
		return false;
	}
	
	public static String linkDocumentClassAttributeToSelf (final String value, final String self){
		final StringBuilder sb;
		
		sb = new StringBuilder();
		
		for (var i = 0; i < value.length(); i++) {
			final char c;
			
			c = value.charAt(i);
			
			if ((i >= (CLASS_REFERENCE_SELF.length() - 1)) && (c == CLASS_REFERENCE_SELF.charAt(CLASS_REFERENCE_SELF.length() - 1)) && value.regionMatches(false, (i - (CLASS_REFERENCE_SELF.length() - 1)), CLASS_REFERENCE_SELF, 0, CLASS_REFERENCE_SELF.length())) {
				if (((i + 1) < value.length()) && Character.isAlphabetic(value.charAt(i + 1))) {
					sb.append(c);
					
					continue;
				}
				
				sb.delete((sb.length() - (CLASS_REFERENCE_SELF.length() - 1)), sb.length());
				sb.append(self);
			} else {
				sb.append(c);
			}
		}
		
		return sb.toString();
	}
	
	private static void linkDocumentClassNode (final HTMLNode node){
		final Map<String, String> attribMods;
		final Iterator<String>    it;

		attribMods = new HashMap<>();

		it = node.getAttributes().keySet().iterator();

		while (it.hasNext()) {
			final String attribKey;
			final String attribVal;

			attribKey = it.next();
			attribVal = node.getAttribute(attribKey);

			if (containsDocumentClassAttributeSelfReference(attribVal)) {
				it.remove();
				
				attribMods.put(attribKey, linkDocumentClassAttributeToSelf(attribVal, "{{__self__}}"));
			}
		}
		
		if (! attribMods.isEmpty()) {
			node.addAttribute(FX_ATTRIB_LINKING, CollectionUtilities.transformStringHashSet(attribMods.keySet(), (element) -> StringUtilities.quote(element).replace("\"", "&quot;")).toString());

			node.addAttributes(attribMods);
		}
	}
	
	private static String loadEmbeddedResource (final String resourceName){
		try (final var stream = HTFXParser.class.getResourceAsStream(resourceName)) {
			return new String(stream.readAllBytes()).strip();
		} catch (final IOException ex) {
			throw new Error(ex);
		}
	}
	
	
	
	private Map<String, HTFXClassLoader> cache = null;
	private Source                source = null;
	private HTFXTranslator        translatorStyle = (data) -> data;
	
	private String                classDirectory = null;
	private final List<HTFXClass> classList = new LinkedList<>();
	private final String          createElement = (FX_PREFIX_INTERNAL + StringUtilities.randomAlphabetic(32));
	
	private final AtomicInteger   counter = new AtomicInteger(0);
	private HTMLDocument          document = null;
	private HTMLNode              documentBody = null;
	private HTMLNode              documentHead = null;
	
	
	
	public HTFXParser (){
		super();
	}
	
	
	
	private JSCode buildClassExternal (final HTFXClass classObject){
		final JSBuilder classBuilder;
		
		classBuilder = new JSBuilder();
		classBuilder.addLine("class %s extends %s {".formatted(classObject.getClassName(), classNameAsInternal(classObject.getClassName())));
		
		if (classObject.hasController()) {
			classBuilder.addLine(classObject.getController());
		} else {
			classBuilder.addLine("constructor (element, subclass = null){");
			classBuilder.addLine("super(element, subclass);");
			classBuilder.addLine("}");
		}
		
		classBuilder.addLine("}");
		classBuilder.addLine();
		classBuilder.addLine("%s[\"%s\"] = %s;".formatted(FX_GLOBAL_CLASSES, classObject.getClassName(), classObject.getClassName()));
		classBuilder.addLine();
		
		return classBuilder.build();
	}
	
	private JSCode buildClassInternal (final HTFXClass classObject){
		final JSBuilder    classBuilder;
		final List<String> propertyList;
		
		classBuilder = new JSBuilder();
		classBuilder.addLine("class %s extends %s {".formatted(classNameAsInternal(classObject.getClassName()), FX_CLASS_ROOT));
		
		propertyList = new LinkedList<>();
		
		classObject.getDocument().walk((element) -> {
			if (element instanceof HTMLNode) {
				final HTMLNode node;
				final String   prop;
				
				node = (HTMLNode)(element);
				
				linkDocumentClassNode(node);
				
				prop = node.getAttribute("property");
				
				if (null != prop) {
					classBuilder.addLine("%s = null;".formatted(JSOXUtilities.translateJSONFieldToJavaField(prop)));
					
					propertyList.add(prop);
				}
			}
		}, -1);
		
		if (! propertyList.isEmpty()) {
			classBuilder.addLine();
			classBuilder.addLine();
		}
		
		injectClasses(classObject.getDocument(), false);
		
		classBuilder.addLine("constructor (element = %s(`%s`), subclass = null){".formatted(createElement, classObject.getBody()));
		classBuilder.addLine("super(element, subclass);");
		classBuilder.addLine();
		classBuilder.addLine("this.registerMembers();");
		
		if (! propertyList.isEmpty()) {
			classBuilder.addLine();
			
			propertyList.forEach(prop -> classBuilder.addLine("this.%s = this.getElementByPropertyId(\"%s\");".formatted(JSOXUtilities.translateJSONFieldToJavaField(prop), prop)));
		}
		
		classBuilder.addLine("}");
		classBuilder.addLine("}");
		
		return classBuilder.build();
	}
	
	private void buildClassSystem (){
		final JSBuilder scriptBuilder;
		
		scriptBuilder = new JSBuilder();
		scriptBuilder.addLine("const %s = {};".formatted(FX_GLOBAL_DOCUMENT));
		scriptBuilder.addLine("const %s = {};".formatted(FX_GLOBAL_CLASSES));
		scriptBuilder.addLine();
		scriptBuilder.addLine("function %s(html) {".formatted(createElement));
		scriptBuilder.addLine("var element;");
		scriptBuilder.addLine();
		scriptBuilder.addLine("element = document.createElement(\"template\");");
		scriptBuilder.addLine("element.innerHTML = html;");
		scriptBuilder.addLine();
		scriptBuilder.addLine("return element.content.children[0];");
		scriptBuilder.addLine("}");
		scriptBuilder.addLine();
		scriptBuilder.addLine(loadEmbeddedResource(FX_CLASS_PLATFORM + ".js"));
		scriptBuilder.addLine(loadEmbeddedResource(FX_CLASS_ROOT + ".js"));
		scriptBuilder.addLine();
		
		for (final var classObject : classList) {
			scriptBuilder.addLine(buildClassInternal(classObject).toJavaScript());
			scriptBuilder.addLine();
			scriptBuilder.addLine(buildClassExternal(classObject).toJavaScript());
		}
		
		documentHead.getChildren().add(0, HTMLUtilities.createScript(scriptBuilder.buildString()));
	}
	
	private String classNameAsInternal (final String className){
		return (FX_PREFIX_INTERNAL + className);
	}
	
	private void injectClasses (final HTMLDocument document, final boolean global){
		final Deque<String>       declarationList;
		final Map<String, String> globals;
		final Deque<String>       injectionList;
		final JSBuilder           scriptBuilder;
		
		declarationList = new LinkedList<>();
		
		injectionList = new LinkedList<>();
		
		globals = new HashMap<>();
		
		document.walk((element) -> {
			if (element instanceof HTMLNode) {
				final String    className;
				final HTFXClass classObject;
				final String    declarationId;
				final int       injectionId;
				final String    locationDefault;
				final HTMLNode  node;
				final HTMLNode  nodeNew;
				
				node = (HTMLNode)(element);
				
				if (node.getName().startsWith(FX_PREFIX_INJECT)) {
					className = node.getName().substring(FX_PREFIX_INJECT.length() + 1);
					
					classObject = CollectionUtilities.seek(classList, (__) -> __.getClassName().equalsIgnoreCase(className), false);
					
					if (null == classObject) {
						throw new Error("No such class: %s".formatted(className));
					}
					
					nodeNew = classObject.newInstance();
					nodeNew.addAttributes(node.getAttributes());
					nodeNew.setAttribute(FX_ATTRIB_CLASS, className);
					
					if (global) {
						injectionId = counter.incrementAndGet();
						
						nodeNew.setAttribute(FX_ATTRIB_INJECTION_ID, Integer.toString(injectionId));
					} else {
						injectionId = -1;
					}
					
					//injectClasses(nodeNew);
					
					locationDefault = nodeNew.getAttribute("defaultproperty");
					
					for (final var childElement : node.getChildren()) {
						final String   location;
						final HTMLNode locationNode;
						final boolean  appendMode;
						final boolean  prependMode;
						
						if ((childElement instanceof HTMLNode) && ((HTMLNode)childElement).getName().startsWith(FX_PREFIX_REFER)) {
							final HTMLNode childNode;
							
							childNode = (HTMLNode)(childElement);
							
							appendMode = childNode.getName().endsWith(FX_SUFFIX_APPEND);
							prependMode = childNode.getName().endsWith(FX_SUFFIX_PREPEND);
							
							location = childNode.getName().substring(FX_PREFIX_REFER.length(), (appendMode ? (childNode.getName().length() - FX_SUFFIX_APPEND.length()) : (prependMode ? (childNode.getName().length() - FX_SUFFIX_PREPEND.length()) : childNode.getName().length())));
							locationNode = (HTMLNode) nodeNew.seek((e) -> ((e instanceof HTMLNode) && location.equalsIgnoreCase(((HTMLNode)e).getAttribute("property"))), -1);
							
							if (null != locationNode) {
								final String elementClassList;
								
								elementClassList = locationNode.getAttribute("class");
								
								locationNode.setAttributes(childNode.getAttributes());
								
								if (null != elementClassList) {
									final String classListApply;
									
									classListApply = childNode.getAttribute("class");
									
									if (null != classListApply) {
										locationNode.setAttribute("class", (elementClassList + " " + classListApply));
									} else {
										locationNode.setAttribute("class", elementClassList);
									}
								}
								
								if (childNode.hasChildren()) {
									if (!appendMode && !prependMode) {
										locationNode.removeAllChildren();
									}
									
									if (prependMode) {
										locationNode.getChildren().addAll(0, childNode.getChildren());
									} else {
										locationNode.addChildren(childNode.getChildren());
									}
								}
							}
						} else if (null != locationDefault) {
							location = locationDefault;
							locationNode = (HTMLNode) nodeNew.seek((e) -> ((e instanceof HTMLNode) && location.equalsIgnoreCase(((HTMLNode)e).getAttribute("property"))), -1);
							
							if (null != locationNode) {
								locationNode.removeAllChildren();
								locationNode.addChild(childElement);
							}
						} else {
							locationNode = nodeNew;
							locationNode.removeAllChildren();
							locationNode.addChild(childElement);
						}
					}
					
					node.swap(nodeNew);
					
					if (global) {
						declarationId = (FX_PREFIX_INTERNAL + "IJE" + Integer.toString(injectionId));
						
						declarationList.addFirst("var %s = null;".formatted(declarationId));

						injectionList.addFirst("%s = new %s(document.querySelector(\"[%s=\\\"%d\\\"]\"));".formatted(declarationId, classObject.getClassName(), FX_ATTRIB_INJECTION_ID, injectionId));
						
						if (null != node.getAttribute("id")) {
							globals.put(node.getAttribute("id"), declarationId);
						}
					}
				}
			}
		}, -1);
		
		if (global) {
			scriptBuilder = new JSBuilder();
			
			for (final var declaration : declarationList) {
				scriptBuilder.addLine(declaration);
			}
			
			scriptBuilder.addLine();
			scriptBuilder.addLine("document.addEventListener(\"DOMContentLoaded\", (e) => {");
			
			for (final var injection : injectionList) {
				scriptBuilder.addLine(injection);
			}
			
			scriptBuilder.addLine();
			
			for (final var globalKey : globals.keySet()) {
				scriptBuilder.addLine("%s[%s.translateProperty(\"%s\")] = %s;".formatted(FX_GLOBAL_DOCUMENT, FX_GLOBAL_ELEMENT, globalKey, globals.get(globalKey)));
			}
			
			scriptBuilder.addLine();
			scriptBuilder.addLine("document.querySelectorAll(\"[id]\").forEach((element) => {");
			scriptBuilder.addLine("if (!element.getAttribute(\"%s\")) {".formatted(FX_ATTRIB_CLASS));
			scriptBuilder.addLine("%s[%s.translateProperty(element.getAttribute(\"id\"))] = element;".formatted(FX_GLOBAL_DOCUMENT, FX_GLOBAL_ELEMENT));
			scriptBuilder.addLine("}");
			scriptBuilder.addLine("});");
			scriptBuilder.addLine();
			
			scriptBuilder.addLine("%s.signalReady();".formatted(FX_GLOBAL_PLATFORM));
			scriptBuilder.addLine("});");
			
			documentHead.addChild(HTMLUtilities.createScript(scriptBuilder.buildString()));
		}
	}
	
	private HTFXClass loadClass (final String classPath) throws IOException {
		final HTFXClass       classLoaded;
		final HTFXClassLoader classLoader;
		final List<HTMLNode>  collection;
		
		if (null == cache) {
			classLoader = new HTFXClassLoader(this.classDirectory, classPath);
		} else {
			classLoader = cache.computeIfAbsent(classPath, (__) -> new HTFXClassLoader(this.classDirectory, classPath));
		}
		
		collection = new LinkedList<>();
		
		classLoaded = classLoader.load();
		classLoaded.getDocument().walk((element) -> {
			if (element instanceof HTMLNode) {
				final HTMLNode node;
				
				node = (HTMLNode)(element);
				
				linkDocumentClassNode(node);
				
				if (node.isType(HTMLStandardType.SCRIPT) || node.isType(HTMLStandardType.STYLE) || node.getName().equalsIgnoreCase("link")) {
					collection.add(node);
				}
			}
		}, Integer.MAX_VALUE);
		
		for (final var node : collection) {
			node.unlink();
			
			documentHead.addChild(node);
		}
		
		return classLoaded;
	}
	
	private void loadClasses (final HTMLDocument document){
		final Iterator<HTMLElement> it;
		
		it = document.getChildren().iterator();
		
		while (it.hasNext()) {
			final HTMLElement element;
			
			element = it.next();
			
			if (element instanceof HTMLSpecialElement) {
				final HTFXClass classObject;
				final String    classPath;
				final int       delimiter;
				
				delimiter = element.getContent().indexOf(' ');
				
				if (delimiter == -1) {
					throw new Error("Invalid import '%s'".formatted(element.getContent()));
				}
				
				if (! element.getContent().regionMatches(true, 0, FX_OPERAND_IMPORT, 0, FX_OPERAND_IMPORT.length())) {
					throw new Error("Invalid import '%s'".formatted(element.getContent()));
				}
				
				try {
					classPath = element.getContent().substring(delimiter + 1).strip();
					
					classObject = loadClass(classPath);
					
					if (null == CollectionUtilities.seek(classList, (__) -> classPath.equalsIgnoreCase(__.getClassPath()), false)) {
						classList.add(classObject);
					}
					
					loadClasses(classObject.getDocument());
				} catch (final IOException ex) {
					throw new Error(document.toHTMLString(), ex);
				}
				
				it.remove();
			}
		}
	}
	
	public HTMLDocument parse () throws HTMLException, IOException {
		final HTMLParser documentParser;
		
		// Parse the document, initially as plain HTML
		documentParser = new HTMLParser();
		documentParser.setTolerateGarbage(true);
		documentParser.setSource(this.source);
		
		document = documentParser.parse();
		
		documentHead = document.lookupFirst(HTMLStandardType.HEAD, -1);
		
		if (null == documentHead) {
			throw new Error("Document without head");
		}
		
		documentBody = document.lookupFirst(HTMLStandardType.BODY, -1);
		
		if (null == documentBody) {
			throw new Error("Document without body");
		}
		
		// Load classes
		classList.clear();
		
		loadClasses(document);
		
		injectClasses(document, true);
		
		for (final var classObject : classList) {
			documentHead.getChildren().add(0, new HTMLNode(HTMLStandardType.STYLE).setContent(classObject.getStyle()));
		}
		
		buildClassSystem();
		
		document.removeSpecialElements();
		
		return document;
	}
	
	public void setCache (final Map<String, ?> cache){
		this.cache = (Map<String, HTFXClassLoader>)(cache);
	}
	
	public void setClassDirectory (final String directoryPath){
		this.classDirectory = directoryPath;
	}
	
	public void setSource (final Source source){
		this.source = source;
	}
	
	public void setStyleTranslator (final HTFXTranslator translator){
		this.translatorStyle = translator;
	}
}