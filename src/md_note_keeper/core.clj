(ns md-note-keeper.core
  (:gen-class)
  (:require [md-note-keeper.db :as db]
            [md-note-keeper.log :as my-log]
            [md-note-keeper.font-dialog :as font-dialog]
            [md-note-keeper.utils :as utils]
            [clojure.string :as str]
            [clojure.java.io :as io]
            [seesaw.bind :as bind]
            [seesaw.font :as font]
            [markdown.core :as md]
            [environ.core :as environ]
            [taoensso.timbre :as timbre] )

  (:use seesaw.dev
        seesaw.core
        seesaw.graphics
        seesaw.color
        seesaw.chooser
        seesaw.mig
        seesaw.keymap
        seesaw.keystroke)
  
  (:import (javax.swing JFrame JPanel JComboBox Timer)
           (javax.swing.undo UndoManager)
           (java.awt.event ActionListener KeyAdapter WindowEvent)
           (java.util.prefs Preferences) ))

(native!)
;; (seesaw.dev/debug!)

(timbre/refer-timbre)

;; Setup logging
;; Possible log levels :trace :debug :info :warn :error :fatal :report
(timbre/merge-config! 
 {:output-fn my-log/timbre-output-fn
  :level :info
  ;; :level :warn
  ;; :level :debug
  :timestamp-opts {:pattern "yyyy/MM/dd HH:mm:ss ZZ"}
  :my-enable-hostname false
  :my-enable-timestamp false
  :my-enable-ns false
  :my-enable-level false
  })

(def program-title "MD Note Keeper")

(def default-db-file-name "my_notes.sqlite")

(def gui-frame (atom nil))

;; Only really needed because I save the "old" note when the list selection changes.
;; Otherwise I could have gotten the note id from the list selection.
(def current-note-id (atom nil))

(def undo-manager (atom (new UndoManager)))

(def css-file (atom "default.css"))

(def html-template "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">
  <html>
  <head>
    <title>%s</title>
    <style type=\"text/css\">
    %s
    </style>
  </head>
  <body>
  
  %s
  
  </body>
  </html>")

(defn get-css []
  ;; (slurp (io/resource @css-file))
  (slurp @css-file) )

(defn get-css-file [db-path]
  (let [custom-css-file (io/file (str (utils/filename-without-extension db-path) ".css"))]
    (if (.exists custom-css-file)
      custom-css-file
      (io/resource "default.css"))))



;;----------------------------------------------------------------------
;; Configuration
;;----------------------------------------------------------------------
(declare -main)
(defn get-app-pref-node []
  (.node (java.util.prefs.Preferences/userRoot)
         (str (:ns (meta #'-main))) ))
;; (str (ns-name *ns*))))

(defn get-pref [pref-str default]
  (let [value (.get (get-app-pref-node) pref-str (str default))]
    (info "  get-pref: " pref-str value)
    value))

(defn set-pref [pref-str value]
  (info "  set-pref: " pref-str value)
  (.put (get-app-pref-node) pref-str (str value)))

(defn clear-pref [pref-str]
  (.remove (get-app-pref-node) pref-str))

(defn get-app-version []
  (or (utils/get-meta-inf 'md-note-keeper "version")
      (:md-note-keeper-version environ/env)))

(defn dialog-select-note-file []
  (let [home-dir (System/getProperty "user.home")
        file-separator (System/getProperty "file.separator")
        default-db-path (str home-dir file-separator default-db-file-name)
        text-field-db-path (text :id :db-path :text default-db-path)]
    (dialog
     :title "Select note file"
     :type :question
     :options [(action :name "OK"
                       :handler #(return-from-dialog % (config text-field-db-path :text)))
               (action :name "Cancel"
                       :handler #(return-from-dialog % nil))]
     :content
     (vertical-panel
      :items ["Select a file in which to store all your notes:"
              (horizontal-panel
               :items [text-field-db-path
                       (action
                        :name "Browse..."
                        :handler
                        (fn [e]
                          (let [result (seesaw.chooser/choose-file
                                        :type "Select note file"
                                        :selection-mode :files-only)]
                            (when result (text! text-field-db-path (.getPath result))))))]
               )] ))))

(defn dialog-note-file-not-found [default]
  (let [note-file (get-pref "db-path" "")]
    (dialog
     :title "Note file not found"
     :type :info
     :options [(action :name "Select another..."
                       :tip "Select another note file"
                       :handler
                       (fn [e]
                         (let [result (-> (dialog-select-note-file) pack! show!)]
                           (when result (return-from-dialog e result)))))
               (action :name "Exit"
                       :tip "Exit program"
                       :handler #(return-from-dialog % nil))]
     :content (vertical-panel
               :items ["The note file that was used previously could not be found:"
                       " "
                       note-file] ))))

(defn locate-db-path []
  (let [db-path (get-pref "db-path" "")
        db-exists (.exists (io/file db-path))
        home-dir (System/getProperty "user.home")
        md-note-keeper-mode (environ/env :md-note-keeper-mode)]
    (cond
      (= md-note-keeper-mode "dev") (-> "md-note-keeper-dev.db" io/resource io/file .getPath)
      (= md-note-keeper-mode "test") (-> "md-note-keeper-test.db" io/resource io/file .getPath)
      
      :else (if db-exists
              db-path
              (if (= db-path "")
                (-> (dialog-select-note-file) pack! show!)
                (-> (dialog-note-file-not-found db-path) pack! show!) )))))

;; (locate-db-path)
;; (get-pref "db-path" "")
;; (.createNewFile (io/file (get-pref "db-path" "")))
;; (set-pref "db-path" "/home/matny/Documents/md_note_keeper.db")
;; (set-pref "db-path" "/home/matny/md_note_keeper.db")
;; (clear-pref "db-path")

(defn dialog-about [_]
  (dialog
   :title "About"
   :type :info
   :content (str (format "%s %s\n" program-title (get-app-version))
                 (format "(c) Mattias Nyrell 2019") )))

(declare set-editor-font)
(defn dialog-preferences [& {:keys [parent editor-font db-path]
                             :or {parent @gui-frame
                                  editor-font (font/font "MONOSPACED-14")}}]
  (let [text-font (text :text (utils/font-to-font-spec editor-font))
        db-path (locate-db-path)]
    (custom-dialog
     :parent parent
     :title "Preferences"
     :content
     (mig-panel
      :constraints [""
                    "[align right] [fill, grow] []"
                    ""]
      :items [[(label :text "Editor font:") ""]
              [text-font "width 150::"]
              [(action :name "Change..."
                       :handler
                       (fn [e]
                         (let [selected-font (-> (font-dialog/font-dialog
                                                  :parent parent
                                                  :font editor-font)
                                                 pack! show!)]
                           (when selected-font
                             (config! text-font
                                      :text (utils/font-to-font-spec selected-font)) ))))
               "wrap"]
              [(label :text "Database:") ""]
              [(text :text db-path :editable? false) "growx, wrap"]
              
              [(action :name "OK"
                       :handler (fn [e]
                                  (set-editor-font (text text-font))
                                  (return-from-dialog e :OK)))
               "gapleft push, span, split 2"]
              [(action :name "Cancel"
                       :handler #(return-from-dialog % nil))
               "wrap"] ] ))))

;; (-> (dialog-preferences) pack! show!)

(declare on-timer-save-note save-note save-note-timer)

(defn on-timer-save-note [e]
  (debug "on-timer-save-note")
  (save-note)
  (.stop save-note-timer) )

(def save-note-delay-time 500)
(def save-note-timer
  (timer on-timer-save-note
         :delay save-note-delay-time
         :initial-delay save-note-delay-time
         :start? false))

(declare get-editor-font)
(defn window-closing [e]
  (info "")
  (info "Closing application")
  (let [frame (to-root e)
        width  (.width (config frame :size))
        height (.height (config frame :size))
        divider-location (config (select frame [:#split-list-note]) :divider-location)
        divider-location-editor (config (select frame [:#split-edit-view]) :divider-location)
        editor-font (get-editor-font)

        ;; Clear bit 0 to avoid saving minimized state
        frame-ext-state (bit-clear (.getExtendedState frame) 0)]

    ;; Only save window size if state is NORMAL
    (when (= frame-ext-state java.awt.Frame/NORMAL)
      (set-pref "width" width)
      (set-pref "height" height) )

    (set-pref "divider-location" divider-location)
    (set-pref "divider-location-editor" divider-location-editor)
    (set-pref "frame-ext-state" frame-ext-state)
    (set-pref "font-name" (.getName editor-font))
    (set-pref "font-size" (.getSize editor-font))

    (save-note) ))

(defn gui-repopulate-list []
  (debug "gui-repopulate-list")
  (let [new-list-model (db/get-note-list)
        list (select @gui-frame [:#list])
        old-ix (.getSelectedIndex list)
        max-ix (dec (count new-list-model))
        new-ix (min old-ix max-ix)]
    (debug (format "  new-ix=%d, count=%d" new-ix (count new-list-model)))
    (config! list :model new-list-model)
    (when (> new-ix -1)
      (debug "  Set new ix" new-ix)
      (.setSelectedIndex list new-ix) )))

(defn add-note [e]
  (let [title-suggestion (format "Note %d" (inc (db/number-of-notes)))
        title (input "Title" :value title-suggestion)]
    (db/add-note title (str "# " title))
    (gui-repopulate-list) ))

(defn rename-note [e]
  (let [list (select (to-root e) [:#list])
        {:keys [id title]} (selection list)
        new-title (input "Title" :value title)]
    (when new-title
      (db/update-note id {:title new-title})
      (gui-repopulate-list) )))

(defn remove-note [e]
  (let [list (select (to-root e) [:#list])
        {:keys [id title]} (selection list)
        dialog-answer (-> (dialog
                           :content (format "Deleting note %s\nAre you sure?" title)
                           :option-type :yes-no)
                          pack! show!) ]
    (when (= dialog-answer :success)
      (db/remove-note id)
      (gui-repopulate-list) )))

(def a-rename-note (action
                    :handler rename-note
                    :mnemonic "N"
                    :name "Rename note"
                    :key "F2"
                    :tip "Rename note"
                    :icon "SaveAs24.gif"))

(def a-add-note (action
                 :handler add-note
                 :mnemonic "A"
                 :name "Add note"
                 :key  "menu N"
                 :tip  "Add a new note"
                 :icon "Add24.gif"))

(def a-remove-note (action
                    :handler remove-note
                    :mnemonic "R"
                    :name "Remove note"
                    :tip  "Remove the note"
                    :icon "Remove24.gif"))

(declare a-undo a-redo)
(defn update-undo-button-status []
  (if (.canUndo @undo-manager)
    (config! a-undo :enabled? true)
    (config! a-undo :enabled? false))
  (if (.canRedo @undo-manager)
    (config! a-redo :enabled? true)
    (config! a-redo :enabled? false)) )

(defn undo [e]
  (when (.canUndo @undo-manager)
    (.undo @undo-manager)
    (update-undo-button-status)))

(defn redo [e]
  (when (.canRedo @undo-manager)
    (.redo @undo-manager)
    (update-undo-button-status)) )

(def a-undo (action
             :name "Undo"
             :handler undo
             :mnemonic "U"
             :key "menu Z" :icon "Undo24.gif"))

(def a-redo (action :name "Redo"
                    :handler redo
                    :mnemonic "R"
                    :key "menu Y"
                    :icon "Redo24.gif"))

(def a-about (action :name "About"
                     :handler #(-> (dialog-about %) pack! show!)
                     :mnemonic "A"
                     :icon "About24.gif"))

(def a-examples
  (action :name "Recreate example notes"
          :mnemonic "R"
          :icon "Import24.gif"
          :handler (fn [_]
                     (db/create-example-notes)
                     (gui-repopulate-list))))

(def a-preferences
  (action :name "Preferences"
          :mnemonic "P"
          :icon "Preferences24.gif"
          :handler
          (fn [e]
            (-> (dialog-preferences :parent (to-root e)
                                    :editor-font (get-editor-font))
                pack! show!)) ))

(def a-exit
  (action :name "Exit"
          :key "menu Q"
          :mnemonic "X"
          :icon "Exit24.gif"
          :handler
          (fn [e]
            (.dispatchEvent
             (to-frame e)
             (new WindowEvent (to-frame e) WindowEvent/WINDOW_CLOSING))) ))

(defn on-mouse-pressed-list [e]
  (let [list (to-widget e)
        x (.x (.getPoint e))
        y (.y (.getPoint e))]
    (debug "on-mouse-pressed-list")
    (debug "  Button:" (.getButton e))
    (debug "  Popup trigger:" (.isPopupTrigger e))
    (debug "  Clicks:" (.getClickCount e))
    (debug "  x:" x)
    (debug "  y:" y)

    (when (.isPopupTrigger e)
      (.setSelectedIndex list (.locationToIndex list (.getPoint e)))
      (let [item (selection list)]
        (.show (popup :items [a-rename-note a-add-note a-remove-note])
               list x y))) ))

(defn gui-update-note-text [md-text]
  (let [text-widget (select @gui-frame [:#editor])]
    (config! text-widget :text md-text)) )

(defn gui-update-note-html [md-html]
  (let [md-html (format html-template "Titel" (get-css) md-html)
        html-widget (select @gui-frame [:#html-view])
        raw-widget  (select @gui-frame [:#raw-view])
        scroll-pos  (-> html-widget
                        .getParent .getParent
                        .getVerticalScrollBar .getModel .getValue)]
    (config! html-widget :text md-html)
    (config! raw-widget :text md-html)

    (invoke-later (.setValue (-> html-widget
                                 .getParent .getParent .getVerticalScrollBar .getModel)
                             scroll-pos)) ))

(defn save-note []
  (let [text    (config (select @gui-frame [:#editor]) :text)
        listbox-id (:id (selection (select @gui-frame [:#list])))]
    (debug (format "Save note [id=%d]" @current-note-id))
    (println "save-note:   @id = " @current-note-id "   list-id = " listbox-id)
    (db/update-note-text @current-note-id text)
    (gui-update-note-html (md/md-to-html-string text))) )

(defn new-selection [e]
  (debug "new-selection: e:" (selection e))
  (let [new-note-id (:id (selection e))
        new-note-text (:text (db/get-note new-note-id))
        {:keys [html-view editor]} (group-by-id (to-frame e))]
    (save-note)
    (reset! current-note-id new-note-id)
    
    (gui-update-note-text new-note-text)
    (gui-update-note-html (md/md-to-html-string new-note-text))
    (.discardAllEdits @undo-manager)
    (scroll! html-view :to :top)
    (scroll! editor :to :top) ))

(defn editor-key-typed [e]
  (debug "editor-key-typed")
  (trace "  " (str e))
  (debug "  " (.getKeyCode e))
  (debug "  " (java.awt.event.KeyEvent/getKeyText (.getKeyCode e)))
  (update-undo-button-status)
  (.restart save-note-timer))

(defn set-editor-font
  ([font-name font-size]
   (set-editor-font (str font-name "-" font-size)))
  ([font-spec]
   (when @gui-frame
     (let [editor (select @gui-frame [:#editor])]
       (config! editor :font (font/font font-spec)) ))) )

(defn get-editor-font []
  (when @gui-frame
    (let [editor (select @gui-frame [:#editor])]
      (config editor :font) )))

(defn list-select-by-ix [ix]
  (when @gui-frame
    (let [list (select @gui-frame [:#list])]
      (selection! list (.get (config list :model) ix)))))

(defn note-list-renderer [renderer info]
  (let [v (:value info)]
    (config! renderer :text (:title v))))

(defn editor []
  (scrollable (text :id :editor
                    :multi-line? true
                    :wrap-lines? true
                    :font "MONOSPACED-14"
                    :text "")))

(defn html-view []
  (scrollable (editor-pane
               :id :html-view
               :content-type "text/html"
               :editable? false
               :text "")))

(defn raw-view []
  (scrollable (text :id :raw-view
                    :multi-line? true
                    :editable? false
                    :wrap-lines? true
                    :text "")))

(defn split-panel-layout-t-b []
  (top-bottom-split
   (html-view)
   (editor)
   :divider-location (read-string (get-pref "divider-location-editor" 1/2))
   :id :split-edit-view))

(defn make-window-frame []
  (frame
   :title program-title
   :on-close :dispose
   :icon "icon_md_note_keeper.png"
   :menubar (menubar
             :items
             [(menu :text "File"  :mnemonic "F"
                    :items [a-add-note a-preferences a-exit])
              (menu :text "Edit" :mnemonic "E"
                    :items [a-undo a-redo])
              (menu :text "Help" :mnemonic "H"
                    :items [a-about a-examples]) ])
   :content (border-panel
             :north (toolbar :id :toolbar
                             :items [a-add-note a-undo a-redo])
             :center
             (left-right-split
              (scrollable (listbox :id :list
                                   :border "Notes"
                                   :model nil
                                   :renderer note-list-renderer))
              
              (tabbed-panel
               :id :tabs
               :placement :top
               :tabs [{:title "Note"
                       :tip   "View and edit note"
                       :content (split-panel-layout-t-b)}
                      {:title "Raw HTML"
                       :tip   "View the generated HTML of the MD note"
                       :content (raw-view)} ])
              
              :id :split-list-note
              :divider-location (read-string (get-pref "divider-location" 1/4))))
   ))

(defn add-behaviors [f]
  (let [{:keys [list editor toolbar]} (group-by-id f)
        doc (.getDocument editor)]

    (listen f :window-closing window-closing)
    
    (.addUndoableEditListener doc @undo-manager)
    
    (listen list :mouse-pressed on-mouse-pressed-list)

    ;; Disable text on buttons in the toolbar
    (config! (select toolbar [:JButton]) :text "")
    
    (listen list :selection new-selection)
    (listen editor :key-typed editor-key-typed)
    f))

(defn -main [& args]
  (info "")
  (info "Starting " program-title (get-app-version))
  (info "  Mode: " (environ/env :md-note-keeper-mode))
  (info " " (get-app-pref-node))
  (let [db-path (locate-db-path)]
    (when db-path
      (reset! css-file (get-css-file db-path))
      
      (info " " "Using database at: " db-path)
      (info " " "Using css from: " (.getPath @css-file))
      
      (db/set-active-db db-path)
      (when (not (or
                  ;; (= (environ/env :md-note-keeper-mode) "release")
                  (= (environ/env :md-note-keeper-mode) "dev")
                  (= (environ/env :md-note-keeper-mode) "test")))
        (set-pref "db-path" db-path))
      (db/clear-transaction)
      (when-not (.exists (io/file db-path))
        (info " " "Initializing new database")
        (db/reset-db))
      
      (let [width  (read-string (get-pref "width" 300))
            height (read-string (get-pref "height" 400))
            frame-ext-state (read-string (get-pref "frame-ext-state" java.awt.Frame/NORMAL))
            font-name (get-pref "font-name" "MONOSPACED")
            font-size (read-string (get-pref "font-size" 14))]

        (when-not (nil? @gui-frame) (.dispose @gui-frame))
        (reset! gui-frame (add-behaviors (make-window-frame)))
        (gui-repopulate-list)
        (list-select-by-ix 0)
        (set-editor-font font-name font-size)
        (-> @gui-frame pack! show!)
        (config! @gui-frame :size [width :by height])
        (.setExtendedState @gui-frame frame-ext-state)
        (update-undo-button-status)
        ))) )

;; (-main)

