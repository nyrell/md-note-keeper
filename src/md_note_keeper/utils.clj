(ns md-note-keeper.utils
  (:require [clojure.string :as str]
            [clojure.java.io :as io])
  (:import (java.util Properties)) )

(defn filename-without-extension [s]
  (let [last-dot-ix (str/last-index-of s ".")
        separator (System/getProperty "file.separator")
        last-separator-ix (str/last-index-of s separator)
        ]
    (println last-dot-ix separator last-separator-ix)
    (cond
      (nil? last-dot-ix) s

      (or (nil? last-separator-ix)           ; No directory name
          (> last-dot-ix last-separator-ix))  ; The last dot of s is part of the name and not the path.
      (subs s 0 last-dot-ix) )))

(defn get-meta-inf [dep property]
  (let [path (str "META-INF/maven/" (or (namespace dep) (name dep))
                  "/" (name dep) "/pom.properties")
        props (io/resource path)]
    (when props
      (with-open [stream (io/input-stream props)]
        (let [props (doto (Properties.) (.load stream))]
          (.getProperty props "version") )))))

(defn font-to-font-spec [f]
  (str (.getName f) "-" (.getSize f)))

