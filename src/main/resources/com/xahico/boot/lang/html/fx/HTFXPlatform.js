

const HTFXPlatform = (function() {
	var onloaded = null;
	var routines = [];
	var signaled = false;
	
	
	
	return {
		require: function(type, url) {
			switch (type.toLowerCase()) {
				case "script": {
					const injection = document.createElement("script");
					
					injection.src = url;
					
					document.head.appendChild(injection);
					
					break;
				}
				case "stylesheet": {
					const injection = document.createElement("link");
					
					injection.rel = "stylesheet";
					injection.href = url;
					
					document.head.appendChild(injection);
					
					break;
				}
			}
		},
		
		run: function(callback) {
			if (signaled) {
				callback();
			} else {
				routines.push(callback);
			}
		},
		
		signalReady: function() {
			if (! signaled) {
				signaled = true;

				if (onloaded) {
					onloaded();
				}
				
				routines.forEach((routine) => routine());
			}
		},
	}
})();

