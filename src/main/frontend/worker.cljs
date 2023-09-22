(ns frontend.worker
  (:require ["@sqlite.org/sqlite-wasm" :as wasm]))

(defn init
  []
  (prn :debug "Worker init"))
