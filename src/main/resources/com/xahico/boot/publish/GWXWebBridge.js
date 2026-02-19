

const __GWXWebApplication__ = (function() {
        _TOKEN_IDENTITY = "$(token)";
        _URL_EVENT_CAPTURE_ALL = "$(gateway)/*.**"
        _USE_SMART_CONNECTIONS = false;//$(use_smart_connections);
        
        closeTimeout = null;
        eventListeners = {};
        eventSource = null;
        timeout = $(timeout);
        timeoutHandler = null;
        
        class Event {
            data = null;
            id = null;
            source = null;
            target = null;
            timestamp = null;
            
            constructor (eventObject){
                this.data = eventObject['data'];
                this.id = eventObject['id'];
                this.source = eventObject['source'];
                this.target = eventObject['target'];
                this.timestamp = eventObject['timestamp'];
            }
            
            getSourceName (){
                const delimiter = this.source.lastIndexOf("/");
                
                if (delimiter !== -1) {
                    return this.source.substring(delimiter + 1);
                } else {
                    return null;
                }
            }
            
            getTargetName (){
                const delimiter = this.target.lastIndexOf("/");
                
                if (delimiter !== -1) {
                    return this.target.substring(delimiter + 1);
                } else {
                    return null;
                }
            }
            
            matches (pattern){
                const eventId_words = this.path().split("/");
                const pattern_words = pattern.split("/");
                
                if (pattern_words.length !== eventId_words.length) {
                    return false;
                }

                for (var i = 0; i < pattern_words.length; i++) {
                    const eventId_word = eventId_words[i];
                    const pattern_word = pattern_words[i];

                    if (i === pattern_words.length - 1) {
                        const eventId_split = eventId_word.split(".");
                        const pattern_split = pattern_word.split(".");

                        // Must have same number of dot segments
                        if (eventId_split.length !== pattern_split.length) {
                            return false;
                        }

                        for (let j = 0; j < pattern_split.length; j++) {
                            if (pattern_split[j] === "*") {
                                continue;
                            }
                            
                            if (pattern_split[j] !== eventId_split[j]) {
                                return false;
                            }
                        }
                        // Only return true if all segments match
                        return true;
                    }

                    if (pattern_word === "*") {
                        continue;
                    }

                    if (pattern_word !== eventId_word) {
                        return false;
                    }
                }

                return true;
            }
            
            path (){
                return (this.source + "." + this.id);
            }
        }
        
        function create_listener (){
            eventSource = new EventSource(_URL_EVENT_CAPTURE_ALL, { withCredentials: true });
            eventSource.onerror = (e) => {
                if (!navigator.onLine) {
                    __document__.reload();
                }
            };
            eventSource.onmessage = (eventObject) => {
                if (Object.keys(eventListeners).length > 0) {
                    event = new Event(JSON.parse(eventObject.data));

                    for (var pattern in eventListeners) {
                        if (event.matches(pattern)) {
                            for (var handler of eventListeners[pattern]) {
                                handler(event);
                            }
                        }
                    }
                }
            };
        }
        
        function update_auth (){
            __GWXWebApplication__.store_auth(__GWXWebApplication__.get_auth(), false);

            if (timeoutHandler) {
                clearTimeout(timeoutHandler);

                timeoutHandler = null;
            }

            if (timeout !== 0) {
                timeoutHandler = setTimeout(() => {
                    __GWXWebApplication__.clear_auth(false);

                    __window__.prompt(null, "Your session has timed out", {
                        "Ok": () => {
                            __window__.travelTo("/");
                        }
                    }, "Ok");
                }, timeout);
            }
        }
        
        create_listener();
        
        window.addEventListener("beforeunload", () => {
            if (eventSource) {
                eventSource.close();
            }
        });
        
        document.addEventListener("keyup", () => {
            if (__GWXWebApplication__.is_auth()) {
                update_auth();
            }
        });

        document.addEventListener("mousemove", () => {
            if (__GWXWebApplication__.is_auth()) {
                update_auth();
            }
        });

        document.addEventListener("scroll", () => {
            if (__GWXWebApplication__.is_auth()) {
                update_auth();
            }
        });
        
        if (_USE_SMART_CONNECTIONS) {
            window.addEventListener("focus", () => {
                // Cancel pending close if user returns within 30s
                if (closeTimeout) {
                    clearTimeout(closeTimeout);
                    closeTimeout = null;
                }

                if (!eventSource || (eventSource.readyState === EventSource.CLOSED)) {
                    create_listener();
                }
            });

            document.addEventListener("visibilitychange", () => {
                if (document.visibilityState === "hidden") {
                    // Start a 30-second timer to close the connection
                    closeTimeout = setTimeout(() => {
                        if (eventSource) {
                            eventSource.close();
                            eventSource = null;
                        }
                    }, 30000); // 30 seconds
                } else {
                    // User returned: cancel the pending close
                    if (closeTimeout) {
                        clearTimeout(closeTimeout);
                        closeTimeout = null;
                    }
                }
            });
        }
        
	return {
            clear_auth: function (reset = true){
                __document__.clearCookie(_TOKEN_IDENTITY);
                
                if (reset) {
                    __document__.reload();
                }
            },
            
            get_auth: function (){
                return __document__.lookupCookie(_TOKEN_IDENTITY);
            },
            
            is_auth: function (){
                return (null !== __GWXWebApplication__.get_auth());
            },
            
            listen: function (eventId, listener){
                if (!(eventId in eventListeners)) {
                    eventListeners[eventId] = [listener];
                } else {
                    eventListeners[eventId].push(listener);
                }
                
                return {
                    detach: function (){
                        const index = eventListeners[eventId].indexOf(listener);
                        
                        if (index !== -1) {
                            eventListeners[eventId].splice(index, 1);
                        }
                    }
                };
            },
            
            store_auth: function (token, reset = true){
                __document__.setCookie(_TOKEN_IDENTITY, token, timeout);
                
                if (reset) {
                    __window__.travelTo("/");
                }
            },
            
            transact: async function (method, path, data, callbacks){
                var auth;
                var ops;
                var res;
                var url;
                
                ops = {
                    method,
                    headers: {
                        "Content-Type": "application/json",
                        "Accept": "application/x-ndjson",
                    }
                };
                
                auth = __GWXWebApplication__.get_auth();
                
                if (auth) {
                    ops['headers']["Authorization"] = `Bearer ${auth}`
                }
                
                Object.keys(data).forEach(k => {
                    if (data[k] === undefined) delete data[k];
                });
                
                url = "$(gateway)";;
                
                if (!path.startsWith("/")) {
                    url += "/";
                }
                
                url += path;
                
                if ((method.toUpperCase() === "DELETE") || (method.toUpperCase() === "GET")) {
                    const urlParams = new URLSearchParams(data).toString()
                    
                    if (urlParams.length > 0) {
                        url += "?";
                        url += urlParams;
                    }
                } else {
                    ops.body = JSON.stringify(data);
                }
                
                console.log("transact " + url);
                
                try {
                   res = await fetch(url, ops);
                } catch (err) {
                    if (callbacks['complete']) {
                        callbacks['complete']();
                    }
                    
                    if (callbacks['failure']) {
                        callbacks['failure']("");
                    }
                    
                    return false;
                }
                
                if (!res.ok) {
                    if (callbacks['complete']) {
                        callbacks['complete']();
                    }
                    
                    if (callbacks['failure']) {
                        callbacks['failure']("");
                    }
                    
                    return false;
                }
                
                if (callbacks['complete']) {
                    callbacks['complete']();
                }

                const reader = res.body.getReader();
                const decoder = new TextDecoder();
                let buffer = "";
                let isFirst = true;

                let responseObj = null;
                const items = [];

                try {
                    while (true) {
                        const { value, done } = await reader.read();
                        if (done) break;

                        buffer += decoder.decode(value, { stream: true });

                        let lines = buffer.split("\n");
                        buffer = lines.pop();

                        for (const line of lines) {
                            if (!line.trim()) continue;

                            let obj;
                            try {
                                obj = JSON.parse(line);
                            } catch (err) {
                                if (callbacks['failure']) {
                                    callbacks['failure'](err);
                                }

                                return false;
                            }
                            
                            if (obj.error) {
                                if (callbacks['failure']) {
                                    callbacks['failure'](obj.error);
                                }

                                return false;
                            }

                            if (isFirst) {
                                responseObj = obj;
                                isFirst = false;
                            } else {
                                items.push(obj);
                            }
                        }
                    }

                    if (buffer.trim().length > 0) {
                        let obj = JSON.parse(buffer.trim());

                        if (obj.error) {
                            throw new Error(obj.error);
                        }

                        if (isFirst) responseObj = obj;
                        else items.push(obj);
                    }
                    
                    if (callbacks['success']) {
                        callbacks['success'](responseObj, items);
                    }
                    
                    return true;
                } catch (err) {
                    if (callbacks['failure']) {
                        callbacks['failure'](err);
                    }

                    return false;
                }
            }
	};
})();

