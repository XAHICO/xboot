
from com.xahico.boot.publish import GWXAuthenticationException as AuthError
from com.xahico.boot.publish import GWXCustomException as OddError
from com.xahico.boot.publish import GWXParameterException as ParameterError

class Artifact:
    def __init__(self, path, access, func):
        self.access = access.upper()
        self.func = func
        self.path = path

class Route:
    def __init__(self, path, access, func):
        self.access = access.upper()
        self.func = func
        self.path = path

class Variable:
    def __init__(self, key, value):
        self.key = key
        self.value = value

_artifacts = []

def artifact(path, access):
    def decorator(func):
        e = Artifact(path, access, func)
        
        _artifacts.append(e)
        
        return func
    return decorator

def resource(prefix):
    # ensure prefix ends with "/"
    if not prefix.endswith("/"):
        prefix += "/"

    def decorator(func):
        # create the route path
        full_path = prefix + "$(resource-name)"

        def wrapper(context, instance):
            result = func(context, instance)

            # if callback returned True then auto-build the path
            if result is True:
                return prefix + context.query["resource-name"]

            return result

        # register wrapper as a route
        routed = route(full_path, "SHARED")
        return routed(wrapper)

    return decorator

_routes = []

def route(path, access):
    def decorator(func):
        r = Route(path, access, func)
        
        _routes.append(r)
        
        return func
    return decorator

_variables = []

def var(key, value):
    v = Variable(key, value)
    
    _variables.append(v)

_routes = []

_route_default_private = None
_route_default_public = None

def default(func):
    global _route_default_private
    global _route_default_public
    
    for routed in _routes:
        if routed.func is func:
            if routed.access == "PRIVATE":
                _route_default_private = routed
            if routed.access == "PUBLIC":
                _route_default_public = routed
            if routed.access == "SHARED":
                _route_default_private = routed
                _route_default_public = routed
            break
		
    return func


var("window-vp-xl", "1600px")
var("window-vp-md", "1200px")
var("window-vp-ms", "900px")
var("window-vp-xs", "600px")


@route("/", "PRIVATE")
def callback (context, instance):
    global _route_default_private
    if _route_default_private:
        return _route_default_private.func(context, instance)
        
@route("/", "PUBLIC")
def callback (context, instance):
    global _route_default_public
    if _route_default_public:
        return _route_default_public.func(context, instance)

@route("/favicon.ico", "SHARED")
def callback (context, instance):
    return "favicon.ico"
