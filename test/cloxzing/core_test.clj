(ns cloxzing.core-test
  (:require [clojure.test :refer :all]
            [cloxzing.decode :refer :all]
            [cloxzing.encode :refer :all]
            [clojure.java.io :as io])
  (:import (clojure.lang LazySeq)
           (java.io File)))

(defn temp-file
  []
  (let [tmp (File/createTempFile "cloxzing" ".test")]
    (.deleteOnExit tmp)
    tmp))

(defn with-temp [t]
  (let [tmp (temp-file)]
    (t tmp)
    (.delete tmp)))

(deftest encode-decode
  (testing "Pure vanilla"
    (let [result (from-image (to-image "The quick brown fox ..."))]
      (is (= LazySeq (type result)))
      (is (= "The quick brown fox ..." (apply str result))))))

(deftest decode
  (testing "Hi Mom!"
    (let [result (from-url (io/resource "hi-mom.png"))]
      (is (= LazySeq (type result)))
      (is (= "Hi Mom!" (apply str result))))))

(deftest encode
  (testing "Hi Mom!"
    (with-temp
      (fn [tmp]
        (to-file "Hi Mom!" tmp)
        (is (= "Hi Mom!" (apply str (from-file tmp))))))))
