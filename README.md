# clozxing

A Clojure library designed to encode and decode QR codes
using the [zxing](https://github.com/zxing/zxing) library.

Available on [clojars](https://clojars.org/com.nopolabs/clozxing).

`[com.nopolabs/clozxing "0.1.0"]`

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
