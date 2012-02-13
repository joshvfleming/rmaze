(ns rmaze.search
  (:require [rmaze.core])
  (:use [rmaze.core :only [vertex-from-edge]])
  (:import [rmaze.core Edge Vertex Graph]))

(defn unroll-path
  "Traverses through discorvery parent relations to construct the path."
  [goal parents]
  (loop [current goal
         path '()]
    (if (nil? current)
      path
      (recur (parents current)
             (conj path current)))))

(defn bfs
  "Performs a breadth-first search of the graph, stopping when the goal is
  found."
  [graph start goal]
  (let [map-parent (fn [parents edges parent]
                     (into parents
                           (for [e edges]
                             [(vertex-from-edge graph e) parent])))]
    (loop [explored-set #{start}
           frontier-set (set (map #(vertex-from-edge graph %) (:edges start)))
           frontier (apply conj clojure.lang.PersistentQueue/EMPTY (:edges start))
           parents (map-parent {} (:edges start) start)]
      (let [next (peek frontier)
            vertex (vertex-from-edge graph next)]
        (if (= vertex goal)
          (unroll-path vertex parents)
          (let [neighbor-filter
                #(and (not (contains? explored-set
                                      (vertex-from-edge graph %)))
                      (not (contains? frontier-set
                                      (vertex-from-edge graph %))))
                neighbors (filter neighbor-filter (:edges vertex))]
            (if (empty? neighbors)
              (recur (conj explored-set vertex)
                     (disj frontier-set vertex)
                     (pop frontier)
                     parents)
              (recur (conj explored-set vertex)
                     (apply conj
                            (disj frontier-set vertex)
                            (map #(vertex-from-edge graph %) neighbors))
                     (apply conj
                            (pop frontier)
                            neighbors)
                     (map-parent parents neighbors vertex)))))))))
