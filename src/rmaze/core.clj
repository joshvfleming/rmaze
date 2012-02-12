(ns rmaze.core
  (:use [clojure.data.priority-map :only [priority-map]])
  (:require [clojure.string :as str])
  (:import [java.io FileReader BufferedReader]))

(defrecord Edge [to cost])
(defrecord Vertex [name edges])
(defrecord Graph [vertices])

(defn unroll-path
  [goal parents]
  (loop [current goal
         path '()]
    (if (nil? current)
      path
      (recur (parents current)
             (conj path current)))))

(defn vertex-from-edge
  [graph edge]
  (nth (:vertices graph) (:to edge)))

(defn bfs
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

(defn neighbors
  "Find the neighbors for a given vertex by position."
  [width height pos]
  (let [count (* width height)
        row-pos (mod pos width)
        at-row-bound? (or (zero? row-pos)
                          (= row-pos (dec width)))
        n [(- pos width)
           (+ pos width)
           (if at-row-bound? (- pos 1) -1)
           (if at-row-bound? (+ pos 1) -1)]]
    (filter #(and (>= % 0) (< % count)) n)))

(defn random-cost
  [vertex-count]
  (rand-int (* vertex-count 10)))

(defn generate-maze
  [width height]
  (let [vertex-count (* width height)
        empty-graph (Graph. (vec (take vertex-count (repeat (Vertex. nil [])))))]
    (loop [graph empty-graph
           in-maze #{}
           frontier (priority-map 0 0)]
      (let [pos (first (peek frontier))]
        (if (nil? pos)
          graph
          (let [vertex (nth (:vertices graph) pos)
                vertex (assoc vertex :name pos)
                ns (neighbors width height pos)
                ns (for [n (filter #(not (in-maze %)) ns)]
                     [n (random-cost vertex-count)])
                edges (concat (map #(Edge. (first %) (second %)) ns))
                vertex (update-in vertex [:edges] #(concat % edges))]
            (recur (assoc-in graph [:vertices pos] vertex)
                   (conj in-maze pos)
                   (if (empty? ns)
                     (pop frontier)
                     (apply conj (pop frontier) ns)))))))))
