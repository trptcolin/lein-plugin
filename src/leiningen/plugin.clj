(ns leiningen.plugin
  (:use [leiningen.core :only (home-dir
                               read-project)]
        [leiningen.uberjar :only (write-components)]
        [leiningen.deps :only (deps)]
        [leiningen.jar :only (local-repo-path
                              extract-jar
                              get-default-uberjar-name)]
        [leiningen.install :only (install)]
        [clojure.java.io :only (file)])
  (:import [java.util.zip ZipOutputStream]
           [java.io File FileOutputStream]))

(def plugins-path (file (home-dir) "plugins"))

(defn plugin-install [project-name version]
  (install project-name version)
  (let [[name group] ((juxt name namespace) (symbol project-name))
        temp-project (format "/tmp/lein-%s" (java.util.UUID/randomUUID))
        jarfile (-> (local-repo-path name (or group name) version)
                    (.replace "$HOME" (System/getenv "HOME")))
        _ (extract-jar (file jarfile) temp-project)
        project (read-project (format "%s/project.clj" temp-project))
        standalone-filename (get-default-uberjar-name project)]
    (deps project)
    (with-open [out (-> (str plugins-path "/" standalone-filename)
                        (FileOutputStream.)
                        (ZipOutputStream.))]
      (let [deps (->> (.listFiles (file (:library-path project)))
                      (filter #(.endsWith (.getName %) ".jar"))
                      (cons (file jarfile)))]
        (write-components deps out)))
    (println "Created" standalone-filename)))

(defn plugin-help []
  (println "Plugin tasks available:

  install         Download, package, and install plugin jarfile into
                    ~/.lein/plugins
                  Syntax: lein plugin install GROUP/ARTIFACT-ID VERSION
                    You can use the same syntax here as when listing Leiningen
                    dependencies.

  help            Show this screen
"))

(defn plugin
  ([] (plugin-help))
  ([_] (plugin-help))
  ([_ _] (plugin-help))
  ([subtask project-name version]
    (case subtask
      "install" (plugin-install project-name version)
      (plugin-help))))

