(ns cloxzing.core
  (:require [cloxzing.decode :as decode]
            [cloxzing.encode :as encode]))

(defn encode-decode
  ([text]
   (encode-decode text {}))
  ([text opts]
   (println
     (decode/from-image
       (encode/to-image text opts)))))
