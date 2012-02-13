(ns rmaze.core)

(defrecord Edge [to direction cost])
(defrecord Vertex [name edges])
(defrecord Graph [vertices])

(defn vertex-from-edge
  [graph edge]
  (nth (:vertices graph) (:to edge)))
