(ns modular-api-clj.plugin.plugin-a
  (:require [plugin-a.core :as core]))

(defn ^:modular-api-clj.plugin/hook handler 
  [request]
  (let [{:keys [a b]} (:params request)]
    (core/sum (Double/parseDouble a)
              (Double/parseDouble b))))