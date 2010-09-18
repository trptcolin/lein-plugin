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

(defn plugin [_ project-name version]
  ; _ means "install" for now...
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

