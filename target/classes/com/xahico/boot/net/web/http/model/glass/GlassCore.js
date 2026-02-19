

const __device__ = (function() {
	return {
		isTouchScreen: function() {
			let check = false;
			
			(function(a){if(/(android|bb\d+|meego).+mobile|avantgo|bada\/|blackberry|blazer|compal|elaine|fennec|hiptop|iemobile|ip(hone|od)|iris|kindle|lge |maemo|midp|mmp|mobile.+firefox|netfront|opera m(ob|in)i|palm( os)?|phone|p(ixi|re)\/|plucker|pocket|psp|series(4|6)0|symbian|treo|up\.(browser|link)|vodafone|wap|windows ce|xda|xiino|android|ipad|playbook|silk/i.test(a)||/1207|6310|6590|3gso|4thp|50[1-6]i|770s|802s|a wa|abac|ac(er|oo|s\-)|ai(ko|rn)|al(av|ca|co)|amoi|an(ex|ny|yw)|aptu|ar(ch|go)|as(te|us)|attw|au(di|\-m|r |s )|avan|be(ck|ll|nq)|bi(lb|rd)|bl(ac|az)|br(e|v)w|bumb|bw\-(n|u)|c55\/|capi|ccwa|cdm\-|cell|chtm|cldc|cmd\-|co(mp|nd)|craw|da(it|ll|ng)|dbte|dc\-s|devi|dica|dmob|do(c|p)o|ds(12|\-d)|el(49|ai)|em(l2|ul)|er(ic|k0)|esl8|ez([4-7]0|os|wa|ze)|fetc|fly(\-|_)|g1 u|g560|gene|gf\-5|g\-mo|go(\.w|od)|gr(ad|un)|haie|hcit|hd\-(m|p|t)|hei\-|hi(pt|ta)|hp( i|ip)|hs\-c|ht(c(\-| |_|a|g|p|s|t)|tp)|hu(aw|tc)|i\-(20|go|ma)|i230|iac( |\-|\/)|ibro|idea|ig01|ikom|im1k|inno|ipaq|iris|ja(t|v)a|jbro|jemu|jigs|kddi|keji|kgt( |\/)|klon|kpt |kwc\-|kyo(c|k)|le(no|xi)|lg( g|\/(k|l|u)|50|54|\-[a-w])|libw|lynx|m1\-w|m3ga|m50\/|ma(te|ui|xo)|mc(01|21|ca)|m\-cr|me(rc|ri)|mi(o8|oa|ts)|mmef|mo(01|02|bi|de|do|t(\-| |o|v)|zz)|mt(50|p1|v )|mwbp|mywa|n10[0-2]|n20[2-3]|n30(0|2)|n50(0|2|5)|n7(0(0|1)|10)|ne((c|m)\-|on|tf|wf|wg|wt)|nok(6|i)|nzph|o2im|op(ti|wv)|oran|owg1|p800|pan(a|d|t)|pdxg|pg(13|\-([1-8]|c))|phil|pire|pl(ay|uc)|pn\-2|po(ck|rt|se)|prox|psio|pt\-g|qa\-a|qc(07|12|21|32|60|\-[2-7]|i\-)|qtek|r380|r600|raks|rim9|ro(ve|zo)|s55\/|sa(ge|ma|mm|ms|ny|va)|sc(01|h\-|oo|p\-)|sdk\/|se(c(\-|0|1)|47|mc|nd|ri)|sgh\-|shar|sie(\-|m)|sk\-0|sl(45|id)|sm(al|ar|b3|it|t5)|so(ft|ny)|sp(01|h\-|v\-|v )|sy(01|mb)|t2(18|50)|t6(00|10|18)|ta(gt|lk)|tcl\-|tdg\-|tel(i|m)|tim\-|t\-mo|to(pl|sh)|ts(70|m\-|m3|m5)|tx\-9|up(\.b|g1|si)|utst|v400|v750|veri|vi(rg|te)|vk(40|5[0-3]|\-v)|vm40|voda|vulc|vx(52|53|60|61|70|80|81|83|85|98)|w3c(\-| )|webc|whit|wi(g |nc|nw)|wmlb|wonu|x700|yas\-|your|zeto|zte\-/i.test(a.substr(0,4))) check = true;})(navigator.userAgent||navigator.vendor||window.opera);
			
			return check;
		},
	};
})();


const __document__ = (function() {
	var reloadDisabled = false;
	var loaded = false;
	var loaders = [];
	var popupAttribute = null;
	var popupContainer = null;
	var popupController = null;
	
	return {
		clearCookie: function(cookieName) {
			document.cookie = (cookieName + "=; path=/; expires=Thu, 01 Jan 1970 00:00:01 GMT;");
		},
		
		disableReload (){
			reloadDisabled = true;
		},
		
		fill: function(form, data, ignored = []){
			const { elements } = form;
			
			for (const [key, value] of Object.entries(data)) {
				const input = elements.namedItem(key)
				
				if (input) {
					input.value = value;
				}
			}
		},
		
		getName: function() {
			return "/" + window.location.pathname.split("/").pop();
		},
		
		halt: function(message) {
			window.stop();
			
			if ($.browser.msie) {
				document.execCommand("Stop");
			}
			
			if (null != message) {
				alert(message);
			}
		},
		
		hidePopup: function() {
			if (popupContainer) {
				while (popupContainer.firstChild) {
					popupContainer.removeChild(popupContainer.firstChild);
				}
				
				popupContainer.removeAttribute(popupAttribute);
				
				if (popupController && popupController.hide) {
					popupController.hide();
				}
				
				popupController = null;
				
				return true;
			} else {
				return false;
			}
		},
		
		isCookie: function(cookieName) {
			return (null !== lookupCookie(cookieName));
		},
		
		isPage: function(page){
			return __document__.getName().toLowerCase().endsWith(page.toLowerCase());
		},
		
		lookupCookie: function(cookieName, expectLength = -1) {
			var cookie = null;
			var cookies = document.cookie.split(';');
			
			for (var i = 0; i < cookies.length; i++) {
				var cookie = cookies[i];
				
				while (cookie.charAt(0) === ' ') {
					cookie = cookie.substring(1, cookie.length);
				}
				
				if (cookie.indexOf(cookieName + "=") === 0) {
					cookie = cookie.substring((cookieName.length + 1), cookie.length);
					
					break;
				}
			}
			
			if (null === cookie) {
				return null;
			}
			
			if (cookie.match(/^ *$/) !== null) {
				return null;
			}
			
			if ((expectLength !== -1) && (cookie.length !== expectLength)) {
				return null;
			}
			
			if (cookie === "null") {
				return null;
			}
			
			return cookie;
		},
		
		lookupMeta: function(metaName, type = "string") {
			const metas = document.getElementsByTagName('meta');
			
			for (let i = 0; i < metas.length; i++) {
				if (metas[i].getAttribute('name') === metaName) {
					if (type == "none") {
						return "";
					}
					
					const meta = metas[i].getAttribute('content');
					
					if (type == "boolean") {
						return (meta.toLowerCase() == "true");
					} else if (type == "number") {
						return parseInt(meta);
					} else if (type == "string") {
						return meta;
					} else {
						return meta;
					}
				}
			}
			
			return null;
		},
		
		reload: function() {
			if (! reloadDisabled) {
				__window__.refresh();
			}
		},
		
		ready: function(callback) {
			loaders.push(callback);
			
			if (loaded) {
				callback();
			}
		},
		
		scrollElementIntoView: function(container, element) {
			containerOrders.scrollTo({
				behavior:	"smooth",
				left:		0,
				top:		(element.offsetTop - container.offsetTop),
			});
		},
		
		setCookie: function(cookieName, content, durationMillis, sameSite = true, extra = null){
			var cookie;
			var date = new Date();
			var extraCursor = 0;
			var extraSize = 0;
			
			date.setTime(date.getTime() + durationMillis);
			
			cookie = "";
			cookie += (cookieName + "=" + content + "; ");
			cookie += ("expires=" + date.toUTCString() + "; ");
			
			if (!sameSite) {
				cookie += "sameSite=None; ";
				cookie += "Secure; ";
			}
			
			for (var key in extra) {
				extraSize++;
			}
			
			for (var key in extra) {
				var value = extra[key];
				
				cookie += (key + "=" + value);
				
				if ((extraCursor + 1) < extraSize) {
					cookie += "; ";
					
					extraCursor++;
				}
			}
			
			cookie += "path=/";
			
			document.cookie = cookie;
		},
		
		setPopupOverlay: function(element, attribute) {
			popupContainer = element;
			
			popupAttribute = attribute;
		},
		
		showPopup: function(element, controller = null) {
			if (popupContainer) {
				popupContainer.setAttribute(popupAttribute, true);
				
				popupContainer.appendChild(element);
				
				popupController = controller;
				
				if (popupController && popupController.show) {
					popupController.show();
				}
				
				return true;
			} else {
				return false;
			}
		},
		
		signalReady: function() {
			const bootloader = () => {
				window.addEventListener("pageshow", (e) => {
					const historyTraversal = (e.persisted || ((typeof window.performance != "undefined") && (window.performance.navigation.type === 2)));
					
					if (historyTraversal) {
						__document__.reload();
					}
				});
				
				loaders.forEach((loader) => loader());
				
				window.addEventListener("hashchange", (e) => __window__.loadHash());
				
				if (window.location.hash) {
					__window__.loadHash();
				}
			};
			
			if (! loaded) {
				loaded = true;
				
				HTFXPlatform.run(bootloader);
			}
		}
	};
})();



const __element__ = (function() {
	return {
		removeElementAttribute: function(element, attribute) {
			element.removeAttribute(attribute);
		},
		
		setElementAttribute: function(element, attribute, value = "true") {
			element.setAttribute(attribute, value);
		},
		
		toggleElementAttribute: function(element, attribute, value = "true") {
			if (element.getAttribute(attribute)) {
				element.removeAttribute(attribute);
				
				return false;
			} else {
				element.setAttribute(attribute, value);
				
				return true;
			}
		},
	};
})();


const __host__ = (function() {
	return {
		portal: function(port) {
			var address;
			
			if (window.location.hostname === "localhost") {
				address = "[::1]";
			} else {
				address = window.location.hostname;
			}
			
			return (address + ":" + port);
		},
	};
})();


const __window__ = (function() {
	//* Configurable constant definitions *//
	const TRAVEL_ATTEMPT_INTERVAL_MILLISECONDS = 5000;
	
	//* Globals *//
	var controlledExit = false;
	var defaultPrompt = null;
	var hashNavigator = null;
	var travelHandler = null;
	
	
	return {
		parameters: new URLSearchParams(window.location.search),
		
		
		at: function(pageName) {
			return __document__.getName().toLowerCase().endsWith(page);
		},
		
		back: function (args = null){
			var path = document.referrer.split('?')[0];
			
			if (null !== args) {
				var argList = [];
				
				for (var key in args) {
					 argList.push(encodeURIComponent(key) + "=" + encodeURIComponent(args[key]));
				}
				
				path += "?";
				path += argList.join("&");
			}
			
			__window__.travelTo(path);
		},
		
		clearHash: function() { 
		    var scrollV, scrollH, loc = window.location;
		    
		    if ("pushState" in history)
			  history.pushState("", document.title, loc.pathname + loc.search);
		    else {
			  scrollV = document.body.scrollTop;
			  scrollH = document.body.scrollLeft;

			  loc.hash = "";
			  
			  document.body.scrollTop = scrollV;
			  document.body.scrollLeft = scrollH;
		    }
		},
		
		fill: function(form){
			(new URL(window.location.href)).searchParams.forEach((value, key) => {
				const formElement = form[key];
				
				if (formElement) {
					if (formElement instanceof HTMLInputElement) {
						if (formElement.type === "checkbox") {
							formElement.checked = (value === "true");
						} else if (formElement.type == "text") {
							formElement.value = value;
						} else {
							formElement.value = value;
						}
					} else if (formElement instanceof RadioNodeList) {
						for (var i in formElement) {
							const groupElement = formElement[i];

							if (!(groupElement instanceof HTMLInputElement)) {
								continue;
							}

							if (groupElement.value.toLowerCase() === value.toLowerCase()) {
								value = groupElement.value;

								break;
							}
						}

						formElement.value = value;
					} else {
						console.log("unknown form element '" + formElement + "' for '" + key + "'");
					}
				}
			});
		},
		
		getHashTarget: function(){
			if (window.location.hash) {
				return window.location.hash.split("?", 2)[0].substr(1);
			}

			return null;
		},
		
		getHashParameters: function(){
			if (window.location.hash) {
				return new URLSearchParams(window.location.hash.split("?", 2)[1]);
			}
			
			return null;
		},
		
		getScrollY: function() {
			return (window.pageYOffset + window.innerHeight);
		},
		
		halt: function(message) {
			window.stop();
			
			//if ($.browser.msie) {
			//	document.execCommand("Stop");
			//}
			
			if (message) {
				alert(message);
			}
		},
		
		hashnav: function(target, parameters = new URLSearchParams("")){
			if (hashNavigator && target) {
				hashNavigator(target, parameters);
				
				__window__.clearHash();
			}
		},
		
		loadHash: function (){
			__window__.hashnav(__window__.getHashTarget(), __window__.getHashParameters());
		},
		
		prompt (title = null, message = null, options = null, parax = null){
			if (defaultPrompt) {
				defaultPrompt(title, message, options, parax);
			} else {
				alert(message);
			}
		},
		
		refresh: function() {
			if (! controlledExit) {
				controlledExit = true;
			}
			
			window.location.reload();
		},
		
		scrollTop: function() {
			window.scrollTo(0, 0);
		},
		
		setDefaultPrompt: function(callback){
			defaultPrompt = callback;
		},
		
		setHashNavigator: function(callback){
			hashNavigator = callback;
		},
		
		travelTo: function(path) {
			if (null !== travelHandler) {
				return false;
			}
			
			if (! controlledExit) {
				controlledExit = true;
			}
			
			__document__.disableReload();
			
			window.location.replace(path);
			
			travelHandler = setInterval(() => {
				window.location.replace(path);
			}, TRAVEL_ATTEMPT_INTERVAL_MILLISECONDS);
			
			return true;
		},
		
		warnOnExit: function() {
			window.addEventListener('beforeunload', function(e) {
				if (! controlledExit) {
					e.preventDefault();
					e.returnValue = "";
				}
			});
		},
	};
})();

