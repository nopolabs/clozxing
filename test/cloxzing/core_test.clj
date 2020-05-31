(ns cloxzing.core-test
  (:require [clojure.test :refer :all]
            [cloxzing.decode :refer :all]
            [cloxzing.encode :refer :all])
  (:import (clojure.lang LazySeq)))

(deftest encode-decode
  (testing "Pure vanilla"
    (let [result (from-image (to-image "The quick brown fox ..."))]
      (println result)
      (is (= LazySeq (type result)))
      (is (= "The quick brown fox ..." (apply str result))))))

