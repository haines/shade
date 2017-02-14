(ns leiningen.deploy-shaded-jar
  "Build and deploy shaded uberjar to remote repository."
  (:require [leiningen.deploy :as deploy]
            [leiningen.pom :as pom]
            [leiningen.shade-jar :as shade-jar]))

(defn files-for [project repo]
  (let [signed? (deploy/sign-for-repo? repo)
        artifacts {[:extension "jar"] (shade-jar/shade-jar project)
                   [:extension "pom"] (pom/pom project)}
        sig-opts (deploy/signing-opts project repo)]
    (if (and signed? (not (.endsWith (:version project) "-SNAPSHOT")))
      (reduce merge artifacts (deploy/signatures-for-artifacts artifacts sig-opts))
      artifacts)))

(defn deploy-shaded-jar
  "Deploy shaded uberjar and pom to remote repository.

  The target repository will be looked up in :repositories in project.clj:
    :repositories [[\"snapshots\" \"https://internal.repo/snapshots\"]
                   [\"releases\" \"https://internal.repo/releases\"]
                   [\"alternate\" \"https://other.server/repo\"]]

  If you don't provide a repository name to deploy to, either \"snapshots\" or
  \"releases\" will be used depending on your project's current version. You may
  provide a repository URL instead of a name.

  See `lein help deploying` under \"Authentication\" for instructions on
  how to configure your credentials so you are not prompted on each
  deploy."
  ([project]
   (deploy-shaded-jar project (if (pom/snapshot? project) "snapshots" "releases")))
  ([project repository]
   (with-redefs [deploy/files-for files-for]
     (deploy/deploy project repository))))
