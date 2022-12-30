package propra.imageconverter.image.compression;

import java.io.IOException;
import java.nio.ByteBuffer;
import propra.imageconverter.util.CheckedInputStream;
import propra.imageconverter.util.CheckedOutputStream;
import propra.imageconverter.data.IDataTarget;
import propra.imageconverter.data.IDataTranscoder;
import propra.imageconverter.image.Color;
import propra.imageconverter.image.ColorUtil;
import propra.imageconverter.image.ImageAttributes;

/**
 * Transcoder Implementierung für RLE Kompression
 */
public class ImageTranscoderRLE extends ImageTranscoderRaw {

    // Gepufferte Daten
    private ByteBuffer bufferedData; 
    private boolean isBufferedData;

    public ImageTranscoderRLE(ImageAttributes attributes) {
        super(attributes);
    }

    @Override
    public Compression getCompression() {
        return Compression.RLE;
    }
    
    /**
     * Initialisiert Kodierung der Blöcke
     */
    @Override
    public IDataTranscoder beginEncoding(EncodeMode op, CheckedOutputStream out) throws IOException {
        super.beginEncoding(op, out);
        bufferedData = ByteBuffer.allocate(DEFAULT_BLOCK_SIZE * 2);
        return this;
    }

    
    /**
     * Dekodiert RLE Daten des Streams
     */
    @Override
    public void decode(CheckedInputStream in, IDataTarget target) throws IOException {     
    
        // Puffer vorbereiten
        ByteBuffer buff = ByteBuffer.allocate(DEFAULT_BLOCK_SIZE);
        
        /*
         * Rle Block dekodieren
         */
        int packetHeader;
        while((packetHeader = in.read()) != -1) {
            
            // Paketkopf und Wiederholungen dekodieren
            packetHeader = packetHeader & 0xFF;
            int pixelCount = (packetHeader & 127) + 1;
            int packetLen = pixelCount * Color.PIXEL_SIZE;

            /*
             * Wenn im Ausgabepuffer nicht mehr genug Platz ist, Datenblock zum 
             * Empfänger senden
             */
            if( buff.position() + packetLen > buff.capacity()) {
                pushData(buff,false, target);
            }

            // RLE oder RAW Paket?
            Color rle = new Color();
            if(packetHeader > 127) {
                // Farbwert auslesen und Ausgabepuffer auffüllen
                in.read(rle.get());
                ColorUtil.fill(buff, rle, pixelCount);
            } else {
                // RAW Farben übertragen
                in.read(buff.array(),buff.position(),packetLen);
                buff.position(buff.position() + packetLen);
            }
        }
        
        /*
         *  Letzten Block der Operation kennzeichnen und Restdaten
         *  übertragen
         */
        pushData(buff,true, target);
    }
    
    /**
     * Kodiert Block
     */
    @Override
    public void encode(ByteBuffer block, boolean last) throws IOException{ 
        
        // Puffer erstellen
        ByteBuffer rleBlock = ByteBuffer.allocate(192 * Color.PIXEL_SIZE);
        
        /*
         *  Eingabedaten mit gepufferten Daten zusammenführen
         */
        ByteBuffer inBuffer = getDataToEncode(block);
        int dataLimit = inBuffer.limit();
        
        // Über Bytes iterieren und gemäß RLE verarbeiten
        while(inBuffer.position() < dataLimit) {
            /*
             *  Wenn Blockgrenze erreichbar Restdaten puffern für nächsten Block, 
             *  nur wenn es sich nicht um den letzten Block handelt.
             */
            boolean boundary = inBuffer.position() + (127 * Color.PIXEL_SIZE) >= dataLimit;
            if(boundary && !last) {    
                bufferInputData(inBuffer);
                isBufferedData = true;
                return; 
            }
            
            /*
             *  RLE Kodierung
             */
            int colorCtr = countRleColor(inBuffer);
            if(colorCtr > 1 ) {     
                /**
                 * Rle Pixel kodieren
                 */
                Color color = new Color();
                inBuffer.get(color.get());
                rleBlock.put((byte)(128 + colorCtr - 1));
                rleBlock.put(color.get());
                rleBlock.flip();

                // Gleiche Farben im Eingabepuffer überspringen
                inBuffer.position(inBuffer.position() + (colorCtr - 1) * Color.PIXEL_SIZE);
                
            } else {      
               /*
                * Raw Pixel kopieren und Paketkopf schreiben
                */
                encodeRawData(inBuffer, rleBlock);
            }
            
            // In Resource schreiben
            outStream.write(rleBlock);
            encodedBytes += rleBlock.limit();
            rleBlock.clear();
        }
    }

    /**
     * Schließt Kodierung ab
     */
    @Override
    public long endEncoding() throws IOException {
        switch(operation) {
            case ENCODE -> {
                /*
                 *  Stream flushen und kodierte Datengröße aktualisieren
                 */
                outStream.flush();
            }
        }

        return super.endEncoding();
    }
    
    /**
     *  Zählt identische Pixel bis zu 128
     */
    private int countRleColor(ByteBuffer data) {
        int baseOffset = data.position();
        int runOffset = baseOffset + Color.PIXEL_SIZE;
        byte array[] = data.array();
        int counter = 1;
        int len = data.limit();

        while(ColorUtil.compareColor(array, baseOffset, runOffset)) {
            runOffset += Color.PIXEL_SIZE;  
            counter++;
            
            // Zähler max, oder Ende erreicht?
            if( counter > 127
            ||  runOffset >= len) {
                break;
            }
        }
        
        return counter;
    }
    
    /**
     *  Kopiert bis zu 128 Pixel eines RLE Rawblocks, arbeitet aus 
     *  Performancegründen direkt mit den Arrays der ByteBuffer
     */
    private int encodeRawData(ByteBuffer inBuffer, ByteBuffer outBuffer) {
        
        int inOffset = inBuffer.position();
        byte[] outArray = outBuffer.array();
        byte[] inArray = inBuffer.array();
        int outOffset = 1;
        int rawCounter = 0;
        int limit = inBuffer.limit();
        
        // Vergleicht aktuelle Farbe mit der folgenden Farbe
        while( !ColorUtil.compareColor(inArray, inOffset, inOffset + 3)) {

            outArray[outOffset++] = inArray[inOffset++];
            outArray[outOffset++] = inArray[inOffset++];
            outArray[outOffset++] = inArray[inOffset++];

            rawCounter++;
            
            if(rawCounter >= 128
            || inOffset >= limit) {
                break;
            }
        }
        
        // RLE Kopf schreiben
        outArray[0] = (byte)(rawCounter - 1);
        
        // Puffer aktualisieren
        inBuffer.position(inOffset);
        outBuffer.limit(outOffset);
        
        return rawCounter;
    }
    
    /**
     *  Puffert zu kodierende Daten bis zum nächsten Block
     */
    private void bufferInputData(ByteBuffer inBuffer) {
        if(inBuffer == bufferedData) {
            bufferedData.compact();
        } else {
            bufferedData.clear();
            bufferedData.put(0, inBuffer, 
                                    inBuffer.position(), 
                                    inBuffer.remaining());
            bufferedData.position(inBuffer.remaining());
        }
    }
    
    /**
     *  Wenn gepufferte Daten aus vorherigem Block vorhanden sind,
     *  den neuen Block an diesen Anhängen und passenden ByteBuffer 
     *  zurückgeben
     */
    private ByteBuffer getDataToEncode(ByteBuffer data) {
        if(isBufferedData) {
            bufferedData.put(data);
            bufferedData.flip();
            isBufferedData = false;
            return bufferedData;
        } 
        return data;
    }
}
