(ns shade.core
  (:require [clojure.java.io :as io]
            [clojure.string :as string])
  (:import [org.apache.maven.plugins.shade DefaultShader ShadeRequest]
           [org.apache.maven.plugins.shade.relocation SimpleRelocator]
           [org.codehaus.plexus.logging Logger]
           [org.codehaus.plexus.logging.console ConsoleLogger]))

(defn- namespace-pattern [namespace suffix]
  (str (namespace-munge namespace) suffix))

(defn- relocator [namespace shaded-namespace suffix]
  (SimpleRelocator. (namespace-pattern namespace suffix)
                    (namespace-pattern shaded-namespace suffix)
                    []
                    []))

(defn- relocators [[namespace shaded-namespace]]
  (map #(relocator namespace shaded-namespace %) ["." "$" "__init"]))

(defn- shade-request [jar shaded-jar relocations]
  (doto (ShadeRequest.)
    (.setJars #{(io/as-file jar)})
    (.setUberJar (io/as-file shaded-jar))
    (.setRelocators (mapcat relocators relocations))
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
