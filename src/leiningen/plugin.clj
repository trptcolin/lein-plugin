(ns leiningen.plugin
  (:use [leiningen.deps :only (make-deps-task copy-dependencies)]
        [leiningen.core :only (home-dir)]
        [clojure.java.io :only (file)])
  (:require [lancet]))

(def plugins-path (file (home-dir) "plugins"))

(defn plugin [_ group-and-id version]
  ; _ means "install" for now...
  (let [deps-task (make-deps-task
                    {:root ""
                     :name "Global Plugins"
                     :dependencies [[(symbol group-and-id) version]]}
                    :dependencies)
        _ (.execute deps-task)
        fileset (.getReference lancet/ant-project (.getFilesetId deps-task))]
    (.mkdirs plugins-path)
    (copy-dependencies nil plugins-path true fileset)))


