(ns plugin-b.core
  (:gen-class))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))


(defn product
  "Calculate product of a & b"
  [a b]
  (* a b))
