(ns governance.oauth
  (:require
    [governance.config :refer [env]]
    [buddy.sign.jwe :as jwe]
    [buddy.sign.jwt :as jwt]
    [buddy.core.keys :as keys]
    [clj-http.client :as http]
    [cheshire.core :as json]
    [mount.core :refer [defstate]]
    [clojure.core.cache.wrapped :as cache]
    [clojure.tools.logging :as log]))

;; One hour cache
(def *jwks-cache (cache/ttl-cache-factory {} :ttl (* 1000 60 60)))

(defn get-jwks
  "Goes and gets the jwks from an endpoint"
  [base-url]
  (some-> (str base-url ".well-known/openid-configuration")
          http/get
          :body
          (json/parse-string true)
          (get :jwks_uri)
          http/get
          :body
          (json/parse-string true)
          (get :keys)))
(defn get-key-inner
  [[base-url kid]]
  (some->> base-url
           get-jwks
           (filter #(= kid (get % :kid)))
           first
           keys/jwk->public-key))
(defn get-key
  "Cache getting key so we're not looking up things often"
  [base-url kid]
  (cache/lookup-or-miss *jwks-cache
                        [base-url kid]
                        get-key-inner))
(defn decrypt-jwt
  [config jwt]
  (let [header (buddy.sign.jwe/decode-header jwt)
        jwk (get-key "https://accounts.google.com/" (:kid header))]
    (jwt/unsign jwt jwk header)))
(defn get-id-from-request
  ;; TODO right now I'm just assuming it's google only, need to fix that later
  ;; TODO note that google has an openid flow, while github has an oauth flow. They're different
  [request]
  (let [goog-key (-> request
                     :session
                     :ring.middleware.oauth2/access-tokens
                     :google
                     :id-token)
        header (some-> goog-key buddy.sign.jwe/decode-header)
        jwk (get-key "https://accounts.google.com/" (:kid header))]
    (:email (some-> goog-key (jwt/unsign jwk header)))))
(defn assoc-request-identity
  [request]
  (if (:session request)
    (assoc-in request [:session :email] (get-id-from-request request))
    request))
(defn wrap-assoc-identity
  "Adds the users' email under [:session :email]"
  [handler]
  (fn [request]
    (handler (assoc-request-identity request))))


(comment
  (get-jwks "https://accounts.google.com/")
  (get-key "https://accounts.google.com/" "d946b137737b973738e5286c208b66e7a39ee7c1"))