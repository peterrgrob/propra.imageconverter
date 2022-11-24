package propra.imageconverter.image;

import java.nio.ByteBuffer;

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
    protected ByteBuffer _encode(ByteBuffer in, ByteBuffer out) {
        byte[] color = new byte[3];
        int colorSize = 3;
        int rawCounter = 0;
           
        // Über Bytes iterieren und gemäß RLE verarbeiten
        while(in.position() < in.limit()) {
         
            // Anzahl gleicher Farben zählen
            int rleCtr = countRleColor(in, in.limit());
            if(rleCtr > 1) {
                
                // RLE Block verarbeiten
                // Farbwert speichern
                in.get(color);
                
                // Paketkopf und Farbwert schreiben
                out.put((byte)(127 + rleCtr));
                out.put(color);
                
                // Gleiche Farben im Eingabepuffer überspringen
                in.position(in.position() + (rleCtr - 1) * colorSize);
                
            } else {
                
                // Raw Block verarbeiten
                int headerPosition = out.position();
                
                // Paketkopfbyte überspringen 
                out.position(out.position() + 1);
                
                // Unterschiedliche Farben iterieren
                while(!compareColor(in.array(), 
                                    in.position(),
                                    in.position() + colorSize) && rawCounter <= 127) {
                    
                    // Farbe übertragen
                    in.get(color);
                    out.put(color);
                                    
                    rawCounter++;
                }
                
                // Paketkopf speichern
                out.put(headerPosition, (byte)(rawCounter - 1));
                rawCounter = 0;
            }
        }
        
        // Anzahl der dekodierten Bytes setzen
        out.limit(out.position());  
        
        // Positionszeiger zurücksetzen
        out.rewind();
              
        return out;
    }

    /**
     *
     * @param in
     * @param out
     * @return
     */
    @Override
    protected ByteBuffer _decode(ByteBuffer in, ByteBuffer out) {
        byte[] color = new byte[3];
        
        // Über Bytes iterieren und gemäß RLE verarbeiten
        while(in.position() < in.limit()) {
            
            // Paketkopf und Wiederholungen einlesen
            currentPacketHeader = in.get() & 0xFF;
            currentRepetitionCount = getRepetitionCount();
            
            // Anzahl der zu extrahierenden Bytes
            int currentBytes = currentRepetitionCount * 3;

            // Auf fehlerhafte Datenlänge prüfen
            if(decodedBytes + currentBytes > out.capacity()) {
                ByteBuffer t = ByteBuffer.allocate(out.capacity()<<1);
                t.put(out.array(), 0, out.position());
                out = t;
            }
            
            // RLE oder RAW Paket?
            if(isRlePacket()) {
                // Farbwert lesen
                in.get(color);
                
                // Farbwert in Ausgabebuffer schreiben
                for(int i=0;i<currentRepetitionCount;i++) {
                    out.put(color);
                }
                
            } else {
                // RAW Farben übertragen
                in.get(out.array(), 
                            decodedBytes, 
                            currentBytes);
                
                // Positionszeiger weiterschieben
                out.position(out.position() + currentBytes);
            }
            
            decodedBytes += currentBytes;
        }
        
        // Positionszeiger zurücksetzen
        out.rewind();
        
        // Anzahl der dekodierten Bytes setzen und zurückgeben
        out.limit(decodedBytes);        
        return out;
    }
    
    /**
     *
     * @return
     */
    @Override
    public boolean isValid() {
        return true;
    }
    
    /**
     * 
     * @param data
     * @return 
     */
    private int countRleColor(ByteBuffer data, int length) {
        byte[] array = data.array();
        int baseOffset = data.position();
        int runningOffset = baseOffset + 3;
        int counter = 1;
        
        // Zählen solange Farben gleich sind
        while (compareColor(array, baseOffset, runningOffset)) {

            counter++;
            runningOffset += 3;
            
            // Zähler max, oder Ende erreicht?
            if( counter > 127
            ||  runningOffset >= length) {
                break;
            }
        }
        
        return counter;
    }
   
    /**
     * 
     * @param array
     * @param offset0
     * @param offset1
     * @return 
     */
    boolean compareColor(byte[] array, int offset0, int offset1) {
        return (array[offset0 + 0] == array[offset1 + 0]
            &&  array[offset0 + 1] == array[offset1 + 1]
            &&  array[offset0 + 2] == array[offset1 + 2]);
    }
}
