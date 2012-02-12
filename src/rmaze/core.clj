(ns rmaze.core
  (:require [clojure.string :as str])
  (:import [java.io FileReader BufferedReader]))

(defrecord Edge [to cost])
(defrecord Vertex [name edges])
(defrecord Graph [vertices])

(defn read-data-line
  "Reads one line of the data file."
  [line]
  (let [vertex (str/split (str/trim line) #"\:")
        edges (str/split (second vertex) #"\,")
        edge-comps (map #(str/split % #"\.") edges)]
    (Vertex. (first vertex)
             (map #(Edge. (Integer/parseInt (first %))
                          (Integer/parseInt (second %)))
                  edge-comps))))

(defn read-data-file
  "Reads a data file, returning an entity count and data points."
  [filename]
  (let [reader (-> filename FileReader. BufferedReader.)
        lines (line-seq reader)]
    (Graph. (map read-data-line lines))))

(def myg (read-data-file "resources/graph1.txt"))

(def arad (nth (:vertices myg) 2))
(def bucharest (nth (:vertices myg) 13))
(def oradea (nth (:vertices myg) 0))
(def neamt (nth (:vertices myg) 12))

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

(map #(:name %) (bfs myg arad bucharest))
