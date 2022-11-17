package propra.imageconverter.image;

import java.nio.ByteBuffer;
import propra.imageconverter.data.DataBuffer;
import propra.imageconverter.data.DataFormat;

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
        byte[] color = new byte[3];
        ByteBuffer inBytes = in.getBuffer();
        ByteBuffer outBytes = out.getBuffer();
        int colorSize = 3;
        int rawCounter = 0;
           
        // Über Bytes iterieren und gemäß RLE verarbeiten
        while(inBytes.position() < in.getDataLength()) {
         
            // Anzahl gleicher Farben zählen
            int rleCtr = countRleColor(in.getBuffer(), in.getDataLength());
            if(rleCtr > 1) {
                
                // RLE Block verarbeiten
                // Farbwert speichern
                inBytes.get(color);
                
                // Paketkopf und Farbwert schreiben
                outBytes.put((byte)(127 + rleCtr));
                outBytes.put(color);
                
                // Gleiche Farben im Eingabepuffer überspringen
                in.skipBytes((rleCtr - 1) * colorSize);
                rleCtr = 0;
                
            } else {
                
                // Raw Block verarbeiten
                int headerPosition = outBytes.position();
                
                // Paketkopfbyte überspringen 
                out.skipBytes(1);
                
                // Unterschiedliche Farben iterieren
                while(!compareColor(inBytes.array(), 
                                    inBytes.position(),
                                    inBytes.position() + colorSize) && rawCounter <= 127) {
                    
                    // Farbe übertragen
                    inBytes.get(color);
                    outBytes.put(color);
                                    
                    rawCounter++;
                }
                
                // Paketkopf speichern
                outBytes.put(headerPosition, (byte)(rawCounter - 1));
                rawCounter = 0;
            }
        }
        
        // Anzahl der dekodierten Bytes setzen
        out.setDataLength(outBytes.position());  
        
        // Positionszeiger zurücksetzen
        out.getBuffer().clear();
              
        return out.getDataLength();
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
        while(inBytes.position() < in.getDataLength()) {
            
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
        out.setDataLength(decodedBytes);        
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

    /**
     * 
     * @param dataFormat 
     */
    @Override
    public void dataFormat(DataFormat dataFormat) {
    }

    /**
     * 
     * @return 
     */
    @Override
    public DataFormat dataFormat() {
        return null;
    }
}
