(defproject com.nopolabs/clozxing "0.1.3"
  :description "Encode and decode QR codes using zxing library"
  :url "http://github.com/nopolabs/clozxing"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :deploy-repositories [["releases" :clojars]
                        ["snapshots" :clojars]]
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/data.json "1.0.0"]
                 [com.google.zxing/core "3.4.0"]
                 [com.google.zxing/javase "3.4.0"]
                 [net.coobird/thumbnailator "0.4.11"]]
  :repl-options {:init-ns com.nopolabs.clozxing.core})
