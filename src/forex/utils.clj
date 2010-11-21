(ns forex.utils
  (:import java.util.Calendar)
  (:require [clojure.contrib.def :as d]
	    [clojure.contrib.str-utils2 :as s]))

(defn log [e] (pr "ERROR!: " e))
(defmacro wlog [& body]
  `(try (do ~@body) (catch Exception e# (log e#))))
(defmacro mapc [& args] `(dorun (map ~@args)))
(defmacro thread [& body]
  `(let [thread# (Thread. (bound-fn [] (wlog ~@body)))]
     (.start thread#)
     thread#))


(defmacro throwf [message & args]
  (if args
    `(throw (Exception. (format ~message ~@args)))
    `(throw (Exception. ~message))))
(defmacro is [val & message]
  `(let [result# ~val]
     (if (not result#)
       (throw (Exception. ~(or (first message) (format "assert: %s" (str val)))))
       result#)))

(def split s/split)

(defn group
  ([coll] (group coll 2))
  ([coll by] (partition-all by coll)))

(defmacro doseq* [[& args] & body]
  (let [a (group args 2)
	first-args (map first a)
	second-args (map second a)]
    `(doseq [[~@first-args] (map vector ~@second-args)]
       ~@body)))
;;TODO: make more efficient

(defmacro constants [& args]
  `(do ~@(map #(list 'def (first %) (second %)) (group args))))

(defmacro defonce- [& args] `(d/defonce- ~@args))




(defonce *env* (atom {:timeframe 1440 :index 0})) ;default +D1+
(defn env [key] (key @*env*))
(defn env! [map]
  (swap! *env* merge map))

;;todo: fix private!
;;todo: ignores all nils?
(defmacro wenv [[ & {symbol :symbol socket :socket timeframe :timeframe index :index}] & body]
  `(binding [forex.utils/*env*
	     (atom (merge-with #(or %2 %1) @@~#'*env*
			       {:symbol ~symbol :socket ~socket :timeframe ~timeframe :index ~index}))]
     ~@body))

