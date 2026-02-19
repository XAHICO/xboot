
from com.xahico.boot.publish import GWXAuthenticationException as AuthError
from com.xahico.boot.publish import GWXCustomException as OddError
from com.xahico.boot.publish import GWXParameterException as ParameterError

from java.lang import Boolean as JavaBoolean
from java.lang import Double as JavaDouble
from java.lang import Float as JavaFloat
from java.lang import Integer as JavaInteger
from java.lang import Long as JavaLong
from java.lang import Short as JavaShort
from java.lang import String as JavaString
from java.util import List as JavaList
from java.util import Map as JavaMap
from java.util import Set as JavaSet

from org.json import JSONObject

_TYPE_MAP = {
    bool: (bool, JavaBoolean),
    dict: (dict, JavaMap),
    float: (float, JavaDouble, JavaFloat),
    int: (int, JavaInteger, JavaLong, JavaShort),
    list: (list, JavaList, JavaSet),
    str: (str, unicode, JavaString),
}

def check_type(val, typ):
    return isinstance(val, _TYPE_MAP.get(typ, (typ,)))

class Call:
    def __init__(self, path, method, func, require, authorized, params, returns, doc):
        self.async = False
        self.authorized = authorized
        self.require = require
        self.method = method.upper()
        self.path = path
        self.func = func
        self.doc = doc.strip() if doc is not None else None
        self.secret = False
        self.params = params
        self.returns = returns

class Event:
    def __init__(self, path, id, func, doc):
        self.path = path
        self.id = id
        self.func = func
        self.doc = doc.strip() if doc is not None else None
        self.secret = False

class Parameter:
    def __init__(self, name, type, required, default):
        self.name = name
        self.type = type
        self.required = required
        self.default = default

_calls = []

def call(path, method, require='?', authorized=False):
    def decorator(func):
        if not hasattr(func, "__params__"):
            func.__params__ = []
        if not hasattr(func, "__returns__"):
            func.__returns__ = []
            
        def wrapper(context, instance, exchange):
            for param in func.__params__:
                if param.name in exchange.request:
                    param_val = exchange.request[param.name]
                else:
                    param_val = None
                    
                # if expecting str and param value is not str then stringify
                if param.type is str and not isinstance(param_val, str):
                    param_val = str(param_val)
                    
                if param_val is None or param_val is JSONObject.NULL:
                    param_val = param.default
                if param.required and param_val is None:
                    raise ParameterError("missing %s" % param.name)
                if param_val is not None and not check_type(param_val, param.type):
                    raise ParameterError("invalid type for %s (expected %s, was %s)" % (param.name, param.type, type(param_val)))
                if param.default:
                    exchange.request[param.name] = param_val
            return func(context, instance, exchange)
        r = Call(path, method, wrapper, require, authorized, func.__params__, func.__returns__, func.__doc__)
        
        _calls.append(r)
        
        func.__call_object__ = r
        
        return func
    return decorator

_events = []

def event(path, id):
    def decorator(func):
        e = Event(path, id, func, func.__doc__)
        
        _events.append(e)
        
        return func
    return decorator

def param(name, type, required=False, default=''):
    def decorator(func):
        if not hasattr(func, "__params__"):
            func.__params__ = []
        func.__params__.append(Parameter(name, type, required, default))
        
        return func
    return decorator

def returns(name, type):
    def decorator(func):
        if not hasattr(func, "__returns__"):
            func.__returns__ = []
        func.__returns__.append(Parameter(name, type, False, None))
        
        return func
    return decorator

def async(func):
    r = func.__call_object__
    r.async = True
    return func

def secret(func):
    found_target = False

    if not found_target:
        for eventd in _events:
            if eventd.func is func:
                eventd.secret = True
                found_target = True
                break

    if not found_target:
        for routed in _calls:
            if routed.func is func:
                routed.secret = True
                found_target = True
                break
		
    return func

def get_api_events (privileged):
    events = []

    for eventd in _events:
        if eventd.secret and not privileged:
            continue
        
        events.append({
            'doc': eventd.doc,
            'name': eventd.name,
        })
    
    return events

def get_api_routes (privileged):
    routes = []
    
    for routed in _calls:
        if routed.secret and not privileged:
              continue
              
        params_x = []

        for param in routed.params:
            param_x = {
                'name': param.name,
                'type': param.type.__name__,
                'required': param.required,
            }
            
            if param.default:
                param_x['default'] = param.default
            
            params_x.append(param_x)
        
        returns_x = []

        for param in routed.returns:
            param_x = {
                'name': param.name,
                'type': param.type.__name__,
            }
            
            returns_x.append(param_x)
        
        http_methods = {
            'CREATE': 'POST',
            'READ': 'GET',
            'UPDATE': 'PUT',
            'DELETE': 'DELETE',
        }
        
        routes.append({
            'doc': routed.doc,
            'method': http_methods[routed.method.upper()],
            'path': routed.path,
            'params': params_x,
            'returns': returns_x,
            'authorized': routed.authorized,
        })
    
    return routes

@call("/events", 'READ')
def callback (context, instance, exchange):
    """
    returns the API event table
    """

    exchange.response['table'] = get_api_events(instance.is_privileged())

@call("/exports", 'READ')
def callback (context, instance, exchange):
    """
    returns the API export tables
    """

    exchange.response['events'] = get_api_events(instance.is_privileged())
    exchange.response['routes'] = get_api_routes(instance.is_privileged())

@call("/methods", 'READ')
def callback (context, instance, exchange):
    """
    returns the API method table
    """

    exchange.response['table'] = get_api_routes(instance.is_privileged())
