(ns bigmouth.routes.well-known-test
  (:require [clojure.spec.test.alpha :as t]
            [clojure.test :refer [deftest is testing]]
            [bigmouth.models.account :as account]
            [bigmouth.models.keystore :as keystore]
            [bigmouth.models.subscription :as subs]
            [bigmouth.routes.well-known :refer [make-well-known-routes]]))

(t/instrument)

(defn test-context [{:keys [id username] :as account}]
  {:accounts (reify account/AccountRepository
               (find-account [_ username']
                 (when (= username username')
                   account))
               (find-account-by-id [_ id']
                 (when (= id id')
                   account)))
   :subscriptions (subs/simple-in-memory-subscription-repository)
   :keystore (keystore/simple-in-memory-keystore)
   :configs {:use-https? false :local-domain "example.com"}})

(deftest make-well-known-routes-test
  (let [context (test-context {:id "0" :username "foo"})
        handler (make-well-known-routes context)]
    (testing "GET /.well-known/host-meta"
      (let [res (handler {:request-method :get
                          :uri "/.well-known/host-meta"})]
        (is (= 200 (:status res)))))
    (testing "GET /.well-known/webfinger"
      (let [res (handler {:request-method :get
                          :uri "/.well-known/webfinger"
                          :params {:resource "acct:foo@example.com"}})]
        (is (= 200 (:status res)))))))
