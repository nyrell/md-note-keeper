(ns md-note-keeper.font-dialog
  (:gen-class)
  (:require [clojure.string :as str]
            [seesaw.bind :as bind]
            [seesaw.font :as font]
            #_[taoensso.timbre :as timbre])

  (:use seesaw.dev
        seesaw.core
        seesaw.graphics
        seesaw.color
        seesaw.chooser
        seesaw.mig
        seesaw.keymap
        seesaw.keystroke)

  (:import (javax.swing JPanel)
           (java.awt.event ActionListener KeyAdapter WindowEvent)) )

;; (timbre/refer-timbre)
(native!)
;; (seesaw.dev/debug!)


(def lorem-ipsum "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Curabitur facilisis sit amet ex ac sollicitudin. Duis in ornare massa. Nullam sollicitudin nisl orci, a aliquet ante tempor id. Etiam pellentesque porta elit, eget faucibus dui tristique et. Nulla dictum viverra quam. Etiam efficitur ac justo sit amet facilisis. Curabitur ultrices dolor nec eros posuere, eu gravida nisl semper. Nulla gravida, diam vel eleifend elementum, est augue ullamcorper mi, vel vulputate dui diam vitae dui. Curabitur feugiat vitae orci ac eleifend. Vestibulum dignissim urna vitae rutrum eleifend. Quisque porttitor pellentesque nisl. Sed tempor est augue, nec tempor tortor bibendum ut. Maecenas laoreet nec enim in iaculis.

Morbi facilisis purus tortor, at aliquam lorem sagittis nec. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia Curae; Cras vel facilisis lectus. Sed at ipsum id felis vulputate vestibulum. Sed at hendrerit orci, eget interdum nisi. Suspendisse non volutpat ante. Proin posuere diam sit amet ex pellentesque, eget scelerisque augue tempor. Maecenas id dignissim dolor. Vestibulum vitae odio quis nunc semper molestie. Ut aliquam porttitor justo, nec mollis turpis convallis ut.

Proin hendrerit rhoncus diam, eget vulputate libero cursus in. Ut quam dolor, posuere eget rutrum in, tincidunt non ligula. Morbi nec tortor maximus, suscipit mi sit amet, porta tortor. Nam efficitur consectetur est viverra porta. Sed vitae pellentesque metus, id dapibus nisi. Mauris nec tellus enim. Sed vulputate mi nibh, id sollicitudin magna molestie eu. Duis consequat consectetur ante eget pulvinar. Nunc euismod sem est, quis semper nisi facilisis non. Morbi ornare arcu non turpis convallis, ac accumsan eros blandit. Cras finibus dictum quam, a pharetra neque. Aliquam consectetur erat ultricies ornare pharetra. Pellentesque fermentum mi sed odio bibendum, non iaculis dolor molestie.

Etiam blandit gravida massa, ut consequat dui interdum at. Vestibulum nec mi nunc. Donec rutrum purus sed leo congue sodales. Vivamus consequat, mauris nec ultrices dictum, libero est fermentum justo, non pharetra augue nunc in urna. Ut nec ipsum nibh. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia Curae; Sed porta erat libero, ac facilisis augue accumsan a. Praesent in fringilla turpis, vel finibus ante. Quisque et facilisis nunc, vitae scelerisque dolor. Nullam pellentesque nibh sed orci pellentesque, ullamcorper facilisis tellus feugiat.

Interdum et malesuada fames ac ante ipsum primis in faucibus. Nunc maximus lorem eget tortor rutrum ullamcorper. Etiam aliquet risus a leo efficitur mattis vitae id erat. Vivamus cursus mauris enim, in iaculis odio sollicitudin id. Maecenas commodo lorem quis tortor tincidunt, et imperdiet dolor rutrum. Phasellus a odio id nisi blandit gravida. Pellentesque vel ligula eget sem sollicitudin efficitur.")



(defn font-dialog [& {:keys [parent font] :or {font (font/font "MONOSPACED-14")}}]
  (let [font-filter (text :id :font-filter)
        text-size (spinner :id :text-size :model 16)
        font-list (listbox :id :font-list
                           :selection-mode :single
                           :model (font/font-families) )
        text-preview (text
                      :id :text-preview
                      :multi-line? true
                      :wrap-lines? true
                      ;; :editable? false
                      :font "MONOSPACED-14"
                      :text lorem-ipsum)
        
        transform-to-font
        (fn [[font-name font-size]]
          #_(println (format "Font: '%s', Size: '%s'" font-name  font-size))
          (if (and (not (empty? font-name))
                   (integer? font-size))
            (font/font :name font-name :size font-size)
            (config text-preview :font) ))

        dlg (custom-dialog
             :parent parent
             :title "Select font"
             :content
             (mig-panel
              :constraints [""
                            "[align right] [fill, grow]"
                            "[top] [fill, grow, top] [top] [top] [top]"]
              :items [[(label :text "Filter:") ""]
                      [font-filter "wrap"]
                      [(label :text "Fonts:") "gapbottom push"]
                      [(scrollable font-list) "height 50::, grow 100, wrap"]
                      ["Preview:" ""]
                      [(scrollable text-preview) "height 150::150, growy 0, wrap"]
                      ["Size:" ""]
                      [text-size "wrap"]
                      [(action :name "OK"
                               :handler 
                               (fn [e]
                                 (return-from-dialog
                                  e
                                  (transform-to-font
                                   [(selection font-list {:multi? false})
                                    (value text-size)] ))) )
                       "gapleft push, span, split 2"]
                      [(action :name "Cancel"
                               :handler (fn [e] (return-from-dialog e nil)))
                       "wrap"]])) ]
    (bind/bind
     (bind/funnel
      (bind/selection font-list {:multi? false})
      text-size)
     (bind/transform transform-to-font)
     (bind/property text-preview :font) )

    (bind/bind
     (.getDocument font-filter)
     (bind/transform
      (fn [s]
        (filter #(.contains (str/lower-case %)
                            (str/lower-case s))
                (font/font-families))))
     (bind/property font-list :model) )
    
    (selection! font-list (.getName font))
    (scroll! font-list :to [:row (.indexOf (config font-list :model) (.getName font))])
    (value! text-size (.getSize font))
    
    dlg))

;; (-> (font-dialog :font (font/font :name "DejaVu Sans Mono" :size 20)) pack! show!)

;; (font/font "MONOSPACED-14")
;; (font/font :name "MONOSPACED" :size 14)
  
;; (.getFontName (default-font "Label.font"))
;; (println (enumeration-seq (.keys (javax.swing.UIManager/getDefaults))))

;; (pprint (font/font-families))

;; (filter #(.contains % "DejaVu") (font/font-families))

#_(let [f (font/font :style :plain :name "Abyssinica SIL")]
  (println "Name: " (.getFontName f))
  (println "Size: " (.getSize f))
  (println "Style: " (.getStyle f)) )

