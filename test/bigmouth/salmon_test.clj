(ns bigmouth.salmon-test
  (:require [bigmouth.salmon :as salmon]
            [bigmouth.models.keystore :as keystore]
            [clojure.test :refer [deftest testing is]]))

(deftest magic-key-test
  (testing "magic-key->public-key is inverse of public-key->magic-key"
    (let [keypair (keystore/fresh-keypair)
          public-key (.getPublic keypair)
          public-key' (-> public-key
                          salmon/public-key->magic-key
                          salmon/magic-key->public-key)]
      (is (= public-key public-key')))))
