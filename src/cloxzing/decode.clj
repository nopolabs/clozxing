(ns cloxzing.decode
  (:import (javax.imageio ImageIO)
           (java.io InputStream ByteArrayInputStream ByteArrayOutputStream File FileInputStream)
           (com.google.zxing MultiFormatReader DecodeHintType BarcodeFormat BinaryBitmap)
           (java.util EnumMap EnumSet)
           (com.google.zxing.multi GenericMultipleBarcodeReader)
           (com.google.zxing.client.j2se BufferedImageLuminanceSource)
           (com.google.zxing.common GlobalHistogramBinarizer HybridBinarizer)
           (java.awt.image BufferedImage)
           (java.net URL)))

(def hints
  (new EnumMap {DecodeHintType/TRY_HARDER       true
                DecodeHintType/POSSIBLE_FORMATS (EnumSet/of BarcodeFormat/QR_CODE)}))

(def hints-pure
  (let [hints-pure (new EnumMap hints)]
    (.put hints-pure DecodeHintType/PURE_BARCODE true)
    hints-pure))

(defn- multiple-barcodes [bitmap]
  (try
    (let [multi-reader (new GenericMultipleBarcodeReader (new MultiFormatReader))]
      (.decodeMultiple multi-reader bitmap hints))
    (catch Exception _
      nil)))

(defn- decode [bitmap hints]
  (try
    (let [reader (new MultiFormatReader)]
      (.decode reader bitmap hints))
    (catch Exception _
      nil)))

(defn- pure-barcode [bitmap]
  (decode bitmap hints-pure))

(defn- photo-barcode [bitmap]
  (decode bitmap hints))

(defn- hybrid-barcode [source]
  (let [bitmap (new BinaryBitmap (new HybridBinarizer source))]
    (decode bitmap hints)))

(defn from-stream
  [^InputStream input-stream]
  (let [image (ImageIO/read input-stream)
        source (new BufferedImageLuminanceSource image)
        bitmap (new BinaryBitmap (new GlobalHistogramBinarizer source))
        results (multiple-barcodes bitmap)
        results (if (nil? results) (pure-barcode bitmap) results)
        results (if (nil? results) (photo-barcode bitmap) results)
        results (if (nil? results) (hybrid-barcode source) results)]
    (map #(.getText %) results)))

(defn from-file
  [file]
  (if (instance? File file)
    (from-stream (new FileInputStream file))
    (from-file (new File file))))

(defn from-bytes
  [bytes]
  (from-stream (new ByteArrayInputStream bytes)))

(defn from-image
  [image]
  (let [output (new ByteArrayOutputStream)]
    (ImageIO/write ^BufferedImage image "PNG" output)
    (from-bytes (.toByteArray output))))

(defn from-url
  [^URL url]
  (from-stream (.openStream url)))
