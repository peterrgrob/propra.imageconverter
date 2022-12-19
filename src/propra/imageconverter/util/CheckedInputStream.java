package propra.imageconverter.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;


/**
 * 
 * @author pg
 */
public class CheckedInputStream extends BufferedInputStream {
    
    // Prüfsumme
    private Checksum checksum;
    
    // Letzte Ergebnis
    private int r;
    
    /**
     * 
     * @param in 
     */
    public CheckedInputStream(InputStream in) {
        super(in);
    }
    
    /**
     * 
     * @param in
     * @param checksum 
     */
    public CheckedInputStream(  InputStream in,
                                Checksum checksum) {
        super(in);
        this.checksum = checksum;
    }
    
    /**
     * 
     * @param checksum 
     */
    public void setChecksum(Checksum checksum) {
        this.checksum = checksum;
    }
    
    /**
     * 
     * @return 
     */
    public boolean eof() {
        return r == -1;
    }
    
    /**
     * 
     * @return
     * @throws IOException 
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

    /**
     * 
     * @param b
     * @param off
     * @param len
     * @return
     * @throws IOException 
     */
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
     * @param b
     * @return
     * @throws IOException 
     */
    @Override
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }
    
    /**
     * 
     * @param buff
     * @return
     * @throws IOException 
     */
    public int read(ByteBuffer buff) throws IOException {
        return read(buff.array(), buff.position(), buff.limit());
    }
}
