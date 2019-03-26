(ns md-note-keeper.old)

(def db-name "md_note_keeper.db")

(defn dialog-select-note-file-old []
  (let [home-dir (System/getProperty "user.home")
        file-separator (System/getProperty "file.separator")
        default-db-path (str home-dir file-separator default-db-file-name)]
    (input "Select a file in which to store all your notes:" :title "Select note file" :value default-db-path :type :question)) )

(defn get-config-dir []
  (let [app-cfg-dir-name "md_note_keeper"
        xdg-cfg-dir (System/getenv "XDG_CONFIG_HOME")
        file-separator (System/getProperty "file.separator")
        os-name (clojure.string/lower-case (System/getProperty "os.name"))
        windows? (.contains os-name "win")
        windows-cfg-dir (str "\\AppData\\Roaming\\")
        mac? (.contains os-name "mac")
        mac-cfg-dir "/Library/Application Support/"
        home-dir (System/getProperty "user.home")]
    (if xdg-cfg-dir
      (str xdg-cfg-dir file-separator app-cfg-dir-name)
      (if windows?
        (str home-dir windows-cfg-dir app-cfg-dir-name file-separator)
        (if mac?
          (str home-dir mac-cfg-dir app-cfg-dir-name file-separator)
          (str home-dir "/.config/" app-cfg-dir-name file-separator))))) )



(.getPath (io/file (get-app-db-path)))
;; => /home/matny/.config/md_note_keeper/md_note_keeper.db

(.getName (io/file (get-app-db-path)))
;; => md_note_keeper.db

(.getParent (io/file (get-app-db-path)))
;; => /home/matny/.config/md_note_keeper


(get-app-db-path)
;; => /home/matny/.config/md_note_keeper/md_note_keeper.db

(.exists (io/file ""))

(def my-file (io/file "/home/matny/.config/md_note_keeper/md_note_keeper.db"))
(def my-file-dir (.getParentFile my-file))

(.exists my-file-dir)
(.mkdir my-file-dir)
(.delete my-file-dir)

(.exists my-file)
(.createNewFile my-file)
(.delete my-file)




(.exists (io/file (get-app-db-path)))



(defn print-sys-prop []
  (println (map #(str % ": " (System/getProperty %) "\n")
                ["user.dir" "user.home" "os.arch" "os.name" "os.version" "file.separator"])) )

(defn pref-test []
  (let [prefs (.node (java.util.prefs.Preferences/userRoot) (str (ns-name *ns*)))
        id-1 "Test1"
        id-2 "Test2"
        id-3 "Test3"]

    (println id-1 (.getBoolean prefs id-1 true))
    (println id-2 (.get prefs id-2 "Kalle"))
    (println id-3 (.getInt prefs id-3 10))

    (.putBoolean prefs id-1 false)
    (.put prefs id-2 "Olle")
    (.putInt prefs id-3 42)
    ) )



(.exists (io/file "test.tmp"))
(.createNewFile (io/file "test.tmp"))
(.delete (io/file "test.tmp"))


;; (.setExtendedState @gui-frame java.awt.Frame/MAXIMIZED_BOTH)
;; (.setExtendedState @gui-frame java.awt.Frame/NORMAL)

;; (show-events (left-right-split (text) (text) :divider-location 1/4))
;; (show-events (text))
;; (show-events (listbox))
;; (show-events (popup))

;; (defn gui-switch-page
;;   ([page frame]
;;    (config! frame :content page)
;;    (pack! frame))
;;   ([page] (gui-switch-page page @gui-frame)))



;; java.awt.event.MouseEvent[MOUSE_CLICKED,
;;                           (70,336),
;;                           absolute(1114,375),
;;                           button=1,
;;                           modifiers=Button1,
;;                           clickCount=1]
;; on seesaw.core.proxy$javax.swing.JList$Tag$fd407141[,0,0,99x628,alignmentX=0.0,alignmentY=0.0,
;;                                                     border=javax.swing.border.TitledBorder@72b67618,
;;                                                     flags=50331944,
;;                                                     maximumSize=,minimumSize=,preferredSize=,
;;                                                     fixedCellHeight=-1,fixedCellWidth=-1,
;;                                                     horizontalScrollIncrement=-1,
;;                                                     selectionBackground=javax.swing.plaf.ColorUIResource[r=184,g=207,b=229],
;;                                                     selectionForeground=sun.swing.PrintColorUIResource[r=51,g=51,b=51],
;;                                                     visibleRowCount=8,
;;                                                     layoutOrientation=0]




(select @gui-frame [:#toolbar])


  (-> (select @gui-frame [:#text-box])
      .getParent .getParent .getVerticalScrollBar .getModel .getValue .toString)

  (-> (select @gui-frame [:#text-box])
      .getParent .getParent .getVerticalScrollBar .getModel .getExtent .toString)

  (.setValue (-> (select @gui-frame [:#text-box])
                 .getParent .getParent .getVerticalScrollBar .getModel)
             500)
