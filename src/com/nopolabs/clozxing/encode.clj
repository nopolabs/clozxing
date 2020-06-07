(ns com.nopolabs.clozxing.encode
  (:require [com.nopolabs.clozxing.decode :as decode]
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
(def safe-error-correction-set #{error-correction-H
                                 error-correction-Q
                                 error-correction-M
                                 error-correction-L})

; character set UTF-8 ISO-8859-1
(def utf-8 StandardCharsets/UTF_8)
(def iso-8859-1 StandardCharsets/ISO_8859_1)
(def safe-character-set-set #{utf-8
                              iso-8859-1})

(def source-over (AlphaComposite/getInstance AlphaComposite/SRC_OVER (float 1)))

(def default-opts
  {:size 300
   :error-correction error-correction-H
   :character-set iso-8859-1
   :margin 1
   :logo nil
   :format "PNG"})

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

(defn- overlay-qrcode-image
  [base-image logo-image logo-size]
  (if (<= logo-size 0)
    ; no room for the logo
    base-image
    ; try overlaying logo with logo-size
    (let [overlay-image (resize-image logo-image logo-size)
          candidate-image (overlay base-image overlay-image)
          decoded (decode/from-image candidate-image)]
      (if (empty? decoded)
        ; if we can't recover text from candidate-image then recurse with a smaller logo-size
        (overlay-qrcode-image base-image logo-image (- logo-size 10))
        ; we found a logo size that works
        candidate-image))))

(defn- qrcode-image
  ([text size hints]
   (text-to-qr-image text size hints))
  ([text size hints logo logo-size]
   (let [base-image (text-to-qr-image text size hints)
         logo-image (read-image logo)]
     (overlay-qrcode-image base-image logo-image logo-size))))

(defn- bounded-size
  [size min max]
  (if (< max size) max
    (if (< min size) size
      min)))

(defn- safe-logo-size
  [size logo-size]
  (let [max (int (/ size 3))
        logo-size (or logo-size max)]
    (bounded-size logo-size 0 max)))

(defn- safe-size
  [size]
  (if (nil? size)
    (:size default-opts)
    (bounded-size size 100 1000)))

(defn- safe-error-correction
  [error-correction]
  (if (contains? safe-error-correction-set error-correction)
    error-correction
    (:error-correction default-opts)))

(defn- safe-character-set
  [character-set]
  (if (contains? safe-character-set-set character-set)
    character-set
    (:character-set default-opts)))

(defn- safe-margin
  [margin]
  (if (nil? margin)
    (:margin default-opts)
    (if (> 0 margin) 0
      (if (< 20 margin) 20
        margin))))

(defn- safe-opts
  [{:keys [size logo logo-size error-correction character-set margin]}]
  (let [size (safe-size size)
        opts {:size size
              :logo logo
              :logo-size (safe-logo-size size logo-size)
              :error-correction (safe-error-correction error-correction)
              :character-set (safe-character-set character-set)
              :margin (safe-margin margin)}]
    (into {} (filter (comp some? val) opts))))

(defn- qrcode
  [text opts]
  (let [{:keys [size logo logo-size error-correction character-set margin]} (safe-opts opts)]
    (let [hints {EncodeHintType/ERROR_CORRECTION error-correction
                 EncodeHintType/CHARACTER_SET character-set
                 EncodeHintType/MARGIN margin}]
      (if (nil? logo) (qrcode-image text size hints)
                      (qrcode-image text size hints logo logo-size)))))

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
        format (or format (:format default-opts))
        stream (new FileOutputStream file)]
    (ImageIO/write ^BufferedImage image ^String format stream)
    (.getAbsolutePath file)))

(defn- write-image-to-stream
  [image stream { format :format }]
  (let [format (or format (:format default-opts))]
    (ImageIO/write ^BufferedImage image ^String format ^OutputStream stream)
    stream))

(defn to-image
  "Returns QR code as a java.awt.image.BufferedImage suitable for further processing"
  ([text] ^BufferedImage (to-image text {}))
  ([text opts] ^BufferedImage (qrcode text opts)))

(defn to-stream
  "Writes QR code to a java.io.OutputStream"
  ([text stream] ^OutputStream (to-stream text stream {}))
  ([text stream opts] ^OutputStream (write-image-to-stream (to-image text opts) stream opts)))

(defn to-file
  "Writes QR code to a file (file parameter may either be String or java.io.File)"
  ([text file] ^String (to-file text file {}))
  ([text file opts] ^String (write-image-to-file (to-image text opts) file opts)))
