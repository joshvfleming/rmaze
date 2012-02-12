(ns rmaze.core
  (:use [clojure.data.priority-map :only [priority-map]])
  (:require [clojure.string :as str])
  (:import [java.io FileReader BufferedReader]))

(defrecord Edge [to direction cost])
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
        n [{ :pos (- pos width) :direction :north }
           { :pos (+ pos width) :direction :south }
           { :pos (if at-row-bound? (- pos 1) -1) :direction :east }
           { :pos (if at-row-bound? (+ pos 1) -1) :direction :west }]]
    (filter #(and (>= (:pos %) 0) (< (:pos %) count)) n)))

(defn random-cost
  [vertex-count]
  (rand-int (* vertex-count 10)))

(defn generate-maze
  [width height]
  (let [vertex-count (* width height)
        empty-graph (Graph. (vec (take vertex-count (repeat (Vertex. nil [])))))]
    (loop [graph empty-graph
           in-maze #{0}
           frontier (priority-map 0 0)]
      (if-let [pos (first (peek frontier))]
        (let [vertex (nth (:vertices graph) pos)
              vertex (assoc vertex :name pos)
              ns (neighbors width height pos)
              ns (for [n (filter #(not (in-maze (:pos %))) ns)]
                   (assoc n :cost (random-cost vertex-count)))
              edges (map #(Edge. (:pos %) (:direction %) (:cost %)) ns)
              vertex (update-in vertex [:edges] #(concat % edges))]
          (if (empty? ns)
            (recur graph in-maze (pop frontier))
            (recur (assoc-in graph [:vertices pos] vertex)
                   (apply conj in-maze (map #(:pos %) ns))
                   (apply conj
                          (pop frontier)
                          (map #(vec [(:pos %) (:cost %)]) ns)))))
      graph))))

;(def g (generate-maze 10 10))
;(map :name (bfs g (first (:vertices g)) (last (:vertices g))))

(defn maze-to-html
  [g]
  (str "<div class='container'>"
       (apply str
        (map
         (fn [v]
           (let [dir-to-i {:north 0 :east 1 :south 2 :west 3}
                 open-dirs (map #(dir-to-i (:direction %)) (:edges v))
                 closed-dirs (apply disj #{0 1 2 3} open-dirs)]
             (apply str (map
                   (fn [e]
                     (str "<div class='cell "
                          (apply str (map #(str "closed-" % " ") closed-dirs))
                          "'>&nbsp;</div>"))
                   closed-dirs))))
         (:vertices g)))
       "</div>"))

;(println (maze-to-html g))