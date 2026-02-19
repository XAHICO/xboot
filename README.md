# xboot

**xboot** is a full-stack Java application framework built around annotation-driven service composition, multi-protocol networking, and a Python-scriptable web/API layer. It provides the scaffolding to build everything from lightweight TCP services to full server-rendered web applications — with very little boilerplate.

> **Package:** `com.xahico.boot`
> **License:** See [LICENSE](LICENSE)

---

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Building](#building)
  - [Writing Your First App](#writing-your-first-app)
- [Core Concepts](#core-concepts)
  - [Boot System & Service Lifecycle](#boot-system--service-lifecycle)
  - [Annotations Reference](#annotations-reference)
- [Networking](#networking)
  - [TCP Protocols](#tcp-protocols)
  - [HTTP & Glass Web Framework](#http--glass-web-framework)
  - [GWX: The Published API & Web Bridge](#gwx-the-published-api--web-bridge)
- [Python Scripting Layer](#python-scripting-layer)
  - [IAPI.py — API Definition](#iapipy--api-definition)
  - [IWeb.py — Web Route Definition](#iwebpy--web-route-definition)
- [JavaScript Client Bridge](#javascript-client-bridge)
- [Language & Serialization Utilities](#language--serialization-utilities)
  - [JSOX](#jsox)
  - [HTML Engine](#html-engine)
  - [JavaScript Code Generation](#javascript-code-generation)
  - [XML Utilities](#xml-utilities)
- [Distributed Systems](#distributed-systems)
- [Synchronicity](#synchronicity)
- [Analytics](#analytics)
- [Accessibility Search Engine](#accessibility-search-engine)
- [Platform & I/O Utilities](#platform--io-utilities)
- [Cryptography](#cryptography)
- [Geolocation](#geolocation)
- [Dependencies](#dependencies)
- [Contributing](#contributing)

---

## Overview

xboot is designed around a single principle: your application components should be **discovered, wired, and started automatically** based on their declared types and annotations — not manually instantiated and glued together in a main method.

You annotate your classes. `Boot.launch()` scans them, resolves dependencies, starts services in order, and calls your entry point. Teardown is symmetric. Everything in between — networking, serialization, concurrency, web serving — is handled by the framework's built-in subsystems.

---

## Features

- **Annotation-driven boot sequencer** — `@MainClass`, `@AutoStart`, `@Dependencies`, `@ServiceType` control startup order automatically
- **Multi-protocol TCP networking** — BARE, BASH, ASIO (authenticated, encrypted sessions), TRAX, CORTEX (event-driven), and LOBEX (large object exchange)
- **File Transfer Service (FTS)** — dedicated protocol for authenticated file uploads
- **HTTP server & Glass web framework** — server-side rendered pages, sessions, routing, theming, SSE, WebSocket support
- **GWX (Gateway Exchange)** — a published service API bridge exposing Java backends to Python-scripted API/web routes and a JavaScript client runtime
- **Python scripting layer** — define HTTP API endpoints and web routes using Python decorators; the Java runtime executes them via Jython/GraalPy
- **JSOX** — type-safe, annotation-driven JSON object mapping with deep copy, merge, and parameterization
- **HTML engine** — full HTML parser, document model, injection, and an HTFX component platform
- **JavaScript code generation** — fluent Java API for emitting JS classes, functions, enums, and singletons
- **Distributed ID generation** — Snowflake, SafeSnowflake, ULID, CUID (Fast/Nano/Secure), and IDx
- **Synchronicity primitives** — Mutex, Promise, Watcher, managed/controlled executors, atomic monitors
- **Analytics accelerometers** — fast, steady, and big-data numeric mass collectors
- **Accessibility search engine** — scored, fuzzy, property-based search with wildcard and specified matchers
- **Geolocation** — coordinate types, country codes, Mercator/Spherical Mercator projection
- **SSL/TLS utilities** — streamlined SSL context construction
- **Configuration files** — file-backed key/value property configuration
- **Platform introspection** — OS family, processor architecture, process management

---

## Project Structure

```
src/
└── main/
    ├── java/com/xahico/boot/
    │   ├── accessibility/search/   # Scored search engine
    │   ├── algorithm/              # Sorting, generation algorithms
    │   ├── analytics/              # Data mass, accelerometers
    │   ├── collections/            # Read-only / unmodifiable wrappers
    │   ├── cryptography/           # SSL context utilities
    │   ├── dev/                    # Development helpers & markers
    │   ├── distributed/
    │   │   └── identification/     # Snowflake, ULID, CUID, IDx
    │   ├── event/                  # EventQueue
    │   ├── geo/                    # GeoLocation, Mercator, CountryCode
    │   ├── io/                     # IOBuffer, IOByteBuffer, SocketChannel wrappers
    │   ├── lang/
    │   │   ├── html/               # HTML parser, document model, HTFX platform
    │   │   ├── javascript/         # JS code generation (JSBuilder, JSObject, etc.)
    │   │   ├── json/               # JSON serialization utilities
    │   │   ├── jsox/               # JSOX type-safe object mapping
    │   │   └── xml/                # XML utilities
    │   ├── logging/                # Logger
    │   ├── net/
    │   │   ├── inet/               # IP address types, Port, Domain, URL, ranges
    │   │   ├── sock/               # TCP sockets, connectors, servers
    │   │   │   └── model/
    │   │   │       ├── asio/       # Authenticated, buffered session I/O protocol
    │   │   │       ├── bare/       # Raw transaction protocol
    │   │   │       ├── bash/       # Session-based protocol
    │   │   │       ├── cortex/     # Event-driven protocol
    │   │   │       ├── fts/        # File transfer service
    │   │   │       ├── lobex/      # Large object exchange protocol
    │   │   │       └── trax/       # Transaction-oriented protocol
    │   │   └── web/
    │   │       └── http/           # HTTP server, client, proxy
    │   │           └── model/
    │   │               └── glass/  # Glass server-rendered web framework
    │   ├── pilot/                  # Boot sequencer, service registry, DI
    │   ├── platform/               # OS, arch, file, process utilities
    │   ├── publish/                # GWX API/web bridge
    │   │   ├── collections/        # Node collection implementations
    │   │   └── handlers/           # HTTP, WebSocket, SSE, auth handlers
    │   ├── reflection/             # Reflection utilities and method wrappers
    │   ├── synchronicity/          # Concurrency primitives
    │   └── util/                   # General-purpose utilities
    │       ├── async/              # Async task, timer, monitor
    │       ├── configurable/       # Configuration files
    │       └── transformer/        # Data/object/string transformers
    └── resources/com/xahico/boot/
        ├── lang/html/fx/           # HTFXElement.js, HTFXPlatform.js
        ├── net/web/http/model/glass/ # GlassCore.js
        └── publish/                # GWXWebBridge.js, GWXWebCore.js, IAPI.py, IWeb.py
```

---

## Getting Started

### Prerequisites

- Java 17+ (module system used)
- Maven 3.8+
- Netty 4.x
- org.json

### Building

```bash
git clone https://github.com/your-org/bootsy.git
cd bootsy
mvn clean package
```

Add as a dependency in your project's `pom.xml`:

```xml
<dependency>
    <groupId>com.xahico</groupId>
    <artifactId>boot</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Writing Your First App

1. **Mark your main class and entry point:**

```java
@MainClass
public class MyApp {

    @MainEntryPoint
    public void start(Parameters args) {
        System.out.println("Hello from xboot!");
    }
}
```

2. **Launch from your JVM main:**

```java
public static void main(String[] args) {
    Boot.launch(args);
}
```

xboot will discover your `@MainClass`, invoke `@MainEntryPoint`, then scan the classpath for `@AutoStart` classes and `@ServiceType`-annotated services, resolving dependencies and starting each one in order.

---

## Core Concepts

### Boot System & Service Lifecycle

`Boot.launch(args)` drives the entire startup sequence:

1. Scans the classpath for the class annotated `@MainClass`
2. Invokes its `@MainEntryPoint` method (accepts `String[]`, `Parameters`, or no args)
3. Collects all `@AutoStart` classes and registered service types
4. Resolves `@Dependencies` to determine start order
5. Calls `start()` on each `Launchable` in sequence
6. Calls `Boot.onLoaded(runnable)` callback when complete

`Boot.shutdown()` calls `stop()` on each started component in reverse order, then calls `System.exit(0)`.

The `Core` component starts automatically (`@AutoStart`) and provides two thread pools — an async work-stealing pool and a blocking I/O fixed-thread pool — both accessible statically:

```java
Core.executeAsync(() -> doSomethingAsync());
Core.executeBlocking(() -> doSomethingWithIO());
```

### Annotations Reference

| Annotation | Target | Purpose |
|---|---|---|
| `@MainClass` | Type | Marks the application entry class |
| `@MainEntryPoint` | Method | Marks the startup method inside `@MainClass` |
| `@AutoStart` | Type | Starts the class automatically during boot |
| `@Dependencies({...})` | Type | Declares startup dependencies by class |
| `@Export(ExportType.START)` | Method | Marks the start method inside an `@AutoStart` class |
| `@Export(ExportType.STOP)` | Method | Marks the stop method inside an `@AutoStart` class |
| `@ServiceType(Provider.class)` | Annotation | Associates a service annotation with its provider |
| `@Service` | Type | Generic service marker |
| `@ASIOService(port=...)` | Type | Declares an ASIO TCP service |
| `@GlassService(port=...)` | Type | Declares a Glass HTTP web service |
| `@GWXService(port=...)` | Type | Declares a GWX published API/web service |
| `@Instance` | Type | Declares an instance implementation for a service |

---

## Networking

### TCP Protocols

xboot ships five pluggable TCP protocol models, each with its own session, request/response, transaction, and service provider abstraction.

**BARE** — raw binary protocol for minimal-overhead transactions. Suitable for internal microservice communication. Includes wire-test request/response for connection health checks.

**BASH** — session-based protocol with configurable session lifecycle. Provides `BASHSession`, `BASHService`, and error handling via `BASHConnectionErrorHandler`.

**ASIO (Buffered Abstract Session I/O)** — the most full-featured socket protocol. Supports authentication (`ASIOAuthRequest`/`ASIOAuthResponse`), encryption (`ASIOCryptor`, `ASIOSecurityProvider`), configurable buffer sizes, dynamic buffers, and option flags. Annotate a class with `@ASIOService` to register it:

```java
@Instance(MyASIOHandler.class)
@ASIOService(port = 9000, bufferSize = 1024)
public class MyASIOHandler implements ASIOHandler {
    @Override
    public void handle(ASIOSession session, ASIOExchange exchange) { ... }
}
```

**TRAX** — transaction-oriented protocol with a lightweight request/response model and error handling.

**CORTEX** — event-driven protocol. Sessions emit `CORTEXEvent` objects and a `CORTEXEventMarker` system allows typed event routing.

**LOBEX** — large object/binary exchange protocol mirroring CORTEX's event model but optimized for bulk data payloads.

**FTS (File Transfer Service)** — dedicated file upload service with authentication (`FTSSecurityProvider`, `FTSCryptor`), session configuration, and upload progress callbacks.

All protocols share the common `TCPSocket`, `TCPSocketServer`, `TCPSocketConnector`, `TCPSocketConnection` infrastructure and surface a consistent `SocketFactory` / `SocketChannelFactory` API.

### HTTP & Glass Web Framework

The HTTP layer provides a full server (`HttpServiceProvider`), client (`HttpServiceClient`), and proxy (`HttpProxyServiceProvider`). Standard HTTP features include:

- Method routing (`HttpMethod`)
- MIME type handling (`HttpMimeType`)
- Status codes (`HttpStatus`)
- Header management (`HttpHeaders`)
- Event-based handler hooks (`HttpEvent`, `HttpServiceEvent`)
- Resource management (`WebResourceManager`)
- URL construction (`URLBuilder`)

**Glass** is the server-side web framework built on top of the HTTP server. It provides:

- **Routing** — `GlassRoute`, `GlassRouteType`, `GlassRoutingMethod` with `GlassNavigationContext`
- **Sessions** — `GlassSession` (extensible)
- **Server-sent events** — `GlassPingEvent`, `GlassEvent`, `GlassEventMarkup`
- **Theming** — `GlassTheme`, `GlassCSS`
- **Class loading** — `GlassClass`, `GlassClassLoader`, `GlassClassManifest` for dynamic component loading
- **Namespaces & imports** — `GlassNamespace`, `GlassImporter`, `GlassImportable`
- **Callbacks** — `GlassCallback`, `GlassCallbacks`, `GlassCallbackCondition`
- **Security** — token-based auth via `authToken` in `@GlassService`
- **WebSocket** — upgrade, keep-alive, and force-insecure-upgrade handlers
- **SSE** — `GWXSseEventHandler`

Annotate a class with `@GlassService` to register it as a web application:

```java
@Instance(MyWebApp.class)
@GlassService(
    port = 8080,
    sessionClass = MySession.class,
    statusClass = MyStatus.class,
    fileServer = true,
    multithreaded = true
)
public class MyWebApp extends GlassServiceBase { ... }
```

### GWX: The Published API & Web Bridge

GWX (Gateway Exchange) is the highest-level networking abstraction in xboot. It publishes Java services to both a REST-style API and a web interface, bridged to a Python scripting layer and a JavaScript client runtime.

A GWX service exposes:
- A node tree (`GWXNodeTree`, `GWXNodeCollection`) for hierarchical resource organization
- An event bus (`GWXEvent`, `GWXEventHandler`, `GWXEventListener`, `GWXEventSubscription`)
- A call table (`GWXCallTable`) for dispatching incoming API calls
- Permission and authentication model (`GWXPermission`, `GWXUser`, `GWXAuthHandler`)
- A resource access store (`GWXResourceAccessStore`, `GWXResourceLoader`, `GWXResourceManager`)
- Path resolution (`GWXPath`, `GWXPathResolver`, `GWXPathWalker`)
- WebSocket and SSE handler integration
- Hook system (`GWXHook`, `GWXHookType`) for pre/post-call interception

```java
@Instance(MyService.class)
@GWXService(port = 8080)
public class MyService extends GWXServiceBase { ... }
```

---

## Python Scripting Layer

GWX services use embedded Python scripts to define their API endpoints and web routes. Two base modules are provided as resources:

### IAPI.py — API Definition

Decorators for defining JSON API endpoints:

```python
from IAPI import call, param, returns, event, secret, async

@call("/users", "READ")
@param("id", int, required=True)
@returns("user", dict)
def get_user(context, instance, exchange):
    """Returns a user by ID"""
    exchange.response['user'] = instance.get_user(exchange.request['id'])
```

| Decorator | Purpose |
|---|---|
| `@call(path, method)` | Register an API endpoint (`READ`, `CREATE`, `UPDATE`, `DELETE`) |
| `@param(name, type, required, default)` | Declare an input parameter with type validation |
| `@returns(name, type)` | Declare a return value for documentation |
| `@event(path, id)` | Register a server-sent event emitter |
| `@async` | Mark an endpoint as asynchronous |
| `@secret` | Hide an endpoint from the public export table |

Built-in endpoints automatically available on every GWX service:

- `GET /methods` — returns all registered API routes
- `GET /events` — returns all registered event emitters
- `GET /exports` — returns both combined

### IWeb.py — Web Route Definition

Decorators for defining web page routes:

```python
from IWeb import route, artifact, resource, var, default

@route("/dashboard", "PRIVATE")
@default
def dashboard(context, instance):
    return "dashboard.html"

@route("/login", "PUBLIC")
def login(context, instance):
    return "login.html"

@artifact("/logo.png", "SHARED")
def logo(context, instance):
    return instance.get_logo_bytes()

@resource("/static/")
def static_resource(context, instance):
    return True  # auto-resolves path from query
```

Access levels: `PUBLIC`, `PRIVATE`, `SHARED`

Built-in viewport CSS variables are automatically injected: `window-vp-xl` (1600px), `window-vp-md` (1200px), `window-vp-ms` (900px), `window-vp-xs` (600px).

---

## JavaScript Client Bridge

xboot ships a client-side JavaScript runtime that communicates with GWX services:

- **`GWXWebCore.js`** — core client runtime, method calling, event subscription
- **`GWXWebBridge.js`** — bridge layer between the page and the GWX service
- **`GlassCore.js`** — Glass client-side runtime for navigation and component updates
- **`HTFXElement.js`** — component base class for the HTFX component platform
- **`HTFXPlatform.js`** — HTFX platform loader and lifecycle management

These are served automatically by GWX services and embedded in pages rendered by the Glass framework.

---

## Language & Serialization Utilities

### JSOX

JSOX is xboot's type-safe JSON object mapping system. Any class implementing `JSOX` gains structured serialization, deserialization, deep copy, merge, and parameterization.

```java
public class User implements JSOX {
    @JSOXColumn("name")
    public String name;

    @JSOXColumn("age")
    public int age;
}

User user = JSOXFactory.create(User.class);
user.assume("{\"name\":\"Alice\",\"age\":30}");
String json = user.toJSONString();
Parameters params = user.parameterize();
```

Key interfaces and annotations:

- `@JSOXColumn` — map a field to a JSON key
- `@JSOXTransient` — exclude a field from serialization
- `@JSOXTranslated` — apply a transformer during serialization
- `JSOXObject`, `JSOXArray`, `JSOXMap`, `JSOXCollection` — container types
- `JSOXVariant` — union/variant types
- `JSOXUpdateHandler` — receive callbacks on field updates
- `JSOXCastable` — custom casting logic

### HTML Engine

- `HTMLParser` — parses raw HTML into a `HTMLDocument` / `HTMLNode` tree
- `HTMLElement`, `HTMLComment`, `HTMLString`, `HTMLSpecialElement` — node types
- `HTMLInjector` / `HTMLInjectionContext` — inject content into parsed documents
- `HTMLUtilities` — helper methods for common DOM operations
- `HTFXParser` / `HTFXTranslator` — HTFX component syntax handling
- `HTFXClass`, `HTFXClassLoader` — dynamic HTFX component loading

### JavaScript Code Generation

Fluent Java API for emitting JavaScript source code:

```java
JSBuilder builder = new JSBuilder();
JSObject obj = builder.object("MyService");
obj.function("greet").param("name").body("return 'Hello, ' + name;");
String code = builder.build();
```

Classes: `JSBuilder`, `JSObject`, `JSFunction`, `JSParameter`, `JSVariable`, `JSEnum`, `JSSingleton`, `JSNamedObject`, `JSCode`, `JSType`

### XML Utilities

`XMLObject`, `XMLAttributes`, `XMLStringBuilder`, `XMLUtilities` — parse, build, and manipulate XML documents.

---

## Distributed Systems

The `com.xahico.boot.distributed.identification` package provides multiple ID generation strategies for distributed environments:

| Class | Algorithm | Notes |
|---|---|---|
| `Snowflake` | Twitter Snowflake | Worker ID + data center ID + sequence, epoch 2020-01-01 |
| `SafeSnowflake` | Thread-safe Snowflake | Synchronized variant |
| `ULID` | ULID | Universally unique, lexicographically sortable |
| `FastCUIDFactory` | CUID | Optimized for throughput |
| `NanoCUIDFactory` | CUID (nano) | Nanosecond precision |
| `SecureCUIDFactory` | CUID (secure) | Cryptographically random component |
| `IDx` | Custom | Framework-internal extended ID type |

---

## Synchronicity

`com.xahico.boot.synchronicity` provides concurrency primitives beyond the standard Java library:

- **`Mutex`** — explicit mutual exclusion lock
- **`Promise`** — future-style value container with callback chaining
- **`Watcher`** — monitors a value or condition and triggers callbacks on change
- **`ControlledExecutor`** / **`ManagedExecutor`** — executor wrappers with lifecycle management
- **`Synchronizable`** / **`Synchronized`** / **`Synchronization`** — annotation and interface-based synchronization
- **`SynchronizationProvider`** / **`SynchronizationHandler`** — pluggable sync backends
- **`ClockedIntensity`** — rate measurement over time
- **`ConcurrentFallbackMap`** — concurrent map with fallback value production
- **`FutureBindings`** — bind futures to callbacks
- **`ScheduledRunnable`** — runnable with scheduling metadata

Async utilities (`com.xahico.boot.util.async`):

- **`AsyncTask`** — lightweight async execution wrapper
- **`Monitor`** / **`MonitorSet`** — object monitors for wait/notify coordination
- **`Timer`** — high-level repeating/one-shot timer
- **`Accessor`** — thread-safe value accessor

---

## Analytics

`com.xahico.boot.analytics` provides numeric data collection and analysis:

- **`DataMass`** / **`BigDataMass`** — accumulate and query large datasets
- **`NumericMass`** / **`BigNumericMass`** — numeric statistics over collected data
- **`Accelerometer`** — track rate of change in a numeric signal
- **`FastAccelerometer`** — low-overhead accelerometer
- **`SteadyAccelerometer`** — smoothed/stabilized accelerometer
- **`MetricNumber`** — SI-prefixed numeric formatting

---

## Accessibility Search Engine

`com.xahico.boot.accessibility.search` implements a scored, property-based search engine:

```java
AccessibleSearchEngine engine = new AccessibleSearchEngine();
engine.register(myDataSource);

QueryResults results = engine.search(Query.of("Alice"));
for (var result : results) {
    System.out.println(result.getScore() + " -> " + result.getData());
}
```

Key types:
- `AccessibleSearchEngine` — main search executor
- `AccessibleDataSource` — implement to make your data searchable
- `AccessibleData` / `AccessibleProperties` / `AccessibleProperty` — data model
- `QueryParser` — parses query strings into structured `Query` objects
- `QueryRating` / `Score` / `ScoreMultiplier` — scoring model
- `SpecifiedMatcher` / `WildlycardMatcher` — matching strategies
- `SearchFilter` / `SortingPolicy` — result filtering and ordering

---

## Platform & I/O Utilities

**Platform** (`com.xahico.boot.platform`):
- `PlatformUtilities` — detect OS family (`PlatformFamily`) and processor architecture (`ProcessorArchitecture`)
- `PlatformModel` — full platform descriptor
- `FileUtilities` — file operations
- `ProcessUtilities` — spawn and manage OS processes

**I/O** (`com.xahico.boot.io`):
- `IOByteBuffer` / `IODynamicByteBuffer` — managed byte buffers
- `IOStringBuffer` — string-backed buffer
- `IOSocketChannel` — NIO socket channel wrapper
- `IOReadable` / `IOWritable` — read/write interfaces
- `GenericReader` / `GenericReadResults` — generic chunked reading
- `ReverseFileInputStream` — read files in reverse
- `Source` — pluggable data source abstraction

**General utilities** (`com.xahico.boot.util`):
- `StringUtilities`, `NumberUtilities`, `MathUtilities`, `ArrayUtilities`, `CollectionUtilities`, `StreamUtilities`
- `DateTime`, `TimeUtilities`, `TimeFormatter`
- `Parameters`, `ParametersParser` — typed key/value parameter maps
- `PathTree` — hierarchical path structure
- `TokenStore`, `IDFactory`, `IDTokenFactory`
- `FileCache` — in-memory file caching
- `ZipUtilities`, `ZipHandler`
- `Benchmarker` — nanosecond-precision benchmarking
- `Configuration`, `ConfigurationFile` — properties-based config files
- `DataTransformer`, `ObjectTransformer`, `StringTransformer` — pluggable transformation pipeline

---

## Cryptography

`com.xahico.boot.cryptography`:
- `SSL` — annotation/marker for SSL-aware components
- `SSLUtilities` — build and configure `SSLContext` instances for both server and client use

Protocol-level encryption is handled per-protocol (e.g., `ASIOCryptor` / `ASIOSecurityProvider` in ASIO, `FTSCryptor` / `FTSSecurityProvider` in FTS).

---

## Geolocation

`com.xahico.boot.geo`:
- `GeoLocation` — latitude/longitude coordinate pair
- `CountryCode` — ISO country code enum
- `Mercator` / `SphericalMercator` — coordinate projection utilities
- `AddressNotLocatibleException` — thrown when an address cannot be resolved to coordinates

---

## Dependencies

| Dependency | Purpose |
|---|---|
| [Netty](https://netty.io/) | Async network I/O for all TCP and HTTP transports |
| [org.json](https://github.com/stleary/JSON-java) | JSON parsing and construction |
| JDK `jdk.httpserver` | Fallback HTTP server support |
| JDK `jdk.management` | JVM management and monitoring |
| Python runtime (Jython/GraalPy) | Execution of `IAPI.py` and `IWeb.py` scripts |

---

## Contributing

Contributions are welcome. Please open an issue to discuss significant changes before submitting a pull request.

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/my-feature`)
3. Commit your changes (`git commit -m 'Add my feature'`)
4. Push to the branch (`git push origin feature/my-feature`)
5. Open a pull request

Classes marked with `@Helper` or `@Untested` in `com.xahico.boot.dev` are development aids — tests and documentation for these areas are especially welcome.
