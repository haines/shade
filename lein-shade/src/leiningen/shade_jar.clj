(ns leiningen.shade-jar
  "Package up the project files and shaded dependencies into a jar file."
  (:require [shade.core :as shade]
            [leiningen.core.main :as main]
            [leiningen.core.project :as project]
            [leiningen.jar :as jar]
            [leiningen.uberjar :as uberjar]
            [clojure.pprint :refer [pprint]]
            [clojure.java.io :as io]))

(defn- root-namespace [{:keys [group name]}]
  (if (= group name)
    name
    (str group "." name)))

(defn- relocate [namespace under]
  [namespace (str under "." namespace)])

(defn- preserve-shaded-profile [project]
  (-> project
      (project/add-profiles {::shaded (-> project meta :profiles :shaded)})
      (project/merge-profiles [::shaded])))

(defn shade-jar
  "Package up the project files and shaded dependencies into a jar file.

  Includes the contents of each of the dependency jars and relocates specified
  namespaces under a 'shaded' namespace to prevent dependency clashes. Suitable
  for standalone distribution."
  [project]
  (let [output-dir (io/file (:target-path project) "shaded")
        uberjar-file (-> project preserve-shaded-profile uberjar/uberjar)
        shaded-jar-file (str (io/file output-dir (-> project jar/get-jar-filename io/file .getName)))
        options (:shade project)
        shade-under (:under options (str (root-namespace project) ".shaded"))
        relocations (map #(relocate % shade-under) (:namespaces options))]
    (shade/shade uberjar-file shaded-jar-file relocations)
    (main/info "Created" shaded-jar-file)
    shaded-jar-file))
