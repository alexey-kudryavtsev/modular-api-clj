(ns modular-api-clj.plugin.plugin-b
  (:require [plugin-b.core :as core]))

(defn ^:modular-api-clj.plugin/hook handler
  [request]
  (let [{:keys [a b]} (:params request)]
    (core/product (Double/parseDouble a)
                  (Double/parseDouble b))))