(ns qt.test.handler
  (:require
    [clojure.test :refer :all]
    [ring.mock.request :refer :all]
    [qt.handler :refer :all]
    [qt.middleware.formats :as formats]
    [muuntaja.core :as m]
    [mount.core :as mount]))

(defn parse-json [body]
  (m/decode formats/instance "application/json" body))

(use-fixtures
  :once
  (fn [f]
    (mount/start #'qt.config/env
                 #'qt.handler/app-routes)
    (f)))

(deftest test-app
  (testing "main route"
    (let [response ((app) (request :get "/"))]
      (is (= 200 (:status response)))))

  (testing "not-found route"
    (let [response ((app) (request :get "/invalid"))]
      (is (= 404 (:status response)))))


  (testing "Test Booking for Hipster returns expected response for pink cars"
  (let [response ((app) (request :get  "/booktrip?uid=abc&lat=123&lng=32&type=hipster"))
        body     (:body response)]
    (is (= (:status response) 200))))

  (testing "Test GET request to list of available cars"
  (let [response ((app) (request :get  "/listtaxis"))
        body     (:body response)]
    (is (= (:status response) 200))))

  (testing "Test Booking for Hipster returns expected response for pink cars"
  (let [response ((app) (request :get  "/booktrip?uid=abc&lat=23&lng=22&type=hipster"))
        body     (:body response)]
    (is (= (:status response) 200))))

  (testing "Test Booking for Hipster returns expected response for no cars"
  (let [response ((app) (request :get  "/booktrip?uid=abc&lat=13&lng=32&type=hipster"))
        body     (:body response)]
    (is (= (:status response) 200))))

  (testing "Test GET request to any other user response with a car"
  (let [response ((app) (request :get  "/booktrip?uid=abc&lat=43&lng=38&type=other"))
        body     (:body response)]
    (is (= (:status response) 200))))

  (testing "Test GET request to any other user response with a list of available cars"
  (let [response ((app) (request :get  "/endtrip?id=3&lat=13&lng=3&type=hipster"))
        body     (:body response)]
    (is (= (:status response) 200))))

  (testing "Test GET request to any other user response with a car"
  (let [response ((app) (request :get  "/booktrip?uid=abc&lat=23&lng=142&type=hipster"))
        body     (:body response)]
    (is (= (:status response) 200)))))
