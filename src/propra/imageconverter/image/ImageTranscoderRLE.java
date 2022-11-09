package propra.imageconverter.image;

import java.nio.ByteBuffer;
import propra.imageconverter.util.DataBuffer;

/**
 *
 * @author pg
 */
public class ImageTranscoderRLE extends ImageTranscoder {

    int currentPacketHeader;
    int currentRepetitionCount;
    int decodedBytes;

    @Override
    public void begin(ColorFormat inFormat) {
        // Operation initialisieren
        super.begin(inFormat);
        currentPacketHeader = 0;
        currentRepetitionCount = 0;
        decodedBytes = 0;
    }
    
    /** 
     *
     * @return
     */
    protected boolean isRlePacket() {
        return currentPacketHeader > 127;
    }
    
    /**
     *
     * @return
     */
    protected int getRepetitionCount() {
        return (currentPacketHeader & 127) + 1;
    }

    /**
     *
     * @param in
     * @param out
     * @return
     */
    @Override
    protected long _encode(DataBuffer in, DataBuffer out) {

        return 0;
    }

    /**
     *
     * @param in
     * @param out
     * @return
     */
    @Override
    protected long _decode(DataBuffer in, DataBuffer out) {
        byte[] color = new byte[3];
        ByteBuffer inBytes = in.getBuffer();
        ByteBuffer outBytes = out.getBuffer();
        
        // Über Bytes iterieren und gemäß RLE verarbeiten
        while(inBytes.position() < in.getCurrDataLength()) {
            
            // Paketkopf und Wiederholungen einlesen
            currentPacketHeader = inBytes.get() & 0xFF;
            currentRepetitionCount = getRepetitionCount();
            
            // Anzahl der zu extrahierenden Bytes
            int currentBytes = currentRepetitionCount * 3;

            // Auf fehlerhafte Datenlänge prüfen
            if(decodedBytes + currentBytes > outBytes.capacity()) {
                throw new IllegalArgumentException("arg");
            }
            
            // RLE oder RAW Paket?
            if(isRlePacket()) {
                // Farbwert lesen
                inBytes.get(color);
                
                // Farbwert in Ausgabebuffer schreiben
                for(int i=0;i<currentRepetitionCount;i++) {
                    outBytes.put(color);
                }
                
            } else {
                // RAW Farben übertragen
                inBytes.get(outBytes.array(), 
                            decodedBytes, 
                            currentBytes);
                
                // Positionszeiger weiterschieben
                out.skipBytes(currentBytes);
            }
            
            decodedBytes += currentBytes;
        }
        
        // Positionszeiger zurücksetzen
        out.getBuffer().clear();
        
        // Anzahl der dekodierten Bytes setzen und zurückgeben
        out.setCurrDataLength(decodedBytes);        
        return decodedBytes;
    }
    
    /**
     *
     * @return
     */
    @Override
    public boolean isValid() {
        return true;
    }
}
