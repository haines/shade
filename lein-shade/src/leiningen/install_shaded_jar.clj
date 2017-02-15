(ns leiningen.install-shaded-jar
  "Install the current project's shaded uberjar to the local repository."
  (:require [cemerick.pomegranate.aether :as aether]
            [leiningen.core.project :as project]
            [leiningen.core.main :as main]
            [leiningen.shade-jar :as shade-jar]
            [leiningen.pom :as pom]
            [clojure.java.io :as io]))

(defn install-shaded-jar
  "Install shaded uberjar and pom to the local repository; typically ~/.m2."
  [project]
  (when (not (or (:install-releases? project true)
                 (pom/snapshot? project)))
    (main/abort "Can't install release artifacts when :install-releases?"
                "is set to false."))
  (let [jarfiles {[:extension "jar"] (shade-jar/shade-jar project)}
        pomfile (pom/pom project)
        local-repo (:local-repo project)]
    (aether/install
     :coordinates [(symbol (:group project) (:name project))
                   (:version project)]
     :artifact-map jarfiles
     :pom-file (io/file pomfile)
     :local-repo local-repo)
    (main/info (str "Installed shaded uberjar and pom into " (if local-repo
                                                               local-repo "local repo") "."))))
