(ns rmaze.generator
  (:require [rmaze.core])
  (:use [rmaze.core :only [vertex-from-edge]]
        [clojure.data.priority-map :only [priority-map]])
  (:import [rmaze.core Edge Vertex Graph]))

(defn neighbors
  "Find the neighbors for a given vertex by position."
  [width height pos]
  (let [count (* width height)
        row-pos (mod pos width)
        at-left-bound? (zero? row-pos)
        at-right-bound? (= row-pos (dec width))
        n [{ :pos (- pos width) :direction :north }
           { :pos (+ pos width) :direction :south }
           { :pos (if at-right-bound? -1 (+ pos 1)) :direction :east }
           { :pos (if at-left-bound? -1 (- pos 1)) :direction :west }]]
    (filter #(and (>= (:pos %) 0) (< (:pos %) count)) n)))

(def reverse-direction
     {:north :south,
      :east :west,
      :south :north,
      :west :east })

(defn random-cost
  "Generates a random cost."
  [vertex-count]
  (rand-int (* vertex-count 10)))

(defn create-empty-graph
  "Creates a full graph with no edges."
  [vertex-count]
  (Graph.
   (vec
    (map #(Vertex. % [])
         (take vertex-count
               (iterate inc 0))))))

(defn generate-maze
  "Generates a maze of given width and height using a randomized form of Prim's
  algorithm."
  [width height]
  (let [vertex-count (* width height)]
    (loop [graph (create-empty-graph vertex-count)
           in-maze #{0}
           frontier (priority-map 0 0)]
      (if-let [pos (first (peek frontier))]
        (let [vertex (nth (:vertices graph) pos)
              ns (neighbors width height pos)
              ns (for [n (filter #(not (in-maze (:pos %))) ns)]
                   (assoc n :cost (random-cost vertex-count)))
              edges (map #(Edge. (:pos %) (:direction %) (:cost %)) ns)
              vertex (update-in vertex [:edges] #(concat % edges))
              ; add in reverse edges also
              vertices (for [e edges]
                         (update-in
                          (vertex-from-edge graph e)
                          [:edges]
                          #(conj % (Edge. pos
                                          (reverse-direction (:direction e))
                                          (:cost e)))))
              vertices (conj vertices vertex)
              vertices (apply assoc (:vertices graph)
                              (flatten (map #(vec [(:name %) %]) vertices)))]
          (if (empty? ns)
            (recur graph in-maze (pop frontier))
            (recur (assoc-in graph [:vertices] vertices)
                   (apply conj in-maze (map #(:pos %) ns))
                   (apply conj
                          (pop frontier)
                          (map #(vec [(:pos %) (:cost %)]) ns)))))
      graph))))
