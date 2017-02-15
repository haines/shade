(ns shade.core
  (:require [clojure.java.io :as io]
            [clojure.string :as string])
  (:import [org.apache.maven.plugins.shade DefaultShader ShadeRequest]
           [org.apache.maven.plugins.shade.relocation Relocator]
           [java.util.regex Pattern]
           [org.codehaus.plexus.logging Logger]
           [org.codehaus.plexus.logging.console ConsoleLogger]))

(defn- namespace->path [namespace]
  (-> namespace
      namespace-munge
      (string/replace "." "/")))

(defn- path-pattern [namespace]
  (re-pattern (str #"(?<=^/?)"
                   (namespace->path namespace)
                   #"(?=[.$/]|__init(?:\.class)?$|$)")))

(defn- class-pattern [namespace]
  (re-pattern (str #"^"
                   (-> namespace namespace-munge Pattern/quote)
                   #"(?=[.$]|__init$|$)")))

(defn- relocator [[namespace shaded-namespace]]
  (let [path-pattern (path-pattern namespace)
        path-replacement (namespace->path shaded-namespace)
        class-pattern (class-pattern namespace)
        class-replacement (namespace-munge shaded-namespace)]
    (reify Relocator
      (canRelocatePath [this path]
        (boolean (re-find path-pattern path)))
      (canRelocateClass [this class]
        (boolean (re-find class-pattern class)))
      (relocatePath [this path]
        (string/replace path path-pattern path-replacement))
      (relocateClass [this class]
        (string/replace class class-pattern class-replacement))
      (applyToSourceContent [this source]
        source))))

(defn- shade-request [jar shaded-jar relocations]
  (doto (ShadeRequest.)
    (.setJars #{(io/as-file jar)})
    (.setUberJar (io/as-file shaded-jar))
    (.setRelocators (map relocator relocations))
    (.setFilters [])
    (.setResourceTransformers [])
    (.setShadeSourcesContent false)))

(defn- shader []
  (doto (DefaultShader.)
    (.enableLogging (ConsoleLogger. Logger/LEVEL_WARN "shade"))))

(defn shade
  "Relocates namespaces in the jar-file according to the translations given in
  the relocations map ({\"old.namespace\" \"new.namespace\", ...}), saving
  the modified jar to the shaded-jar-file."
  [jar-file shaded-jar-file relocations]
  (.shade (shader) (shade-request jar-file shaded-jar-file relocations)))
