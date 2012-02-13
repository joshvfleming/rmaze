(ns rmaze.view
  (:use [rmaze.generator :only [generate-maze]]
        [rmaze.search :only [bfs]])
  (:require [clojure.string :as str]
            [net.cgrand.enlive-html :as html]))

(html/deftemplate main-template "rmaze/templates/main.html"
  []
  [:.content] "")

(defn maze-to-html
  [g]
  (str "<div class='container'>"
       (apply str
        (map
         (fn [v]
           (let [dir-to-i {:north 0 :east 1 :south 2 :west 3}
                 open-dirs (map #(dir-to-i (:direction %)) (:edges v))
                 closed-dirs (apply disj #{0 1 2 3} open-dirs)]
             (str "<div class='cell "
                  (apply str (map #(str "closed-" % " ") closed-dirs))
                  "'>&nbsp;</div>")))
         (:vertices g)))
       "</div>"))

(defn emit-solution
  [solution]
  (str "<script type='text/javascript'>"
       (str "rmaze.solution = [" (str/join "," (map :name solution)) "];")
       "</script>"))

(defn show
  [session]
  {:headers {"Content-Type" "text/html; charset=utf-8"}
   :body (main-template)
   :session session})

(defn maze
  [session]
  (let [maze (generate-maze 50 40)
        start (first (:vertices maze))
        goal (last (:vertices maze))
        solution (bfs maze start goal)]
    {:headers {"Content-Type" "text/html; charset=utf-8"}
     :body (str (maze-to-html maze) (emit-solution solution))
     :session session}))
