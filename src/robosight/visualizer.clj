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
    ((fn [canvas]
       (async/go
         (while true
           (let [objects (<! objects-channel)]
             (Platform/runLater #(robosight.ui/draw (.getGraphicsContext2D canvas) objects)))))))))

(defn -start
  [this stage]
  (let [objects-channel (async/chan)]
    (doto stage
      (.setTitle "robosight")
      (.setResizable false)
      (.setScene (Scene. (Group. [(field objects-channel)])))
      (.show))
    (async/go
      (doseq [objects (map read-string (line-seq (java.io/reader System/in :encoding (System/getProperty "file.encoding"))))]
        (>! objects-channel objects)
        (Thread/sleep 100)))))

(defn -main
  [& args]
  (Application/launch com.tail_island.robosight.Visualizer (into-array String args)))
