#! /bin/bash
cd -- "$(dirname "$BASH_SOURCE")"
mkdir KE1_Konvertiert
javac --source-path src src/propra/imageconverter/ImageConverter.java -d bin
cd bin
java -Xmx256m propra.imageconverter.ImageConverter --input=../testbilder/test_01_uncompressed.tga --output=../KE1_Konvertiert/test_01.propra
java -Xmx256m propra.imageconverter.ImageConverter --input=../testbilder/test_02_uncompressed.tga --output=../KE1_Konvertiert/test_02.propra
java -Xmx256m propra.imageconverter.ImageConverter --input=../testbilder/test_03_uncompressed.propra --output=../KE1_Konvertiert/test_03.tga
java -Xmx256m propra.imageconverter.ImageConverter --input=../testbilder/test_04_uncompressed.propra --output=../KE1_Konvertiert/test_04.tga

echo "Ungültiges suffix"
java -Xmx256m propra.imageconverter.ImageConverter --input=../testbilder/test_03_uncompressed_100_0.fff --output=../KE1_Konvertiert/test_03_uncompressed_0_0.propra
echo "Keine PArameter"
java -Xmx256m propra.imageconverter.ImageConverter 


echo "Optionale Anforderungen TGA"
echo "0_0"
java -Xmx256m propra.imageconverter.ImageConverter --input=../testbilder/test_01_uncompressed_0_0.tga --output=../KE1_Konvertiert/test_01_uncompressed_0_0.tga
echo "0_100"
java -Xmx256m propra.imageconverter.ImageConverter --input=../testbilder/test_01_uncompressed_0_100.tga --output=../KE1_Konvertiert/test_01_uncompressed_0_100.tga
echo "100_0"
java -Xmx256m propra.imageconverter.ImageConverter --input=../testbilder/test_01_uncompressed_100_0.tga --output=../KE1_Konvertiert/test_01_uncompressed_100_0.tga
echo "bpp"
java -Xmx256m propra.imageconverter.ImageConverter --input=../testbilder/test_01_uncompressed_bpp.tga --output=../KE1_Konvertiert/test_01_uncompressed_bpp.tga
echo "encoding"
java -Xmx256m propra.imageconverter.ImageConverter --input=../testbilder/test_01_uncompressed_compr.tga --output=../KE1_Konvertiert/test_01_uncompressed_compr.tga
echo "size too small"
java -Xmx256m propra.imageconverter.ImageConverter --input=../testbilder/test_01_uncompressed_toosmall_h.tga --output=../KE1_Konvertiert/test_01_uncompressed_toosmall_h.tga
echo "wrong origin"
java -Xmx256m propra.imageconverter.ImageConverter --input=../testbilder/test_01_uncompressed-wrong-origin.tga --output=../KE1_Konvertiert/test_01_uncompressed_toosmall_h.tga


echo "Optionale Anforderungen ProPra"
echo "0_0"
java -Xmx256m propra.imageconverter.ImageConverter --input=../testbilder/test_03_uncompressed_0_0.propra --output=../KE1_Konvertiert/test_03_uncompressed_0_0.propra
echo "0_100"
java -Xmx256m propra.imageconverter.ImageConverter --input=../testbilder/test_03_uncompressed_0_100.propra --output=../KE1_Konvertiert/test_03_uncompressed_0_0.propra
echo "100_0"
java -Xmx256m propra.imageconverter.ImageConverter --input=../testbilder/test_03_uncompressed_100_0.propra --output=../KE1_Konvertiert/test_03_uncompressed_0_0.proprajava -Xmx256m propra.imageconverter.ImageConverter --input=../testbilder/test_03_uncompressed_0_0.propra --output=../KE1_Konvertiert/test_03_uncompressed_0_0.propra
echo "bpp"
java -Xmx256m propra.imageconverter.ImageConverter --input=../testbilder/test_03_uncompressed_100_0.propra --output=../KE1_Konvertiert/test_03_uncompressed_0_0.proprajava -Xmx256m propra.imageconverter.ImageConverter --input=../testbilder/test_03_uncompressed_bpp.propra --output=../KE1_Konvertiert/test_03_uncompressed_bpp.propra
echo "Checksum"
java -Xmx256m propra.imageconverter.ImageConverter --input=../testbilder/test_03_uncompressed_checksum.propra --output=../KE1_Konvertiert/test_03_uncompressed_0_0.propra
echo "Encoding"
java -Xmx256m propra.imageconverter.ImageConverter --input=../testbilder/test_03_uncompressed_compr.propra --output=../KE1_Konvertiert/test_03_uncompressed_0_0.propra
echo "Less bytes"
java -Xmx256m propra.imageconverter.ImageConverter --input=../testbilder/test_03_uncompressed_lessbytes.propra --output=../KE1_Konvertiert/test_03_uncompressed_0_0.propra
echo "Magic"
java -Xmx256m propra.imageconverter.ImageConverter --input=../testbilder/test_03_uncompressed_magic.propra --output=../KE1_Konvertiert/test_03_uncompressed_0_0.propra
echo "more Bytes"
java -Xmx256m propra.imageconverter.ImageConverter --input=../testbilder/test_03_uncompressed_morebytes.propra --output=../KE1_Konvertiert/test_03_uncompressed_0_0.propra
echo "H too large"
java -Xmx256m propra.imageconverter.ImageConverter --input=../testbilder/test_03_uncompressed_toobig_h.propra --output=../KE1_Konvertiert/test_03_uncompressed_0_0.propra
echo "W too large"
java -Xmx256m propra.imageconverter.ImageConverter --input=../testbilder/test_03_uncompressed_toobig_w.propra --output=../KE1_Konvertiert/test_03_uncompressed_0_0.propra
echo "h too small"
java -Xmx256m propra.imageconverter.ImageConverter --input=../testbilder/test_03_uncompressed_toosmall_h.propra --output=../KE1_Konvertiert/test_03_uncompressed_0_0.proprajava -Xmx256m propra.imageconverter.ImageConverter --input=../testbilder/test_03_uncompressed_0_0.propra --output=../KE1_Konvertiert/test_03_uncompressed_0_0.propra
echo "W too small"
java -Xmx256m propra.imageconverter.ImageConverter --input=../testbilder/test_03_uncompressed_toosmall_w.propra --output=../KE1_Konvertiert/test_03_uncompressed_0_0.propra