(ns md-note-keeper.log
  (:require [taoensso.timbre :as timbre]
            [clojure.string :as str] ))

(defn timbre-output-fn
  "Default (fn [data]) -> string output fn.
  Use`(partial default-output-fn <opts-map>)` to modify default opts."
  ([     data] (timbre-output-fn nil data))
  ([opts data] ; For partials
   (let [{:keys [no-stacktrace? stacktrace-fonts]} opts
         {:keys [level ?err #_vargs msg_ ?ns-str ?file hostname_
                 timestamp_ ?line]} data]
     ;; (pprint data)
     (str
      (when (-> data :config :my-enable-timestamp)
        (str (force timestamp_)       " "))
      (when (-> data :config :my-enable-hostname)
        (str (force hostname_)        " "))
      (when (-> data :config :my-enable-level)
        (str (str/upper-case (name level))  " "))
      (when (-> data :config :my-enable-ns)
        (str "[" (or ?ns-str ?file "?") ":" (or ?line "?") "] - "))
      (force msg_)
      (when-not no-stacktrace?
        (when-let [err ?err]
          (str "\n" (timbre/stacktrace err opts)) ))))))

