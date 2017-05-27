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
    (async/go-loop [game-info-string (read-line)]
      (let [game-info (clojure.edn/read-string game-info-string)]
        (if-let [state (:state game-info)]
          (do (>! objects-channel (:objects state))
              (Thread/sleep 100)
              (recur (read-line)))
          (println (case (:winner game-info)
                     0 "Left team win!"
                     1 "Right team win!"
                     "No game...")))))))

(defn -main
  [& args]
  (Application/launch com.tail_island.robosight.Visualizer (into-array String args)))
