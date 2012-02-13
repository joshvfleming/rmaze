(ns rmaze.server
  (:use [compojure.core]
        [ring.middleware params
         keyword-params
         nested-params
         multipart-params
         cookies
         session]
        [ring.adapter jetty])
  (:require [compojure.route :as route]
            [ring.middleware.session.cookie :as cookie]
            [rmaze.view :as view]))

(def secret-key "j7hfsrKo2uHkJR6b")

(def cookie-store
     (cookie/cookie-store {:key secret-key}))

(defn not-found
  "A route that returns a 404 not found response, with its argument as the
  response body.
  NOTE: This was modified from compojure.core to support content-type header."
  [response]
  (let [resp-with-status (if (map? response)
                           (merge response {:status 404})
                           (assoc {:status 404} :body response))] 
    (routes
     (HEAD "*" [] (dissoc resp-with-status :body))
     (ANY "*" [] resp-with-status))))

(defroutes main-routes
  (GET "/" {session :session}
       (view/show session))
  (GET "/maze" {session :session}
       (view/maze session))
  (route/resources "/")
  (not-found "Sorry, couldn't find that."))

(def app
     (-> main-routes
         wrap-keyword-params
         wrap-nested-params
         wrap-params
         wrap-multipart-params
         (wrap-session {:store cookie-store
                        :cookie-name "rmaze-session"})))

(defn -main [port]
  (run-jetty app {:port (Integer. port)}))