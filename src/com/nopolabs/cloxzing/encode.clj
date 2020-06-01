(ns com.nopolabs.cloxzing.encode
  (:require [com.nopolabs.cloxzing.decode :as decode]
            [clojure.string :as str])
  (:import (com.google.zxing BarcodeFormat)
           (com.google.zxing EncodeHintType)
           (com.google.zxing.client.j2se MatrixToImageWriter)
           (com.google.zxing.qrcode.decoder ErrorCorrectionLevel)
           (com.google.zxing.qrcode QRCodeWriter)
           (java.nio.charset StandardCharsets)
           (javax.imageio ImageIO)
           (java.io FileOutputStream File InputStream OutputStream)
           (java.awt.image BufferedImage)
           (java.awt AlphaComposite Graphics2D)
           (net.coobird.thumbnailator Thumbnails)
           (java.net URL)
           (javax.imageio.stream ImageInputStream)))

(def QR_CODE BarcodeFormat/QR_CODE)

; error correction L M Q H
(def error-correction-H ErrorCorrectionLevel/H)
(def error-correction-Q ErrorCorrectionLevel/Q)
(def error-correction-M ErrorCorrectionLevel/M)
(def error-correction-L ErrorCorrectionLevel/L)

; character set UTF-8 ISO-8859-1
(def utf-8 StandardCharsets/UTF_8)
(def iso-8859-1 StandardCharsets/ISO_8859_1)

(def source-over (AlphaComposite/getInstance AlphaComposite/SRC_OVER (float 1)))

(def default-format "PNG")

(defn- bit-matrix-to-image
  [bit-matrix]
  (MatrixToImageWriter/toBufferedImage bit-matrix))

(defn- text-to-qr-bit-matrix
  [text size hints]
  (.encode
    (new QRCodeWriter)
    text QR_CODE size size hints))

(defn- text-to-qr-image
  [text size hints]
  (bit-matrix-to-image
    (text-to-qr-bit-matrix
      text size hints)))

(defn- new-image
  [base-image]
  (new BufferedImage
       (.getHeight base-image)
       (.getHeight base-image)
       (.getType base-image)))

(defn- drawImage
  [graphics image x y]
  (.drawImage ^Graphics2D graphics ^BufferedImage image ^int x ^int y nil)
  graphics)

(defn- drawOver
  [graphics image x y]
  (.setComposite ^Graphics2D graphics ^AlphaComposite source-over)
  (drawImage graphics image x y))

(defn- overlay
  [^BufferedImage base-image ^BufferedImage overlay-image]
  (let [base-height (.getHeight base-image)
        base-width (.getWidth base-image)
        overlay-height (.getHeight overlay-image)
        overlay-width (.getWidth overlay-image)
        draw-height (int (Math/round (float (/ (- base-height overlay-height) 2))))
        draw-width (int (Math/round (float (/ (- base-width overlay-width) 2))))
        image (new-image base-image)]
    (-> (.getGraphics image)
        (drawImage base-image 0 0)
        (drawOver overlay-image draw-width draw-height))
    image))

(defn- read-image
  [source]
  (if (instance? String source)
    (ImageIO/read (new File source))
    (if (instance? File source)
      (ImageIO/read ^File source)
      (if (instance? URL source)
        (ImageIO/read ^URL source)
        (if (instance? ImageInputStream source)
          (ImageIO/read ^ImageInputStream source)
          (if (instance? InputStream source)
            (ImageIO/read ^InputStream source)))))))

(defn- resize-image
  [image size]
  (let [thumbnails (Thumbnails/of (into-array [image]))
        resized (.size thumbnails size size)]
    (.asBufferedImage resized)))

(defn- qrcode-image
  ([text size hints]
   (text-to-qr-image text size hints))
  ([text size hints logo logo-size]
   (if (<= logo-size 0)
     ; no room for the logo
     (qrcode-image text size hints)
     (let [base-image (qrcode-image text size hints)
           overlay-image (resize-image (read-image logo) logo-size)
           image (overlay base-image overlay-image)
           decoded (decode/from-image image)]
       (if (empty? decoded)
         ; if we can't recover text from qrcode then retry with a smaller logo
         (qrcode-image text size hints logo (- logo-size 10))
         ; we found a logo size that works
         image)))))

(defn- max-logo-size
  [size logo-size]
  (let [max (int (/ size 3))
        logo-size (or logo-size 75)]
    (if (< logo-size max)
      logo-size
      max)))

(defn- qrcode
  [text {:keys [size logo logo-size error-correction character-set margin]}]
  (let [size (or size 300)
        logo-size (max-logo-size size logo-size)
        hints {EncodeHintType/ERROR_CORRECTION (or error-correction error-correction-H)
               EncodeHintType/CHARACTER_SET (or character-set iso-8859-1)
               EncodeHintType/MARGIN (or margin 1)}]
    (if (nil? logo) (qrcode-image text size hints)
                    (qrcode-image text size hints logo logo-size))))

(defn- valid-format
  [format]
  (if (contains? (set (ImageIO/getWriterFormatNames)) format)
    format
    (throw (new IllegalArgumentException
                (str format " is not a valid format.")))))

(defn- file-format
  [file]
  (let [name (if (instance? File file) (.getName file) file)]
    (last (str/split name #"\."))))

(defn- write-image-to-file
  [image file { format :format }]
  (let [file (if (instance? File file) file (new File file))
        format (or format (file-format file))
        format (valid-format format)
        format (or format default-format)
        stream (new FileOutputStream file)]
    (ImageIO/write ^BufferedImage image ^String format stream)))

(defn- write-image-to-stream
  [image stream { format :format }]
  (let [format (or format default-format)]
    (ImageIO/write ^BufferedImage image ^String format ^OutputStream stream)
    stream))

(defn to-image
  "Returns QR code as a java.awt.image.BufferedImage suitable for further processing"
  ([text] (to-image text {}))
  ([text opts] (qrcode text opts)))

(defn to-stream
  "Writes QR code to a java.io.OutputStream"
  ([text stream] (to-stream text stream {}))
  ([text stream opts] (write-image-to-stream (to-image text opts) stream opts)))

(defn to-file
  "Writes QR code to a file (file parameter may either be String or java.io.File)"
  ([text file] (to-file text file {}))
  ([text file opts] (write-image-to-file (to-image text opts) file opts)))
