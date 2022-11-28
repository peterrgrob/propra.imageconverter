package propra.imageconverter.image;

import java.io.IOException;
import java.nio.ByteBuffer;
import propra.imageconverter.checksum.Checksum;
import propra.imageconverter.data.DataBlock;
import propra.imageconverter.data.IDataListener;

/**
 *
 * @author pg
 */
public class ImageCodecRLE extends ImageCodecRaw {

    private final ByteBuffer bufferedData;
    private final ByteBuffer rleBuffer;

    /*
     * 
     */
    public ImageCodecRLE(   ImageResource resource, 
                            Checksum checksum) {
        super(resource, checksum);
        
        bufferedData = ByteBuffer.allocate(DEFAULT_BLOCK_SIZE);
        rleBuffer = ByteBuffer.allocate(DEFAULT_BLOCK_SIZE * 2);
    }
    
    /*
     * 
     */
    @Override
    public void decode( DataBlock outBlock, IDataListener target) throws IOException {     
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
    
        ByteBuffer inBlock = rleBuffer;
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
            if(inOffset + inBlock.capacity() > inLength) {
                inBlock.limit(inBlock.capacity() - ((inOffset + inBlock.capacity()) - inLength));
            }
            
            // Block lesen
            int inLen = resource.read(inBlock);
            inOffset += inLen;
            
            // Block gemäß RLE verarbeiten
            while(inBlock.remaining() != 0) {

                // Paketkopf und Wiederholungen einlesen
                inPacketHeader =  inBlock.get() & 0xFF;
                inColorCounter = (inPacketHeader & 127) + 1;
                nextBytes = inColorCounter * 3;

                // Wenn gefüllt, Datenblock zum Empfänger senden
                if( outBuffer.position() + nextBytes > outBuffer.capacity()) {
                    outBuffer.flip();
                    sendEvent(  Event.DATA_BLOCK_DECODED, 
                                target, 
                                outBlock);
                    outBuffer.clear();
                }

                // RLE oder RAW Paket?
                if(inPacketHeader > 127) {
                    // Neuen Leseblock beginnen?
                    if(inBlock.remaining() < 3) {
                        inOffset = inOffset - inBlock.remaining() - 1;
                        inBlock.position(inBlock.position() - 1);
                        resource.position(inOffset);
                        break;
                    }
                    
                    // Farbwert auslesen und auffüllen
                    inBlock.get(inRleColor);
                    int o = ColorFormat.fillColor(inRleColor,
                                                    outBytes,
                                                    outBuffer.position(),
                                                    inColorCounter);
                    outBuffer.position(o);
                    
                } else {
                    // Neuen Leseblock beginnen?
                    if(inBlock.remaining() < nextBytes) {
                        inOffset = inOffset - inBlock.remaining() - 1;
                        inBlock.position(inBlock.position() - 1);
                        resource.position(inOffset);
                        break;
                    } 

                    // RAW Farben übertragen
                    outBuffer.put(inBlock.array(), 
                                inBlock.position(), 
                                nextBytes);
                    inBlock.position(inBlock.position() + nextBytes);
                }
            }
            
            // Eingabedaten filtern
            inBlock.flip();
            sendEvent(Event.DATA_IO_READ, 
                        this, 
                        new DataBlock(inBlock,false));
            inBlock.clear();
        } 
        
        if(resource.position() == resource.length()) {
            outBlock.lastBlock = true;
        }
        
        // Restdaten übertragen
        outBlock.data.flip();
        sendEvent(  Event.DATA_BLOCK_DECODED, 
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
        rleBuffer.clear();
        if(bufferedData.position() > 0) {
            bufferedData.flip();
            rleBuffer.put(bufferedData);
            bufferedData.clear();
        }
        rleBuffer.put(block.data);
        rleBuffer.flip();
        
        if(block.lastBlock) {
            block.lastBlock = true;
        }
        
        int writeColorCounter;
        int dataLimit = rleBuffer.limit();
        boolean boundary;        
        byte[] writeRleColor = new byte[3];
        ByteBuffer rleLine = ByteBuffer.allocate(192*3);
        
        // Über Bytes iterieren und gemäß RLE verarbeiten
        while(rleBuffer.position() < dataLimit) {
            
            /*
             *  Wenn Blockgrenze erreichbar, Restdaten puffern für nächsten Block
             *  wenn es sich nicht um den letzten Block handelt.
             */
            boundary = rleBuffer.position() + (127 * 3) >= dataLimit;
            if(boundary && !block.lastBlock) {
                bufferedData.put(0, rleBuffer, 
                                rleBuffer.position(), 
                                rleBuffer.remaining());
                bufferedData.position(rleBuffer.remaining());
                return; 
            }
            
            // Anzahl gleicher Farben zählen
            writeColorCounter = countRleColor(rleBuffer);
            if(writeColorCounter > 1 ) {
                
                /*
                 *  Rle Farbe lesen, RLE Paket kodieren und mit Pixel in den 
                 *  Schreibpuffer schreiben
                 */
                rleBuffer.get(writeRleColor);
                rleLine.put((byte)(128 + writeColorCounter - 1));
                rleLine.put(writeRleColor);
                
                // In Resource schreiben
                rleLine.flip();
                resource.writeBuffered(rleLine);
                rleLine.clear();

                // Gleiche Farben im Eingabepuffer überspringen
                rleBuffer.position(rleBuffer.position() + (writeColorCounter - 1) * 3);

                writeColorCounter = 0;

            } else {
                
               /*
                * Raw Pixel kopieren und Paketkopf schreiben
                */
                int inOffset = rleBuffer.position();
                int outOffset = 1;
                int writeRawCounter = 0;
                byte[] t = rleLine.array();
                byte[] s = rleBuffer.array();

                while( !ColorFormat.compareColor(s, inOffset, inOffset + 3)
                    && writeRawCounter < 128
                    && inOffset < rleBuffer.limit()) {
                    
                    t[outOffset++] = s[inOffset++];
                    t[outOffset++] = s[inOffset++];
                    t[outOffset++] = s[inOffset++];
                    
                    writeRawCounter++;
                }

                t[0] = (byte)(writeRawCounter - 1);
                rleBuffer.position(inOffset);

                // In Resource schreiben
                rleLine.limit(outOffset);
                resource.writeBuffered(rleLine); 
                rleLine.clear();
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
     * Zählt identische Pixel bis zu 128
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
}
