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
    protected Checksum checksum;
    
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
    @Override
    public synchronized int read() throws IOException {
        int r = super.read();
        if( checksum != null
        &&  r != -1) {
            checksum.update((byte)r);
        }
        return r;
    }   

    @Override
    public synchronized int read(byte[] b, int off, int len) throws IOException {
        int r = super.read(b, off, len);
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
