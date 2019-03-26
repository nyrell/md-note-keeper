(defproject md-note-keeper "1.0.0-SNAPSHOT1"
  :description "MD Note Keeper"
  :url "http://example.com/FIXME"
  :license {:name "MIT License"
            :url "https://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [org.clojure/java.jdbc "0.7.8"]
                 [org.xerial/sqlite-jdbc "3.25.2"]
                 [seesaw "1.5.0"]
                 [markdown-clj "1.0.7"]
                 [environ "1.1.0"]              ;; Environment variables and more
                 [com.taoensso/timbre "4.10.0"]
                 ]
  :plugins [[lein-environ "1.1.0"]
            [lein-kibit "0.1.6"]         ; Static code analyzer, run with lein kibit, or in emacs M-x kibit
            [jonase/eastwood "0.3.5"]]   ; LINT, run with lein eastwood
  :eastwood {:out "eastwood.log"}        ; View eastwood result in emacs with compilation-mode active
  :main ^:skip-aot md-note-keeper.core
  :target-path "target/%s"
  :profiles {:dev {:env {:md-note-keeper-mode "dev"}}
             :test {:env {:md-note-keeper-mode "test"}}
             :uberjar {:aot :all
                       :env {:md-note-keeper-mode "release"}}})

