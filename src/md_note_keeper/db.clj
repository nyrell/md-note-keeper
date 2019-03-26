(ns md-note-keeper.db
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.java.io :as io]
            [clojure.xml :as xml]) )

;;----------------------------------------------------------------------
;; DB specification
;;----------------------------------------------------------------------
(def db-path (atom nil))

(defn set-active-db [path]
  (reset! db-path path))

(defn get-db-spec []
  {:classname   "org.sqlite.JDBC"
   :subprotocol "sqlite"
   :subname @db-path})



;;----------------------------------------------------------------------
;; DB transaction handling. Makes it possible to reuse a connection
;; regardless where it was started.
;;----------------------------------------------------------------------
(def transaction-connection (atom nil))

;; Return the db-spec or a transaction-connection
(defn get-db-connection []
  (or @transaction-connection
      (get-db-spec)))

;; Register a transaction connection started by "jdbc/with-db-transaction [t-con db-spec]"
(defn register-transaction [t-con]
  (reset! transaction-connection t-con))

;; Reset the transaction. Should be done before inside "jdbc/with-db-transaction"
(defn clear-transaction []
  (reset! transaction-connection nil))



;;----------------------------------------------------------------------
;; DB construction and destruction
;;----------------------------------------------------------------------
(defn create-tables []
  (jdbc/db-do-commands
   (get-db-connection)
   [(jdbc/create-table-ddl :notes
                           [[:id :integer :primary :key :autoincrement]
                            [:title :text]
                            [:text :text]
                            [:ix :integer] ]
                           {:conditional? true})
    (jdbc/create-table-ddl :config
                           [;;[:id :integer :primary :key :autoincrement]
                            [:name :text :primary :key]
                            [:value :text]]
                           {:conditional? true})
    ]) )

(defn drop-tables []
  (jdbc/db-do-commands (get-db-connection)
                       [(jdbc/drop-table-ddl :notes {:conditional? true})
                        ;;(jdbc/drop-table-ddl :config {:conditional? true})
                        ]) )



;;----------------------------------------------------------------------
;; Functions for notes
;;----------------------------------------------------------------------
(defn number-of-notes []
  (first (vals (first (jdbc/query (get-db-connection)
                                  ["SELECT count(id) from notes"])))))

(defn add-note [title text]
  (let [next-ix (number-of-notes)]
    (first (vals (first (jdbc/insert! (get-db-connection) :notes
                                      {:title title :text text :ix next-ix}) )))
    ) )

(defn remove-note [id]
  (jdbc/delete! (get-db-connection) :notes [(format "id = %d" id)]))

(defn update-note [id values]
  (jdbc/update! (get-db-connection) :notes
                values
                [(format "id = %d" id)]))

(defn update-note-text [id text]
  (first (update-note id {:text text})))
;;(update-note-text 1 "kalle kalle")

(defn update-note-title [id title]
  (first (update-note id {:title title})))
;;(update-note-title 1 "kalle")

(defn get-note [id]
  (first (jdbc/query (get-db-connection) [(format "SELECT * from notes where id = %d" id)])))
;;(get-note 1)

(defn get-note-list []
  (jdbc/query (get-db-connection) ["SELECT id, ix, title from notes"]))



;;----------------------------------------------------------------------
;; Functions for configuration stored in the DB
;;----------------------------------------------------------------------
(defn set-conf [name value]
  ;; (println "  set-conf: " name "=" value)
  (let [];;name "width", value "321"]
    (jdbc/delete! (get-db-connection) :config [(format "name='%s'" name)])
    (first (vals (first (jdbc/insert! (get-db-connection) :config
                                      {:name name :value (str value)}) )))) )
  
(defn get-conf [name default]
  (let [;;name "width", default "300"
        {:keys [conf_exist value] :as result}
        (first (jdbc/query (get-db-connection)
                           [(format "SELECT count(*) as conf_exist, value from config where name='%s'" name)])) ]
    (if (zero? conf_exist) (str default) value)) )



;;----------------------------------------------------------------------
;; Functions for setting up a new db with example notes
;;----------------------------------------------------------------------
(defn create-example-notes []
  (add-note
   "Welcome"
   (slurp (io/resource "README.md")))
  
  (add-note
   "Markdown examples"
   (slurp (io/resource "example.md")) ) )



;;----------------------------------------------------------------------
;; Functions for XML-import
;;----------------------------------------------------------------------

(defn xml-import-note [xml]
  (let [{title :title :or {title "note"}} (:attrs xml)
        contents (:content xml)
        text (first contents)
        note-id (add-note title text)]
    ;; (when contents
    ;;   (println contents))
    note-id) )

(defn xml-import [path-to-xml]
  (time (jdbc/with-db-transaction [t-con (get-db-spec)]
          (println "\n\nImporting XML from: " path-to-xml)
          (register-transaction t-con)
          (let [all-items-xml (:content (-> path-to-xml io/resource io/file xml/parse))]
            (doseq [item all-items-xml]
              (println (:tag item) item "\n\n")
              (cond
                (= (:tag item) :note) (xml-import-note item) )))
          (clear-transaction)
          )))

;; (.exists (-> "import.xml" io/resource io/file))
;; (.exists (-> "import.xml" io/file))

(defn reset-db []
  (clear-transaction)
  (drop-tables)
  (create-tables)
  (create-example-notes)
  ;;(xml-import "import.xml")
  )

(defn init-new-db []
  (create-example-notes))

