(ns rmaze.view
  (:use [rmaze.generator :only [generate-maze]]
        [rmaze.search :only [bfs]])
  (:require [clojure.string :as str]
            [clojure.data.json :as json]
            [net.cgrand.enlive-html :as html]))

(html/deftemplate main-template "rmaze/templates/main.html"
  []
  [:.content] "")

(def direction-map
     {:north 0,
      :east 1,
      :south 2,
      :west 3 })

(defn show
  [session]
  {:headers {"Content-Type" "text/html; charset=utf-8"}
   :body (main-template)
   :session session})

(defn maze
  [session]
  (let [maze (generate-maze 30 30)
        start (first (:vertices maze))
        goal (last (:vertices maze))
        solution (bfs maze start goal)]
    {:headers {"Content-Type" "application/json; charset=utf-8"}
     :body (json/json-str
            {:maze (map (fn [v]
                          (map #(direction-map (:direction %))
                               (:edges v)))
                        (:vertices maze))
             :solution (map :name solution)})
     :session session}))
