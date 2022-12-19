package propra.imageconverter.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;


/**
 *
 */
public class CheckedInputStream extends BufferedInputStream {
    
    // Prüfsumme
    private Checksum checksum;
    
    // Letzte Ergebnis
    private int r;
    
    /**
     *  Konstruktoren
     */
    public CheckedInputStream(InputStream in) {
        super(in);
    }
    
    public CheckedInputStream(  InputStream in,
                                Checksum checksum) {
        super(in);
        this.checksum = checksum;
    }
    
    /**
     * 
     */
    public boolean eof() {
        return r == -1;
    }
    
    /**
     * 
     */
    @Override
    public synchronized int read() throws IOException {
        r = super.read();
        if( checksum != null
        &&  r != -1) {
            checksum.update((byte)r);
        }
        return r;
    }   

    @Override
    public synchronized int read(byte[] b, int off, int len) throws IOException {
        r = super.read(b, off, len);
        if( checksum != null
        &&  r != -1) {
            checksum.update(b, off, len);
        }
        return r;
    }    
    
    /**
     * 
     */
    public int read(ByteBuffer buff) throws IOException {
        return read(buff.array(), buff.position(), buff.limit());
    }
}
