package propra.imageconverter.image;

import java.io.IOException;
import java.nio.ByteBuffer;
import propra.imageconverter.data.DataFormat;
import propra.imageconverter.data.IDataListener;
import propra.imageconverter.data.IDataListener.Event;
import propra.imageconverter.util.CheckedInputStream;
import propra.imageconverter.util.CheckedOutputStream;

/**
 *
 * 
 */
public class ImageCodecRLE extends ImageCodec {

    private ColorBuffer bufferedData; 
    private boolean isBufferedData;

    /*
     * 
     */
    public ImageCodecRLE(ImageResource resource) {
        super(resource);
    }

    /**
     * 
     * @param op
     * @throws IOException 
     */
    @Override
    public void begin(DataFormat.Operation op) throws IOException {
        super.begin(op);
        bufferedData = new ColorBuffer(DEFAULT_IMAGEBLOCK_SIZE * 2, 
                                image.getHeader().colorFormat());
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
        ColorBuffer buff = new ColorBuffer(DEFAULT_IMAGEBLOCK_SIZE,
                                            image.getHeader().colorFormat());
        
        /*
         * Rle Block dekodieren
         */
        int packetHeader;
        while((packetHeader = stream.read()) != -1) {
            
            // Paketkopf und Wiederholungen dekodieren
            packetHeader = packetHeader & 0xFF;
            int pixelCount = (packetHeader & 127) + 1;
            int packetLen = pixelCount * ColorFormat.PIXEL_SIZE;

            /*
             * Wenn im Ausgabepuffer nicht mehr genug Platz ist, Datenblock zum 
             * Empfänger senden
             */
            if( buff.position() + packetLen > buff.capacity()) {
                buff.flip();
                dispatchEvent(Event.DATA_BLOCK_DECODED, 
                                target, 
                                buff.getBuffer(),
                                false);
                buff.clear();
            }

            // RLE oder RAW Paket?
            Color rle = new Color();
            if(packetHeader > 127) {
                // Farbwert auslesen und Ausgabepuffer auffüllen
                stream.read(rle.get());
                buff.fill(rle, pixelCount);
            } else {
                // RAW Farben übertragen
                stream.read(buff.array(), 
                            buff.position(), 
                            packetLen);
                buff.position(buff.position() + packetLen);
            }
        }
        
        /**
         *  Letzten Block der Operation kennzeichnen und Restdaten
         *  übertragen
         */
        buff.flip();
        dispatchEvent(Event.DATA_BLOCK_DECODED, 
                        target, 
                        buff.getBuffer(),
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
        
        // Input Stream
        CheckedOutputStream stream = resource.getOutputStream();
        
        // Puffer erstellen
        ByteBuffer rleBlock = ByteBuffer.allocate(192 * ColorFormat.PIXEL_SIZE);
        ColorBuffer buff = new ColorBuffer(block, image.getHeader().colorFormat());
        

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
        ColorBuffer inBuffer = getDataToEncode(buff);
        int dataLimit = inBuffer.limit();
        
        // Über Bytes iterieren und gemäß RLE verarbeiten
        while(inBuffer.position() < dataLimit) {
            /*
             *  Wenn Blockgrenze erreichbar Restdaten puffern für nächsten Block, 
             *  nur wenn es sich nicht um den letzten Block handelt.
             */
            boolean boundary = inBuffer.position() + (127 * ColorFormat.PIXEL_SIZE) >= dataLimit;
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
                Color color = new Color();
                inBuffer.get(color);
                rleBlock.put((byte)(128 + colorCtr - 1));
                rleBlock.put(color.get());
                rleBlock.flip();

                // Gleiche Farben im Eingabepuffer überspringen
                inBuffer.position(inBuffer.position() + (colorCtr - 1) * 3);
                
            } else {      
               /*
                * Raw Pixel kopieren und Paketkopf schreiben
                */
                encodeRawData(inBuffer.getBuffer(), rleBlock);
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
    private int countRleColor(ColorBuffer data) {
        int offs = data.position() + ColorFormat.PIXEL_SIZE;
        int counter = 1;

        while ( data.compareColor(data.position(), offs)
            &&  counter < 128
            &&  offs < data.limit()) {
            offs += ColorFormat.PIXEL_SIZE;  
            counter++;
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
    private void bufferInputData(ColorBuffer inBuffer) {
        if(inBuffer == bufferedData) {
            bufferedData.getBuffer()
                        .compact();
        } else {
            bufferedData.clear();
            bufferedData.getBuffer()
                        .put(0, inBuffer.getBuffer(), inBuffer.position(), inBuffer.remaining());
            bufferedData.position(inBuffer.remaining());
        }
    }
    
    /**
     *  Wenn gepufferte Daten aus vorherigem Block vorhanden sind,
     *  den neuen Block an diesen Anhängen und passenden ByteBuffer 
     *  zurückgeben
     */
    private ColorBuffer getDataToEncode(ColorBuffer data) {
        if(isBufferedData) {
            bufferedData.put(data);
            bufferedData.flip();
            isBufferedData = false;
            return bufferedData;
        } 
        return data;
    }
}
