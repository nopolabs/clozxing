(ns com.nopolabs.cloxzing.core
  (:require [com.nopolabs.cloxzing.decode :as decode]
            [com.nopolabs.cloxzing.encode :as encode]))

(defn encode-example [] (encode/to-file "example" "example.png" {:size 300
                                                                 :logo "dev-resources/logo.png"
                                                                 :logo-size 100
                                                                 :error-correction encode/error-correction-H
                                                                 :character-set encode/iso-8859-1
                                                                 :margin 1
                                                                 :format "PNG"}))

(defn decode-example [] (decode/from-file "example.png"))
