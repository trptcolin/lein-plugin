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

(defn plugin-standalone-filename [group name version]
  (str (if group (str group "-") nil) name "-" version ".jar"))

(defn extract-name-and-group [project-name]
  ((juxt name namespace) (symbol project-name)))

(defn plugin-install
  "Download, package, and install plugin jarfile into
                     ~/.lein/plugins
                   Syntax: lein plugin install GROUP/ARTIFACT-ID VERSION
                     You can use the same syntax here as when listing Leiningen
                     dependencies."
  [project-name version]
  (install project-name version)
  (let [[name group] (extract-name-and-group project-name)
        temp-project (format "/tmp/lein-%s" (java.util.UUID/randomUUID))
        jarfile (-> (local-repo-path name (or group name) version)
                    (.replace "$HOME" (System/getenv "HOME")))
        _ (extract-jar (file jarfile) temp-project)
        project (read-project (format "%s/project.clj" temp-project))
        standalone-filename (plugin-standalone-filename group name version)]
    (deps project)
    (with-open [out (-> (str plugins-path "/" standalone-filename)
                        (FileOutputStream.)
                        (ZipOutputStream.))]
      (let [deps (->> (.listFiles (file (:library-path project)))
                      (filter #(.endsWith (.getName %) ".jar"))
                      (cons (file jarfile)))]
        (write-components deps out)))
    (println "Created" standalone-filename)))

(defn plugin-uninstall
  "Delete the plugin jarfile
                    Syntax: lein plugin uninstall GROUP/ARTIFACT-ID"
  [project-name version]
  (let [[name group] (extract-name-and-group project-name)]
    (.delete (file plugins-path (plugin-standalone-filename group name version)))))

(defn plugin-help
  "Show plugin subtasks"
  []
  (println (str "Plugin tasks available:\n
  install         " (:doc (meta #'plugin-install)) "\n
  uninstall       " (:doc (meta #'plugin-uninstall)) "\n
  help            " (:doc (meta #'plugin-help)) "\n")))

(defn plugin
  ([] (plugin-help))
  ([_] (plugin-help))
  ([_ _] (plugin-help))
  ([subtask project-name version]
    (case subtask
      "install" (plugin-install project-name version)
      "uninstall" (plugin-uninstall project-name version)
      (plugin-help))))

