(ns plugin-a.core
  (:gen-class))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))

(defn sum 
  "Calculate sum of a & b"
  [a b]
  (+ a b))
