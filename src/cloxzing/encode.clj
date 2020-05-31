(ns cloxzing.encode
  (:import (com.google.zxing BarcodeFormat)
           (com.google.zxing EncodeHintType)
           (com.google.zxing.client.j2se MatrixToImageWriter)
           (com.google.zxing.qrcode.decoder ErrorCorrectionLevel)
           (com.google.zxing.qrcode QRCodeWriter)
           (java.nio.charset StandardCharsets)
           (javax.imageio ImageIO)
           (java.io ByteArrayOutputStream FileOutputStream File InputStream)
           (java.awt.image BufferedImage)
           (java.awt AlphaComposite)
           (net.coobird.thumbnailator Thumbnails)
           (java.net URL)
           (javax.imageio.stream ImageInputStream)))

(def QR_CODE BarcodeFormat/QR_CODE)

; error correction L M Q H
(def error-correction-H {EncodeHintType/ERROR_CORRECTION ErrorCorrectionLevel/H})
(def error-correction-Q {EncodeHintType/ERROR_CORRECTION ErrorCorrectionLevel/Q})
(def error-correction-M {EncodeHintType/ERROR_CORRECTION ErrorCorrectionLevel/M})
(def error-correction-L {EncodeHintType/ERROR_CORRECTION ErrorCorrectionLevel/L})

; character set UTF-8 ISO-8859-1
(def utf-8 {EncodeHintType/CHARACTER_SET StandardCharsets/UTF_8})
(def iso-8859-1 {EncodeHintType/CHARACTER_SET StandardCharsets/ISO_8859_1})

(def default-hints {EncodeHintType/ERROR_CORRECTION ErrorCorrectionLevel/H
                    EncodeHintType/CHARACTER_SET    StandardCharsets/ISO_8859_1
                    EncodeHintType/MARGIN           1})

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
  [graphics ^BufferedImage image x y]
  (.drawImage graphics image x y nil)
  graphics)

(defn- drawOver
  [graphics ^BufferedImage image x y]
  (.setComposite graphics source-over)
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

(defn- write-image-to-stream
  [image format]
  (let [format (or format default-format)
        stream (new ByteArrayOutputStream)]
    (ImageIO/write ^BufferedImage image ^String format stream)
    stream))

(defn- write-image-to-file
  [image file format]
  (let [format (or format default-format)
        stream (new FileOutputStream (new File file))]
    (ImageIO/write ^BufferedImage image ^String format stream)))

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
           decoded (cloxzing.decode/from-image image)]
       (if (nil? decoded)
         image
         ; if we can't recover text from qrcode then retry with a smaller logo
         (qrcode-image text size hints logo (- logo-size 10)))))))

(defn qrcode
  [text {:keys [size hints logo logo-size]}]
  (let [size (or size 300)
        logo-size (or logo-size 75)
        hints (merge default-hints hints)]
    (if (nil? logo) (qrcode-image text size hints)
                    (qrcode-image text size hints logo logo-size))))

(defn to-image
  ([text] (to-image text {}))
  ([text opts] (qrcode text opts)))

(defn to-stream
  ([text] (to-stream text {} default-format))
  ([text format] (to-stream text {} format))
  ([text opts format] (write-image-to-stream (qrcode text opts) format)))

(defn to-file
  ([text file] (to-file text {} file default-format))
  ([text file format] (to-file text {} file format))
  ([text opts file format] (write-image-to-file (qrcode text opts) file format)))
