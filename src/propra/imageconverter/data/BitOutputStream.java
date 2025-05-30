package propra.imageconverter.data;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Klasse kapselt einen Stream und erlaubt das Schreiben einzelner Bits.
 * Die logische Ordnung der Bits beginnt bei dem höchstwertigen Bit und steigt
 * ab.
 */
public class BitOutputStream {
    
    // Zugeordneter Stream von dem Bits gelesen werden
    private final OutputStream stream;
    
    // Aktuelles Schreib-Byte
    private int value;
    
    // Aktueller Bit-Index
    private int bitIndex;
    
    // Byte Zähler
    private int byteCounter;
    
    public BitOutputStream(OutputStream stream) {
        this.stream = stream;
        value = 0;
        bitIndex = 0;
    }

    /**
     * Anzahl geschriebener Bytes
     */
    public int getByteCounter() {
        return byteCounter;
    }
    
    /**
     * Schließt aktuelles Byte ab und flushed den Stream
     */
    public void flush() throws IOException {
        flushByte();
        stream.flush();
    }
    
    /**
     * Schreibt aktuelles Byte in Stream und beginnt nächstes Byte
     */
    public void flushByte() throws IOException {
        if(bitIndex > 0) {
            stream.write(value);
            value = 0;
            bitIndex = 0; 
            byteCounter++;
        }
    }
    
    /**
     * Schreibt ein Bit in den Stream
     */
    public void write(int bit) throws IOException {
        value |= (bit & 1) << (7 - bitIndex);
                
        if(bitIndex >= 7) {
            flushByte();
        } else { 
            bitIndex++;
        }
    }
    
    /**
     * Schreibt einen BitCode in den Stream
     */
    public void write(BitCode code) throws IOException {  
        int c = code.getCode();
        int len = code.getLength() - 1;
        
        for(int i=len; i>=0; i--) {
            write((c >>> i) & 1);
        }
    }
    
    /**
     * Schreibt Anzahl an Bits in den Stream
     */
    public void writeBits(int b, int bitLen) throws IOException {
        for(int i=bitLen-1; i>=0; i--) {
            write((b >>> i) & 1);
        }
    }
    
    /**
     * Schreibt ein Byte an aktuelle Bit-Position
     */
    public void writeByte(int b) throws IOException {
        for(int i=7; i>=0; i--) {
            write((b >>> i) & 1);
        }
    }
}
