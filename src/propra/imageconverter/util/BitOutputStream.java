package propra.imageconverter.util;

import java.io.IOException;

/**
 * 
 */
public class BitOutputStream {
    
    // Zugeordneter Stream von dem Bits gelesen werden
    private final CheckedOutputStream stream;
    
    // Aktuelles Schreib-Byte
    private byte value;
    
    // Aktueller Bit-Index
    private byte bitIndex;
    
    // Byte Zähler
    private int byteCounter;
    
    /**
     *  Konstruktor
     */
    public BitOutputStream(CheckedOutputStream stream) {
        this.stream = stream;
        value = 0;
        bitIndex = 0;
    }

    /**
     *  Getter 
     */
    public int getByteCounter() {
        return byteCounter;
    }
    
    /**
     * 
     */
    public void flush() throws IOException {
        flushByte();
        stream.flush();
    }
    
    /**
     *  Schließt Byte ab und schreibt es in den Stream
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
     *  Schreibt ein Bit in den Stream
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
     *  Schreibt einen BitCode in den Stream
     */
    public void write(BitCode code) throws IOException {  
        int c = code.getCode();
        
        for(int i=code.getLength()-1; i>=0; i--) {
            write((c >>> i) & 1);
        }
    }
    
    /**
     *  Schreibt ein Byte 
     */
    public void writeByte(int b) throws IOException {
        for(int i=7; i>=0; i--) {
            write((b >>> i) & 1);
        }
    }
}
