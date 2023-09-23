(ns frontend.worker
  (:require ["@sqlite.org/sqlite-wasm" :as wasm]
            [frontend.worker.storage :as storage]
            [datascript.core :as d]
            [promesa.core :as p]))

(def sqlite3InitModule (.-default wasm))
(def base-url (str js/self.location.protocol "//" js/self.location.host))
(prn :base-url base-url)

;; 1. without opfs

(defn- without-opfs-test
  []
  (->
   (p/let [sqlite (sqlite3InitModule (clj->js {:url (str base-url "/js/sqlite-wasm/")
                                               :print js/console.log
                                               :printErr js/console.error}))]
     (let [DB (.-DB (.-oo1 sqlite))
           db (DB. "/mydb.sqlite3" "ct")]
       (prn :debug "sqlite without opfs:")
       (js/console.dir db)))
   (p/catch (fn [error]
              (js/console.error error)))))

(defonce *debug-db (atom nil))
(defn- opfs-pool-test
  []
  (->
   (p/let [sqlite (sqlite3InitModule (clj->js {:url (str base-url "/js/sqlite-wasm/")
                                               :print js/console.log
                                               :printErr js/console.error}))
           pool (.installOpfsSAHPoolVfs sqlite #js {:name "debug-db"
                                                    :initialCapacity 3
                                                    :verbosity 2})
           db (new (.-OpfsSAHPoolDb pool) "/sqlite-test")
           storage (storage/sqlite-storage db nil)]
     (.exec db "PRAGMA locking_mode=exclusive")
     (.exec db "create table if not exists kvs (addr INTEGER primary key, content TEXT)")
     (let [conn (or (d/restore-conn storage)
                    (d/create-conn nil {:storage storage}))]
       (prn "(Before transact) Entity 10 data: " (:data (d/entity @conn 10)))
       (d/transact! conn [{:db/id 10
                           :data 1}])
       (prn "Entity 10 data: " (:data (d/entity @conn 10)))))

   (p/catch (fn [error]
              (js/console.error error)))))

(defn init []
  (opfs-pool-test)
  (js/self.addEventListener "message"
                            (fn [^js e]
                              (prn "Received data: " (.. e -data))
                              (js/postMessage (.. e -data)))))
