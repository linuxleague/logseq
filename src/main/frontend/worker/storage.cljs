(ns frontend.worker.storage
  (:require [datascript.storage :refer [IStorage]]
            [clojure.edn :as edn]))

(defn sqlite-storage
  [db _opts]
  (reify IStorage
    (-store [_ addr+data-seq]
      (prn :debug :store {:addr-data addr+data-seq})
      (doseq [[addr data] addr+data-seq]
        (let [content (pr-str data)]
          (.exec db
                 #js {:sql "insert into kvs (addr, content) values (?, ?) on conflict(addr) do update set content = ?"
                      :bind #js [addr content content]}))))
    (-restore [_ addr]
      (let [result (when-let [content (-> (.exec db #js {:sql "select content from kvs where addr = ?"
                                                         :bind #js [addr]
                                                         :rowMode "array"})
                                          ffirst)]
                     (edn/read-string content))]
        (prn :debug :restore {:addr addr
                              :result result})
        result))))
