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
           pool (.installOpfsSAHPoolVfs sqlite #js {:name "debug-db"
                                                    :initialCapacity 3
                                                    :clearOnInit true
                                                    :verbosity 2})
           db (.OpfsSAHPoolDb pool "/logseq-opfs-pool.db")]
     (println :debug :sqlite)
     (js/console.dir sqlite)
     (println :debug :pool)
     (js/console.dir pool)
     (prn "opfs-sahpool successfully installed")
     (println :debug :db)
     (js/console.dir db))
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
