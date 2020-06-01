(ns com.nopolabs.cloxzing.core-test
  (:require [clojure.test :refer :all]
            [clojure.java.io :as io]
            [com.nopolabs.cloxzing.core :as core]
            [com.nopolabs.cloxzing.encode :as encode]
            [com.nopolabs.cloxzing.decode :as decode])
  (:import (clojure.lang LazySeq)
           (java.io File)))

(defn temp-file
  ([] (temp-file ".text"))
  ([suffix]
   (let [tmp (File/createTempFile "cloxzing" suffix)]
     (.deleteOnExit tmp)
     tmp)))

(defn with-temp
  ([test] (with-temp ".text" test))
  ([suffix test]
   (let [tmp (temp-file suffix)]
     (test tmp)
     (.delete tmp))))

(deftest encode-decode
  (testing "Pure vanilla"
    (let [result (decode/from-image (encode/to-image "The quick brown fox ..."))]
      (is (= LazySeq (type result)))
      (is (= "The quick brown fox ..." (apply str result))))))

(deftest decode
  (testing "Hi Mom!"
    (let [result (decode/from-url (io/resource "hi-mom.png"))]
      (is (= LazySeq (type result)))
      (is (= "Hi Mom!" (apply str result))))))

(deftest encode
  (testing "Hi Mom!"
    (with-temp
      (fn [tmp]
        (encode/to-file "Hi Mom!" tmp)
        (is (= "Hi Mom!" (apply str (decode/from-file tmp)))))))
  (testing "Hi Mom! (with logo)"
    (with-temp ".png"
      (fn [tmp]
        (encode/to-file "Hi Mom!" {:size 300 :logo "dev-resources/logo.png" :logo-size 200} tmp "png")
        (is (= "Hi Mom!" (apply str (decode/from-file tmp))))))))
