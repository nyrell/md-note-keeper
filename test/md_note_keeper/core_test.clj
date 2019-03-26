(ns md-note-keeper.core-test
  (:require [clojure.test :refer :all]
            [md-note-keeper.core :refer :all]
            [environ.core :as environ]))

(deftest a-test
  (println "mode=" (environ/env :mode))
  (testing "FIXME, I fail."
    (is (= 1 1))))
