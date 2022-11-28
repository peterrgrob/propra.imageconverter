package propra.imageconverter.image;

import java.io.IOException;
import java.nio.ByteBuffer;
import propra.imageconverter.checksum.Checksum;
import propra.imageconverter.data.DataBlock;
import propra.imageconverter.data.IDataListener;

/**
 *
 * 
 */
public class ImageCodecRLE extends ImageCodecRaw {

    private final ByteBuffer bufferedData; 
    private boolean isBufferedData;

    /*
     * 
     */
    public ImageCodecRLE(   ImageResource resource, 
                            Checksum checksum) {
        super(resource, checksum);
        
        bufferedData = ByteBuffer.allocate(DEFAULT_BLOCK_SIZE * 2);
    }
    
    /*
     * 
     */
    @Override
    public void decode( DataBlock outBlock, 
                        IDataListener target) throws IOException {     
        if(!isValid()
        ||  outBlock == null
        ||  target == null) {
            throw new IllegalArgumentException();
        }
        
        // Temporäre Variablen zur Performanceoptimierung
        int inOffset = (int)resource.position();
        int inLength = (int)resource.length();
        byte[] inRleColor = new byte[3];
        int inColorCounter = 0;
        int inPacketHeader = 0;
        int nextBytes = 0;
    
        // Puffer vorbereiten
        ByteBuffer inBuffer = bufferedData;
        if(outBlock.data == null) {
            outBlock.data = ByteBuffer.allocate(DEFAULT_BLOCK_SIZE);
        }
        ByteBuffer outBuffer = outBlock.data;
        byte[] outBytes = outBuffer.array();
        
        /**
         * Blockweise Daten einlesen und dekodieren bis Ausgabeblock 
         * gefüllt ist
         */
        while( inOffset < inLength ) {
            
            // Blockgröße anpassen am Ende der Datei
            if(inOffset + inBuffer.capacity() > inLength) {
                inBuffer.limit(inBuffer.capacity() - ((inOffset + inBuffer.capacity()) - inLength));
            }
            
            // Block lesen
            int inLen = resource.read(inBuffer);
            inOffset += inLen;
            
            /**
             * Rle Daten dekodieren
             */
            while(inBuffer.remaining() != 0) {

                // Paketkopf und Wiederholungen einlesen
                inPacketHeader =  inBuffer.get() & 0xFF;
                inColorCounter = (inPacketHeader & 127) + 1;
                nextBytes = inColorCounter * 3;

                // Wenn gefüllt, Datenblock zum Empfänger senden
                if( outBuffer.position() + nextBytes > outBuffer.capacity()) {
                    
                    outBuffer.flip();
                    dispatchEvent(  Event.DATA_BLOCK_DECODED, 
                                    target, 
                                    outBlock);
                    outBuffer.clear();
                }

                // RLE oder RAW Paket?
                if(inPacketHeader > 127) {
                    
                    // Neuen Leseblock beginnen?
                    if(inBuffer.remaining() < 3) {
                        
                        inOffset = inOffset - inBuffer.remaining() - 1;
                        inBuffer.position(inBuffer.position() - 1);
                        resource.position(inOffset);
                        
                        break;
                    }
                    
                    // Farbwert auslesen und auffüllen
                    inBuffer.get(inRleColor);
                    int o = ColorFormat.fillColor(inRleColor,
                                                    outBytes,
                                                    outBuffer.position(),
                                                    inColorCounter);
                    outBuffer.position(o);
                    
                } else {
                    
                    // Neuen Leseblock beginnen?
                    if(inBuffer.remaining() < nextBytes) {
                        
                        inOffset = inOffset - inBuffer.remaining() - 1;
                        inBuffer.position(inBuffer.position() - 1);
                        resource.position(inOffset);
                        
                        break;
                    } 

                    // RAW Farben übertragen
                    outBuffer.put(inBuffer.array(), 
                                inBuffer.position(), 
                                nextBytes);
                    inBuffer.position(inBuffer.position() + nextBytes);
                }
            }
            
            // Eingabedaten filtern
            dispatchEvent(Event.DATA_IO_READ, 
                            this, 
                            new DataBlock(inBuffer.flip(),false));
            inBuffer.clear();
        } 
        
        if(resource.position() == resource.length()) {
            outBlock.lastBlock = true;
        }
        
        // Restdaten übertragen
        outBlock.data.flip();
        dispatchEvent(  Event.DATA_BLOCK_DECODED, 
                        target, 
                        outBlock);
    }
    
    
    /*
     *  Kodiert Pixelblock als RLE
     */
    @Override
    public void encode( DataBlock block) throws IOException{ 
        if(!isValid()
        ||  block == null) {
            throw new IllegalArgumentException();
        }
        
        /**
         * Lokale Variablen
         */
        int colorCtr;
        boolean boundary;        
        byte[] writeRleColor = new byte[3];
        ByteBuffer rleBlock = ByteBuffer.allocate(192*3);
        ByteBuffer inBuffer;

        // Farbkonvertierung
        if(image.getHeader().colorFormat().compareTo(ColorFormat.FORMAT_RGB) != 0) {   
            ColorFormat.convertColorBuffer( block.data, 
                                            ColorFormat.FORMAT_RGB, 
                                            block.data,
                                            image.getHeader().colorFormat());
        }
        
        /*
         *  Wenn gepufferte Daten aus vorherigem Block vorhanden sind,
         *  den neuen Block an diese Anhängen
         */
        if(isBufferedData) {
            bufferedData.put(block.data);
            bufferedData.flip();
            isBufferedData = false;
            inBuffer = bufferedData;
        } else {
            inBuffer = block.data;
        }
        
        int dataLimit = inBuffer.limit();
        
        // Über Bytes iterieren und gemäß RLE verarbeiten
        while(inBuffer.position() < dataLimit) {
            
            /*
             *  Wenn Blockgrenze erreichbar, Restdaten puffern für nächsten Block
             *  wenn es sich nicht um den letzten Block handelt.
             */
            boundary = inBuffer.position() + (127 * 3) >= dataLimit;
            if(boundary && !block.lastBlock) {
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
                isBufferedData = true;
                return; 
            }
            
            /*
             *  Kodierung
             */
            colorCtr = countRleColor(inBuffer);
            if(colorCtr > 1 ) {
                
                /**
                 * Rle Pixel kodieren
                 */
                encodeRleData(inBuffer, rleBlock, colorCtr);
                
                // In Resource schreiben
                resource.writeBuffered(rleBlock.flip());
                rleBlock.clear();
                
            } else {
                
               /*
                * Raw Pixel kopieren und Paketkopf schreiben
                */
                encodeRawData(inBuffer, rleBlock);

                // In Resource schreiben
                resource.writeBuffered(rleBlock); 
                rleBlock.clear();
            }
        }
        
        resource.flush();
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
        
        byte[] array = data.array();
        int baseOffset = data.position();
        int nextOffset = baseOffset + 3;
        int counter = 1;

        while ( ColorFormat.compareColor(array, baseOffset, nextOffset)
        &&      counter < 128
        &&      nextOffset < data.limit()) {
            counter++;
            nextOffset += 3;         
        }

        return counter;
    }
    
    /**
     * 
     */
    private void encodeRleData( ByteBuffer inBuffer, 
                                ByteBuffer outBuffer,
                                int rleCount) throws IOException {
        byte[] color = new byte[3];
        /*
         *  Rle Farbe lesen, RLE Paket kodieren und mit Pixel in den 
         *  Schreibpuffer schreiben
         */
        inBuffer.get(color);
        outBuffer.put((byte)(128 + rleCount - 1));
        outBuffer.put(color);

        // Gleiche Farben im Eingabepuffer überspringen
        inBuffer.position(inBuffer.position() + (rleCount - 1) * 3);
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
        while( !ColorFormat.compareColor(s, 
                                        inOffset, 
                                        inOffset + 3)
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
}
