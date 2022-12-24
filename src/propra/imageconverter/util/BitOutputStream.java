package propra.imageconverter.util;

import java.io.IOException;
import java.io.OutputStream;

/**
 * 
 * @author pg
 */
public class BitOutputStream {
    
    // Zugeordneter Stream von dem Bits gelesen werden
    private final OutputStream stream;
    
    // Aktuelles Schreib-Byte
    private byte value;
    
    // Aktueller Bit-Index
    private byte bitIndex;
    
    // Byte Zähler
    private int byteCounter;
    
    /**
     * 
     * @param stream 
     */
    public BitOutputStream(OutputStream stream) {
        this.stream = stream;
        value = 0;
        bitIndex = 0;
    }

    /**
     * 
     * @return 
     */
    public int getByteCounter() {
        return byteCounter;
    }
    
    /**
     * 
     * @throws IOException 
     */
    public void flush() throws IOException {
        flushByte();
        stream.flush();
    }
    
    /**
     * 
     * @throws IOException 
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
     * 
     * @param bit
     * @throws IOException 
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
     * 
     * @param code
     * @throws IOException 
     */
    public void write(BitCode code) throws IOException {  
        int c = code.getCode();
        
        for(int i=code.getLength()-1; i>=0; i--) {
            write((c >>> i) & 1);
        }
    }
    
    /**
     * 
     * @param bitLen
     * @return 
     */
    public void writeBits(int b, int bitLen) throws IOException {
        for(int i=bitLen-1; i>=0; i--) {
            write((b >>> i) & 1);
        }
    }
    
    /**
     * Schreibt ein Byte 
     * 
     * @param b
     * @throws IOException 
     */
    public void writeByte(int b) throws IOException {
        for(int i=7; i>=0; i--) {
            write((b >>> i) & 1);
        }
    }
}
