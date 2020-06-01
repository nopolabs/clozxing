(ns com.nopolabs.cloxzing.core
  (:require [com.nopolabs.cloxzing.decode :as decode]
            [com.nopolabs.cloxzing.encode :as encode]))

(defn encode-decode
  ([text]
   (encode-decode text {}))
  ([text opts]
   (println
     (decode/from-image
       (encode/to-image text opts)))))
