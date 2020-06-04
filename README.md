# clozxing

A Clojure library designed to encode and decode QR codes
using the [zxing](https://github.com/zxing/zxing) library.

Available on [clojars](https://clojars.org/com.nopolabs/clozxing).

`[com.nopolabs/clozxing "0.1.1"]`

## Usage

### Encoding

#### encode/to-image
`(encode/to-image text)` `(encode/to-image text opts)`

    Encodes text to a QR code. Returns a java.awt.image.BufferedImage.

#### encode/to-stream
`(encode/to-stream text stream)` `(encode/to-stream stream opts)`

    Encodes text to a QR code and writes results to provided java.io.OutpuStream. Returns true if successful.

    Default output format is PNG, format can be specified in opts, e.g.: { :format "JPG" }.

#### encode/to-file
`(encode/to-file text file)` `(encode/to-file file opts)`

    Encodes text to a QR code and writes results to provided file. Returns true if successful.

    The file parameter may either be a String or a java.io.File.

    Output format is determined from file name suffix and can be overridden in opts, e.g.: { :format "JPG" }.

#### Opts
##### :size
The size of the desired QR code in pixels (a size x size square).
##### :error-correction
[ErrorCorrectionLevel](https://github.com/zxing/zxing/blob/master/core/src/main/java/com/google/zxing/qrcode/decoder/ErrorCorrectionLevel.java)
  - encode/error-correction-H 30%
  - encode/error-correction-Q 25%
  - encode/error-correction-M 15%
  - encode/error-correction-H 7%
##### :character-set
  - encode/utf-8
  - encode/iso-8859-1
##### :margin
Margin (in pixels) around generated QR code.
##### :format
Output image format (JPEG, PNG, GIF, BMP or WBMP)
##### :logo

##### :logo-size
This is a hint for the desired logo size. Logo overlays are possible
because of QR code error correction. Encoder will try overlaying 
progressively smaller logos in the center of the QR code image until
it finds a combination that can be successfully decoded. 
##### defaults
Defaults are defined in `encode/default-opts`

There are no defaults for `:logo` and `:logo-size`
```
(def default-opts
  {:size 300
   :error-correction error-correction-H
   :character-set iso-8859-1
   :margin 1
   :format "PNG"})
```

#### Example
```
(encode/to-file 
  "https://github.com/nopolabs/clozxing" 
  "clozxing.png" 
  { :size 200
    :logo "dev-resources/logo.png"
    :logo-size 67
    :error-correction encode/error-correction-H
    :character-set encode/iso-8859-1
    :margin 1
    :format "PNG" }))
```
![https://github.com/nopolabs/clozxing](clozxing.png "https://github.com/nopolabs/clozxing")

### Decoding

#### decode/from-stream
`(decode/from-stream stream)`

    Reads and decodes QR code from provided java.io.InputStream. Returns decoded text if successful.

#### decode/from-file
`(decode/from-file file)`

    Reads and decodes QR code from file. Returns decoded text if successful.

    The file parameter may either be a String or a java.io.File.

#### decode/from-bytes
`(decode/from-bytes bytes)`

    Reads and decodes QR code from provided byte array. Returns decoded text if successful.

#### decode/from-image
`(decode/from-image image)`

    Reads and decodes QR code from provided java.awt.image.BufferedImage. Returns decoded text if successful.

#### decode/from-url
`(decode/from-url url)`

    Reads and decodes QR code from provided java.net.URL. Returns decoded text if successful.

#### Example
```
(decode/file "clozxing.png")
```

## License

Copyright © 2020 nopolabs

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
