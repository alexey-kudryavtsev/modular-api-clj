# About
This repo is a demo of a simple clojure websever with two runtime-configurable 'plugins' that implement API endpoints. 

## Problem
Suppose we have a clojure server that has some API (JSON-RPC, REST, etc). Over the time our API layer grows. At some point we might want to implement API methods/endpoints and their business logic in a separate projects/repos. In these projects we might need to use facilities provided by the main server app e.g. DB persistence, message broker, etc. We want to develop our supplementary 'plug-in' projects independently, i.e. at build time main server app knows nothing about 'plug-in' projects that we will use at runtime.

## Explicit Configuration Solution 
The problem resembles those solved by IoC containers and SPI. Both 'vanilla' IoC containers (java EE, Spring without Boot) and SPI require developers to specify class names of components (Beans or SPI implementations) in an XML/plaintext configuration file at some well-known location. Then these frameworks leverage java classloader capabilities to dynamically load required classes.  

While it's doable in java and therefore in clojure, java-clojure interop adds unnecessary friction. On the other hand, dynamic nature of clojure makes it almost trivial to load namespaces from classpath with (load ...) and friends. Moreover, there is no need to put configuration files inside our plugin projects (although we may do that), we can as well provide the configuration separately at runtime with CLI options, env variables, etc.


## Autoconfiguration Solution
At system start we almost always know which implementations we are going to use, so it's usually possible to explicitly specify implementation classes with configuration file/string. On the other hand, in case of API endpoint plugins one might reasonably want to use **all** the implemations provided on the JVM classpath.  

While perfectly valid, this requirement essentially forces us to use classpath as poor man's configuration data. Generating classpath is tricky and huge portions of maven, leiningen and deps.edn projects are devoted solely to that purpose. It might not be a good idea to write it by hand or do any tinkering with it. Obviously, by using classpath you also loose granular control of the namespaces/classes used. For instance, you can't disable one buggy API namespace and enable the other if they are both packed in the same jar.    

Putting the arguments aside, you might still prefer 'Spring boot like' experience, zero-configuration and convention-based approach. That's OK. We use Reflections library to achieve something similar to Spring ComponentScan. (We choose not to use original Spring implementation as it's a bit heavy and seems not intended for use by non-Spring libraries).