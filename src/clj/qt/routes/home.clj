(ns qt.routes.home
  (:require
   [qt.layout :as layout]
   [clojure.java.io :as io]
   [qt.middleware :as middleware]
   [ring.util.response]
   [clojure.data.json :as json]
   [ring.util.http-response :as response]))


(def cars_arr [
          {:id 1 :color "red" :lat 12 :lng 14 :busy false}
          {:id 2 :color "blue" :lat 121 :lng 124 :busy false}
          {:id 3 :color "pink" :lat 32 :lng 44 :busy false}
          {:id 4 :color "red" :lat 51 :lng 224 :busy false}
          {:id 5 :color "pink" :lat 22 :lng 94 :busy false}])

(def cars (atom cars_arr))

(defn mark-status
"Marks status for a car as true or false as define dby the status variable"
 [item status]
        (swap! cars
         (fn [cars_arr]
           (map #(if (= (:id item) (:id %)) (assoc % :busy status) %)
                cars_arr))))

(defn mark-latitude
"Marks latitude for a car as true or false as define dby the latitude variable"
 [item latitude]
  (swap! cars
         (fn [cars_arr]
           (map #(if (= (:id item) (:id %)) (assoc % :lat latitude) %)
                cars_arr))))

(defn mark-longitude
"Marks longitude for a car as true or false as define dby the longitude variable"
 [item longitude]
  (swap! cars
         (fn [cars_arr]
           (map #(if (= (:id item) (:id %)) (assoc % :lng longitude) %)
                cars_arr))))

(defn get-car [x] (filter #(= x (:color %)) @cars))

(defn distance 
  "calculates the distance between the taxi and the user and sort it by distance"
  [p]
    (let [free_cars (filter #(= false (:busy %)) @cars)]
     (sort-by :dist 
       (map #(conj % {:dist (let [dx (- (:lng p) (:lng %))
                                dy (- (:lat p) (:lat %))] 
                (Math/sqrt (+ (* dx dx) (* dy dy))))})
           
             (if (= "hipster" (:type p)) 
                 (filter #(= (:color %) "pink") free_cars) 
                 free_cars)))))

(defn home-page [request]
  (layout/render request "home.html"))

(defn list-taxis
  "List taxis available"
 [request]
 (try
  (let [{:keys [id type lat lng uid]} (-> request :params)
        resp-data {:status "SUCCESS" :data (filter #(= false (:busy %)) @cars)}]
    (println (json/write-str resp-data))

    (-> (response/ok (json/write-str resp-data))
        (response/header "Content-Type" "text/plain; charset=utf-8")))
  (catch Exception e (str "caught exception: " (.getMessage e)))))

(defn end-trip 
  "Ends trip for a car identified by id"
 [request]
 (try
  (let [{:keys [id type lat lng uid]} (-> request :params)
        free-taxi (first (filter #(= (Integer/parseInt id) (:id %)) @cars))
        lat-data (mark-latitude free-taxi (Integer/parseInt lat))
        lng-data (mark-longitude free-taxi (Integer/parseInt lng))
        resp-data {:status "SUCCESS" :data (mark-status free-taxi false)}]
    (println (str "Free taxi -> " free-taxi))
    (println (json/write-str resp-data))

    (-> (response/ok (json/write-str resp-data))
        (response/header "Content-Type" "text/plain; charset=utf-8")))
  (catch Exception e (str "caught exception: " (.getMessage e)))))

(defn book-trip 
  "Books a free taxi depending, if a hispter only pink car will be booked" 
  [request]
  (try
    (let [{:keys [id type lat lng uid]} (-> request :params)
          free-cars (distance {:id id
                                    :type type
                                    :uid uid
                                    :lng (Integer/parseInt lng)
                                    :lat (Integer/parseInt lat)})
          taxi-booked (first free-cars)
          current-list (mark-status taxi-booked true)
          resp-data (if (= 0 (count free-cars))
                      {:status "SUCCESS" :data "No cars"}
                      {:status "SUCCESS" :data taxi-booked})]
      (println resp-data)
      (println (json/write-str resp-data))

      (-> (response/ok (json/write-str resp-data))
          (response/header "Content-Type" "text/plain; charset=utf-8")))
    (catch Exception e (str "caught exception: " (.getMessage e)))))

(defn home-routes []
  [""
   {:middleware [middleware/wrap-csrf
                 middleware/wrap-formats]}
   ["/" {:get home-page}]
   ["/booktrip" {:get book-trip}]
   ["/endtrip" {:get end-trip}]
   ["/listtaxis" {:get list-taxis}]
   ["/docs" {:get (fn [_]
                    (-> (response/ok (-> "docs/docs.md" io/resource slurp))
                        (response/header "Content-Type" "text/plain; charset=utf-8")))}]])



