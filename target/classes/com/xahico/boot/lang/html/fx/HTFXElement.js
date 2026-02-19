

var __HTFX_ABYSS = null; // object oblivion
var __HTFX_GLOBAL_ELEMENT_COUNT = 0;

window.onload = () => {
	if (! __HTFX_ABYSS) {
		__HTFX_ABYSS = document.createElement('div');
		__HTFX_ABYSS.style.display = 'none';
		
		document.body.appendChild(__HTFX_ABYSS);
	}
};

class HTFXElement {
	static claimLinkage (object, element){
		const links = JSON.parse(element.getAttribute("dcaLink"));
		
		element.setAttribute("dcaLinkOwner", object.objectId);
		
		for (const link of links) {
			element.setAttribute(link, element.getAttribute(link).replace("{{__self__}}", `HTFXElement.lookupObjectById(${object.objectId})`));
		}
	}
	
	static createElement (className){
		const elementClass = __FXClassList__[className];
		
		return new elementClass();
	}
	
	static isObject (element){
		return (element && element.getAttribute("fxclass"));
	}
	
	static isStaticElement (element){
		return (element && element.getAttribute("injectionId"));
	}
	
	static lookupObjectById (objectId){
		const element = document.querySelector(`[objectId="${objectId}"]`);
		
		if (! element) {
			console.log("ERROR: looking for " + objectId);
			console.log(element);
			return null;
		} else {
			return element.fxobject;
		}
	}
	
	static reflect (element){
		const injectionId = element.getAttribute("injectionId");
		
		if (injectionId) {
			return window["$__" + "IJE" + injectionId];
		}
		
		const objectId = element.getAttribute("objectId");
		
		if (objectId) {
			return element.fxobject;
		}
		
		return null;
	}
	
	static translateProperty (property){
		var translation = "";
		
		for (var i = 0; i < property.length; i++) {
			const c = property.charAt(i);

			if (c === '-') 
				continue;

			if ((i > 0) && (property.charAt(i - 1) === '-')) {
				translation += c.toUpperCase();
			} else {
				translation += c;
			}
		}
		
		return translation;
	}
	
	
	objectId = -1;
	rootElement = null;
	super = null;
	userdata = null;
	
	
	constructor (element, subclass = null){
		element.fxobject = this;
		
		this.rootElement = element;
		
		this.super = () => {
			if (this.rootElement.getAttribute("propertyobject")) {
				return HTFXElement.lookupObjectById(this.rootElement.getAttribute("propertyobject"));
			} else {
				return null;
			}
		};
		
		if (this.rootElement.getAttribute("objectId")) {
			this.objectId = parseInt(this.rootElement.getAttribute("objectId"));
		} else {
			if (subclass) {
				this.objectId = parseInt(subclass.rootElement.getAttribute("objectId"));
			} else {
				this.objectId = (__HTFX_GLOBAL_ELEMENT_COUNT++);
			}
			
			this.rootElement.setAttribute("objectId", this.objectId);
		}
		
		this.linkDocumentMethods();
		
		if (this.rootElement.getAttribute("monitorable") === "true") {
			this.monitor = null;
			this.monitors = [];
		}
		
		if (this.rootElement.getAttribute("serializable") === "true") {
			this.serializable = true;
		} else {
			this.serializable = false;
		}
	}
	
	
	attachMonitor (monitor){
		this.monitors.push(monitor);
	}
	
	detachMonitor (monitor){
		this.monitors.splice(this.monitors.indexOf(monitor), 1);
	}
	
	discard (){
		__HTFX_ABYSS.appendChild(this.rootElement);
		__HTFX_ABYSS.innerHTML = "";
	}
	
	getClassName (){
		return this.rootElement.getAttribute("fxclass");
	}
	
	getElementById (elementId){
		return this.rootElement.getElementById(elementId);
	}
	
	getElementByPropertyId (propertyId){
		const element = this.rootElement.querySelector("[propertyObject=\"" + this.objectId + "\"][property=\"" + propertyId + "\" i]");
		
		if (! element) {
			return null;
		}
		
		if (element.getAttribute("injectionId")) {
			return HTFXElement.reflect(element);
		}
		
		const elementClassName = element.getAttribute("fxclass");
		
		if (elementClassName) {
			const elementClass = __FXClassList__[elementClassName];
			
			if (elementClass) {
				return new elementClass(element, this);
			}
		}
		
		return element;
	}
	
	getInjectionId (){
		return this.rootElement.getAttribute("injectionId");
	}
	
	getObjectId (){
		return this.objectId;
	}
	
	invokeMonitors (action, args = null){
		if (this.monitor) {
			if ((typeof this.monitor) === 'function') {
				this.monitor(action, args);
			} else if (this.monitor[action]) {
				this.monitor[action](args);
			}
		}
		
		if (this.monitors) {
			this.monitors.forEach((monitor) => {
				if ((typeof monitor) === 'function') {
					monitor(action, args);
				} else if (monitor[action]) {
					monitor[action](args);
				}
			});
		}
	}
	
	isInjected (){
		return !this.isInstanced();
	}
	
	isInstanced (){
		return !this.getInjectionId();
	}
	
	linkDocumentMethods (origin = this.rootElement){
		if (! HTFXElement.isStaticElement(origin)) {
			for (var ei = 0; ei < origin.childNodes.length; ei++) {
				const element = origin.childNodes[ei];
				
				if (!element || (element.nodeType === Node.TEXT_NODE)) {
					continue;
				}
				
				const elementAttribs = element.attributes;
				
				if (elementAttribs) {
					const elementLinks = [];
					
					for (var ai = 0; ai < elementAttribs.length; ai++) {
						const elementAttrib = elementAttribs[ai];
						
						if (! elementAttrib) {
							continue;
						}
						
						if (elementAttrib.nodeName === "class") {
							continue;
						}
						
						if ((elementAttrib.nodeName === "injectionId") || (elementAttrib.nodeName === "objectId") || (elementAttrib.nodeName === "propertyObject")) {
							continue;
						}
						
						if ((elementAttrib.nodeName === "dcaLink") || (elementAttrib.nodeName === "dcaLinkOwner")) {
							continue;
						}
						
						if ((elementAttrib.nodeName === "defaultproperty") || (elementAttrib.nodeName === "property")) {
							continue;
						}
						
						if ((elementAttrib.nodeName === "monitorable") || (elementAttrib.nodeName === "serializable")) {
							continue;
						}
						
						if (element.getAttribute("fuck")) {
							continue;
						}
						
						if (! elementAttrib.nodeValue.includes("{{__self__}}")) {
							continue;
						}
						
						element.setAttribute("fuck", true);
						
						elementAttrib.nodeValue = elementAttrib.nodeValue.replace(/{{__self__}}/, `HTFXElement.lookupObjectById(${this.objectId})`);

						elementLinks.push(elementAttrib.nodeName);
					}

					if (elementLinks.length > 0) {
						
						element.setAttribute("dcaLink", `[${elementLinks.toString()}]`)
					}
				}
				
				this.linkDocumentMethods(element);
			}
		} else {
			if (origin.getAttribute("dcaLink")) {
				HTFXElement.claimLinkage(this, origin);
			}

			origin.querySelectorAll(":not([dcaLinkOwner])[dcaLink]").forEach((element) => {
				HTFXElement.claimLinkage(this, element);
			});
		}
	}
	
	registerMembers (){
		this.rootElement.querySelectorAll(":not([propertyObject])[property]").forEach((element) => {
			const propertyName = HTFXElement.translateProperty(element.getAttribute("property"));
			
			if ((propertyName in this) && !this[propertyName]) {
				element.setAttribute("propertyObject", this.objectId);
				
				this[propertyName] = element;
			}
		});
	}
	
	supportsMonitor (){
		return this.rootElement.getAttribute("monitorable");
	}
}

