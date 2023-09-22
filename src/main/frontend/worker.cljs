(ns frontend.worker
  (:require ["@sqlite.org/sqlite-wasm" :as wasm]
            [promesa.core :as p]))

(defn init
  []
  (prn :debug "Worker init"))

(comment
  (def sqlite3InitModule (.-default wasm))
  (->
   (p/let [base-url (str js/window.location.protocol "//" js/window.location.host)
           sqlite (sqlite3InitModule (clj->js {:url (str base-url "/js/sqlite-wasm/")
                                               :print js/console.log
                                               :printErr js/console.error}))]
     (js/console.dir (.-version sqlite)))
   (p/catch (fn [error]
              (js/console.error error)))))
