package propra.imageconverter.util;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * 
 * @author pg
 */
public class CheckedOutputStream extends BufferedOutputStream {
    
    // Prüfsumme
    private Checksum checksum;
    
    /**
     * 
     * @param in 
     */
    public CheckedOutputStream(OutputStream in) {
        super(in);
    }
    
    /**
     * 
     * @param in
     * @param checksum 
     */
    public CheckedOutputStream( OutputStream in,
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
     * @param b
     * @param off
     * @param len
     * @throws IOException 
     */
    @Override
    public synchronized void write(byte[] b, int off, int len) throws IOException {
        super.write(b, off, len);
        if(checksum != null) {
            checksum.update(b, off, len);
        }
    }
    
    /**
     * 
     * @param b
     * @throws IOException 
     */
    @Override
    public synchronized void write(int b) throws IOException {
        super.write(b);
        if(checksum != null) {
            checksum.update((byte)b);
        }
    }
    
    /**
     * 
     * @param buff
     * @return
     * @throws IOException 
     */
    public ByteBuffer write(ByteBuffer buff) throws IOException {
        write(buff.array(), buff.position(), buff.limit());
        return buff;
    }
}
