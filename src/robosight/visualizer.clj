(ns robosight.visualizer
  (:require   (clojure.core [async :as async :refer [>!! <!! >! <!]])
              (robosight    [core  :as robosight]
                            [ui    :as robosight.ui]))
  (:import    (javafx.application  Application Platform)
              (javafx.scene        Group Scene)
              (javafx.scene.canvas Canvas))
  (:gen-class :name    com.tail_island.robosight.Visualizer
              :extends javafx.application.Application
              :main    true))

(defn- field
  [objects-channel]
  (doto (Canvas. robosight/field-size-x robosight/field-size-y)
    (#(async/go
        (while true
          (let [objects (<! objects-channel)]
            (Platform/runLater (fn [] (robosight.ui/draw (.getGraphicsContext2D %) objects)))))))))

(defn -start
  [this stage]
  (let [objects-channel (async/chan)]
    (doto stage
      (.setTitle "robosight")
      (.setResizable false)
      (.setScene (Scene. (Group. [(field objects-channel)])))
      (.show))
    (async/go
      (loop [state-string (read-line)]
        (when state-string
          (let [state (clojure.edn/read-string state-string)]
            (if (coll? state)
              (do (>! objects-channel state)
                  (Thread/sleep 100)
                  (recur (read-line)))
              (println (pr-str state)))))))))

(defn -main
  [& args]
  (Application/launch com.tail_island.robosight.Visualizer (into-array String args)))
