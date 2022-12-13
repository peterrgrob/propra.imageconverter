package propra.imageconverter.data;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 *
 */
public class BitResource {
    
    // Zugeordnete Resource von der Bits gelesen werden
    private IDataResource resource;
    
    // Aktuelles Byte
    private ByteBuffer value;
    
    // Aktueller Bit-Index
    private byte bitIndex;
    
    /**
     *  Konstruktor
     */
    public BitResource(IDataResource resource) {
        this.resource = resource;
        value = ByteBuffer.allocate(1);
        bitIndex = 8;
    }
    
    /**
     *  Liest ein Bit von der Resource
     */
    public byte readBit() throws IOException {
        /*
         *  Wenn Bytegrenze überschritten, neues Byte einlesen. 
         */
        if(bitIndex > 7) {
            resource.read(value);
            bitIndex = 0;
        }
        
        // Extrahiert und gibt den Bitwert zurück
        byte b = (byte)((value.array()[0] >>> (7 - bitIndex)) & 1);
        
        bitIndex++;
        return b;
    }
    
    /**
     *  Liest ein Byte ausgehend von der aktuellen Bit Position ein 
     */
    public byte readByte() throws IOException {
        byte b = 0;
        
        // Iteriert die Bits von vorne nach hinten und konstruiert ein Byte
        for(int i=7; i>=0; i--) {
            b |= readBit() << i;
        }
        
        return b;
    }
}
