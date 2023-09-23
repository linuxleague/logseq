(ns frontend.worker
  (:require ["@sqlite.org/sqlite-wasm" :as wasm]
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

(defn- opfs-pool-test
  []
  (->
   (p/let [sqlite (sqlite3InitModule (clj->js {:url (str base-url "/js/sqlite-wasm/")
                                               :print js/console.log
                                               :printErr js/console.error}))
           pool (.installOpfsSAHPoolVfs sqlite #js {;; :name "debug-db"
                                                    ;; :initialCapacity 3
                                                    ;; :verbosity 2
                                                    })
           db (new (.-OpfsSAHPoolDb pool) "/sqlite-test")]
     (println :debug :pool)
     (js/console.dir pool)
     (println :debug :db)
     (js/console.dir db)
     (prn "opfs-sahpool successfully installed")
     (.exec db "PRAGMA locking_mode=exclusive")
     (.exec db "drop table if exists kvs"))

   (p/catch (fn [error]
              (js/console.error error)))))


(do
  (prn :debug "test opfs pool")
  (opfs-pool-test))

(defn init []
  (js/self.addEventListener "message"
                            (fn [^js e]
                              (prn "Received data: " (.. e -data))
                              (js/postMessage (.. e -data)))))
