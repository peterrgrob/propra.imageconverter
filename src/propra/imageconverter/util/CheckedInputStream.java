package propra.imageconverter.util;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;


/**
 * 
 * @author pg
 */
public class CheckedInputStream extends FilterInputStream {
    
    // Prüfsumme
    private IChecksum checksum;
    
    // Prüfsumme aktiviert?
    boolean checked;
    
    // Letzte Ergebnis
    private int r;
    
    /**
     * 
     * @param in 
     */
    public CheckedInputStream(InputStream in) {
        super(in);
        checked = false;
    }
    
    /**
     * 
     * @param in
     * @param checksum 
     */
    public CheckedInputStream(InputStream in, IChecksum checksum) {
        super(in);
        this.checksum = checksum;
        checked = true;
    }
    
    /**
     * 
     * @param checksum 
     */
    public void setChecksum(IChecksum checksum) {
        this.checksum = checksum;
        checked = true;
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
     * @param checked 
     */
    public void enableChecksum(boolean checked) {
        this.checked = checked;
    }
    
    /**
     * 
     * @return
     * @throws IOException 
     */
    @Override
    public synchronized int read() throws IOException {
        r = in.read();
        if( checksum != null && checked &&  r != -1) {
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
        r = in.read(b, off, len);
        if( checksum != null && checked &&  r != -1) {
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

    /**
     * 
     * @return
     * @throws IOException 
     */
    @Override
    public byte[] readAllBytes() throws IOException {
        byte[] b = super.readAllBytes();
        if(b != null && checksum != null && checked) {
            checksum.update(b, 0, b.length);
        }
        return b;
    }
}
