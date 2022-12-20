package propra.imageconverter.image;

import java.io.IOException;
import java.nio.ByteBuffer;
import propra.imageconverter.data.IDataListener;
import propra.imageconverter.data.IDataListener.Event;
import propra.imageconverter.util.CheckedInputStream;
import propra.imageconverter.util.CheckedOutputStream;

/**
 *
 * 
 */
public class ImageCodecRLE extends ImageCodec {

    private final ByteBuffer bufferedData; 
    private boolean isBufferedData;

    /*
     * 
     */
    public ImageCodecRLE(ImageResource resource) {
        super(resource);
        
        bufferedData = ByteBuffer.allocate(DEFAULT_BLOCK_SIZE * 2);
    }
    
        /*
     * 
     */
    @Override
    public void decode(IDataListener target) throws IOException {     
        if(!isValid()
        ||  target == null) {
            throw new IllegalArgumentException();
        }
        
        // Temporäre Variablen zur Performanceoptimierung
        CheckedInputStream stream = resource.getInputStream();
    
        // Puffer vorbereiten
        ByteBuffer outBuffer = ByteBuffer.allocate(DEFAULT_BLOCK_SIZE);
        
        /*
         * Rle Block dekodieren
         */
        int packetHeader;
        while((packetHeader = stream.read()) != -1) {
            
            // Paketkopf und Wiederholungen dekodieren
            packetHeader = packetHeader & 0xFF;
            int pixelCount = (packetHeader & 127) + 1;
            int packetLen = pixelCount * 3;
            byte[] rleColor = new byte[3];

            /*
             * Wenn im Ausgabepuffer nicht mehr genug Platz ist, Datenblock zum 
             * Empfänger senden
             */
            if( outBuffer.position() + packetLen > outBuffer.capacity()) {
                outBuffer.flip();
                dispatchEvent(  Event.DATA_BLOCK_DECODED, 
                                target, 
                                outBuffer,
                                false);
                outBuffer.clear();
            }

            // RLE oder RAW Paket?
            if(packetHeader > 127) {
                // Farbwert auslesen und Ausgabepuffer auffüllen
                stream.read(rleColor);
                
                ColorFormat.fillColor(rleColor,
                                        outBuffer.array(),
                                        outBuffer.position(),
                                        pixelCount);
            } else {
                // RAW Farben übertragen
                stream.read(outBuffer.array(), 
                            outBuffer.position(), 
                            packetLen);
            }
            
            outBuffer.position(outBuffer.position() + packetLen);
        }
        
        /**
         *  Letzten Block der Operation kennzeichnen und Restdaten
         *  übertragen
         */
        outBuffer.flip();
        dispatchEvent(  Event.DATA_BLOCK_DECODED, 
                        target, 
                        outBuffer,
                        stream.eof());
    }
    
    /*
     *  Kodiert Pixelblock als RLE
     */
    @Override
    public void encode( ByteBuffer block,
                        boolean last,
                        IDataListener listener) throws IOException{ 
        if(!isValid()
        ||  block == null) {
            throw new IllegalArgumentException();
        }
        
        CheckedOutputStream stream = resource.getOutputStream();
        
        ByteBuffer rleBlock = ByteBuffer.allocate(192*3);
        byte[] color = new byte[3];

        // Farbkonvertierung
        if(image.getHeader().colorFormat().compareTo(ColorFormat.FORMAT_RGB) != 0) {   
            ColorFormat.convertColorBuffer( block, 
                                            ColorFormat.FORMAT_RGB, 
                                            block,
                                            image.getHeader().colorFormat());
        }
        
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
            boolean boundary = inBuffer.position() + (127 * 3) >= dataLimit;
            if(boundary && !last) {    
                bufferInputData(inBuffer);
                isBufferedData = true;
                return; 
            }
            
            /*
             *  Kodierung
             */
            int colorCtr = countRleColor(inBuffer);
            if(colorCtr > 1 ) {     
                /**
                 * Rle Pixel kodieren
                 */
                inBuffer.get(color);
                rleBlock.put((byte)(128 + colorCtr - 1));
                rleBlock.put(color);
                rleBlock.flip();

                // Gleiche Farben im Eingabepuffer überspringen
                inBuffer.position(inBuffer.position() + (colorCtr - 1) * 3);
                
            } else {      
               /*
                * Raw Pixel kopieren und Paketkopf schreiben
                */
                encodeRawData(inBuffer, rleBlock);
            }
            
            // In Resource schreiben
            stream.write(rleBlock);
            rleBlock.clear();
        }
        
        stream.flush();
    }
    
    /**
     *  
     */
    @Override
    public boolean isValid() {
        return true;
    }
    
    /**
     *  Zählt identische Pixel bis zu 128
     */
    private int countRleColor(ByteBuffer data) {
        int offs = data.position() + 3;
        int counter = 1;

        while ( ColorFormat.compareColor(data.array(), data.position(), offs)
            &&  counter < 128
            &&  offs < data.limit()) {
            counter++;
            offs += 3;         
        }

        return counter;
    }
    
    /**
     *  Kopiert bis zu 128 Pixel eines RLE Rawblocks, arbeitet aus 
     *  Performancegründen direkt mit den Arrays der ByteBuffer
     */
    private int encodeRawData(  ByteBuffer inBuffer, 
                                ByteBuffer outBuffer) {
        
        int inOffset = inBuffer.position();
        byte[] t = outBuffer.array();
        byte[] s = inBuffer.array();
        int outOffset = 1;
        int rawCounter = 0;
        int limit = inBuffer.limit();
        
        // Vergleicht aktuelle Farbe mit der folgenden Farbe
        while( !ColorFormat.compareColor(s, inOffset, inOffset + 3)
            && rawCounter < 128
            && inOffset < limit) {

            t[outOffset++] = s[inOffset++];
            t[outOffset++] = s[inOffset++];
            t[outOffset++] = s[inOffset++];

            rawCounter++;
        }
        
        // RLE Kopf schreiben
        t[0] = (byte)(rawCounter - 1);
        
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
            bufferedData.put(0, 
                            inBuffer, 
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
