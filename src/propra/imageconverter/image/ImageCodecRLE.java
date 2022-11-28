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
        int inColorCounter;
        int inPacketHeader;
        int outNextBytes;
    
        // Puffer vorbereiten
        if(outBlock.data == null) {
            outBlock.data = ByteBuffer.allocate(DEFAULT_BLOCK_SIZE);
        }
        ByteBuffer outBuffer = outBlock.data;
        ByteBuffer inBuffer = bufferedData;
        byte[] outBytes = outBuffer.array();
        
        /**
         * Blockweise Daten einlesen und dekodieren bis Ausgabeblock 
         * gefüllt ist
         */
        while( inOffset < inLength ) {
            
            // Blockgröße verkleinern am Ende der Datei
            if(inOffset + inBuffer.capacity() > inLength) {
                inBuffer.limit(inBuffer.capacity() - ((inOffset + inBuffer.capacity()) - inLength));
            }
            
            // Block von Resource lesen
            int inLen = resource.read(inBuffer);
            inOffset += inLen;
            
            /**
             * Rle Block dekodieren
             */
            while(inBuffer.remaining() != 0) {

                // Paketkopf und Wiederholungen dekodieren
                inPacketHeader =  inBuffer.get() & 0xFF;
                inColorCounter = (inPacketHeader & 127) + 1;
                outNextBytes = inColorCounter * 3;

                // Wenn gefüllt, Datenblock zum Empfänger senden
                if( outBuffer.position() + outNextBytes > outBuffer.capacity()) {
                    
                    outBuffer.flip();
                    dispatchEvent(  Event.DATA_BLOCK_DECODED, 
                                    target, 
                                    outBlock);
                    outBuffer.clear();
                }
                
                /**
                 *  Wenn nicht mehr genügend Daten im Block vorhanden sind,
                 *  Eingabeposition zurückschieben und neuen Block einlesen
                 */
                if((inPacketHeader > 127 && inBuffer.remaining() < 3)
                || (inPacketHeader <= 127 && inBuffer.remaining() < outNextBytes)) {
                        
                        inOffset = inOffset - inBuffer.remaining() - 1;
                        inBuffer.position(inBuffer.position() - 1);
                        resource.position(inOffset); 
                        break;
                }
                
                // RLE oder RAW Paket?
                if(inPacketHeader > 127) {
                    
                    // Farbwert auslesen und auffüllen
                    inBuffer.get(inRleColor);
                    int o = ColorFormat.fillColor(inRleColor,
                                                    outBytes,
                                                    outBuffer.position(),
                                                    inColorCounter);
                    outBuffer.position(o);
                    
                } else {

                    // RAW Farben übertragen
                    outBuffer.put(inBuffer.array(), 
                                inBuffer.position(), 
                                outNextBytes);
                    inBuffer.position(inBuffer.position() + outNextBytes);
                }
            }
            
            // Eingabedaten filtern
            dispatchEvent(Event.DATA_IO_READ, 
                            this, 
                            new DataBlock(inBuffer.flip(),false));
            inBuffer.clear();
        } 
        

        /**
         *  Letzten Block der Operation kennzeichnen und Restdaten
         *  übertragen
         */
        if(resource.position() == resource.length()) {
            outBlock.lastBlock = true;
        }
        
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
        
        ByteBuffer rleBlock = ByteBuffer.allocate(192*3);

        // Farbkonvertierung
        if(image.getHeader().colorFormat().compareTo(ColorFormat.FORMAT_RGB) != 0) {   
            ColorFormat.convertColorBuffer( block.data, 
                                            ColorFormat.FORMAT_RGB, 
                                            block.data,
                                            image.getHeader().colorFormat());
        }
        
        /*
         *  Eingabedaten mit gepufferten Daten zusammenführen
         */
        ByteBuffer inBuffer = getDataToEncode(block.data);
        int dataLimit = inBuffer.limit();
        
        // Über Bytes iterieren und gemäß RLE verarbeiten
        while(inBuffer.position() < dataLimit) {
            
            /*
             *  Wenn Blockgrenze erreichbar, Restdaten puffern für nächsten Block
             *  wenn es sich nicht um den letzten Block handelt.
             */
            boolean boundary = inBuffer.position() + (127 * 3) >= dataLimit;
            if(boundary && !block.lastBlock) {
                
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
