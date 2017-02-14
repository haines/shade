(ns shade.core
  (:require [clojure.java.io :as io]
            [clojure.string :as string])
  (:import [org.apache.maven.plugins.shade DefaultShader ShadeRequest]
           [org.apache.maven.plugins.shade.relocation SimpleRelocator]
           [org.codehaus.plexus.logging Logger]
           [org.codehaus.plexus.logging.console ConsoleLogger]))

(defn- namespace-pattern [namespace]
  (str (namespace-munge namespace) "."))

(defn- relocator [[namespace shaded-namespace]]
  (SimpleRelocator. (namespace-pattern namespace) (namespace-pattern shaded-namespace) [] []))

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
