(ns robosight.visualizer
  (:require   (clojure.core [async :as async :refer [>!! <!! >! <!]])
              (clojure.java [io    :as java.io])
              (robosight    [core  :as robosight]
                            [ui    :as robosight.ui]))
  (:import    (javafx.animation       AnimationTimer)
              (javafx.application     Application Platform)
              (javafx.scene           Group Scene)
              (javafx.scene.canvas    Canvas)
              (javafx.scene.transform Affine))
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
      (loop [objects-string (read-line)]
        (when objects-string
          (>! objects-channel (read-string objects-string))
          (Thread/sleep 100)
          (recur (read-line)))))))

(defn -main
  [& args]
  (Application/launch com.tail_island.robosight.Visualizer (into-array String args)))
