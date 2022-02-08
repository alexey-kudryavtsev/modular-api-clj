(ns server.core
  (:require [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]
            [io.pedestal.http.body-params :as p-bp]
            [clojure.java.io :as io]
            [clojure.string :as string])
  (:import org.reflections.Reflections
           org.reflections.scanners.Scanners))

;; Dynamic methods

(def method-list (atom {}))

(defn scan-methods
  "Find all methods with :modular-api-clj.plugin/hook annotation"
  []
  (->> (mapcat (fn [n] (vals (ns-publics n)))
               (all-ns))
       (filter (fn [f] (-> f meta :modular-api-clj.plugin/hook)))
       (map (fn [f] [(symbol (str (ns-name (:ns (meta f)))) (str (:name (meta f)))) f]))
       (into {})))

(defn well-known-configs
  "Returns plugin config maps from well-known localtions:
   1) /etc/modular-api-clj/config.edn
   2) config.edn relative to classpath"
  []
  (let [etc-file-location "/etc/modular-api-clj/config.edn"
        etc-config (when (.exists (io/file etc-file-location))
                     (-> etc-file-location
                         slurp
                         read-string))
        config-resource-path "config.edn"
        resource-config (some-> config-resource-path
                                io/resource
                                slurp
                                read-string)]
    (or etc-config resource-config)))

(defn load-all-plugins-from-classpath
  "Uses reflections lib to find and load all classes and clj(c) files on classpath
   that belong to `package`."
  [package]
  (let [package (string/replace package "-" "_")
        true-predicate (reify java.util.function.Predicate (test [_ _] true))
        reflections (Reflections. package
                                  (into-array Scanners
                                              [(.filterResultsBy Scanners/SubTypes true-predicate)
                                               Scanners/Resources]))
        package-classes (.get reflections (.of Scanners/SubTypes (into-array [Object])))
        cljc?-sources (.get reflections (.with Scanners/Resources  ".*\\.cljc?"))]
    ;; Load classes
    (doseq [c package-classes]
      (Class/forName c))
    
    ;; Load clojure source files
    (doseq [src cljc?-sources
            :let [src-without-ext (second (re-find #"(.*)\.cljc?" src))
                  abs-resource-path (str "/" src-without-ext)]]
      (load abs-resource-path))))

(defn load-plugins
  "Loads plugin classes & namespaces.
   Plugin hooks from loaded classes are discoverable with scan-methods fn."
  [& [plugins-from-classpath-scan?]]
  ;; Require plugin namespaces that are explicitly declared in config map 
  (doseq [[_ {ns :ns}] (:plugins (well-known-configs))]
    (require ns))

  ;; If `plugins-from-classpath-scan?` flag is set, try to load all namespaces
  ;; belonging to modular-api-clj.plugin package
  (when plugins-from-classpath-scan?
    (load-all-plugins-from-classpath "modular-api-clj.plugin")))

;; Simple API server

(def merge-params-interceptor
  {:name ::merge-params-interceptor
   :enter (fn [context]
            (let [params ((juxt :query-params
                                :edn-params
                                :json-params
                                :transit-params
                                :form-params) (:request context))]
              (update-in context [:request :params] #(apply merge % params))))})

(defn response [body & {:as headers}]
  {:status 200 :body body :headers headers})

(def routes
  (route/expand-routes
   #{["/api"
      :post
      [(p-bp/body-params)
       merge-params-interceptor
       (fn [request]
         (let [method (get-in request [:params :method])
               f (get @method-list (symbol method))]
           (when f
             (response (str (f request))))))]
      :route-name :api]}))

(def service-map
  {::http/routes routes
   ::http/type   :jetty
   ::http/port   8890})


(defn -main [& args]
  (let [arg-map (reduce #(assoc %1 (first %2) (second %2)) {} (partition 2 args))
        plugins-from-cp? (#{"all" "true"} (get arg-map "--plugins-from-cp"))]
    (println (str "Loading plugins from " (if plugins-from-cp? "classpath and config" "config only")))
    (load-plugins plugins-from-cp?)
    (reset! method-list (scan-methods))
    (println "--- Starting server ---")
    (println "API Method list:")
    (prn @method-list)
    (http/start (http/create-server service-map))))

;; Dev helpers

(defonce server (atom nil))

(defn start-dev [plugins-from-cp?]
  (load-plugins plugins-from-cp?)
  (reset! method-list (scan-methods))
  (reset! server                                                                        ;; <2>
          (http/start (http/create-server
                       (assoc service-map
                              ::http/join? false)))))

(defn stop-dev []
  (http/stop @server))

(defn restart [plugins-from-cp?]                                                                        ;; <4>
  (stop-dev)
  (start-dev plugins-from-cp?))
