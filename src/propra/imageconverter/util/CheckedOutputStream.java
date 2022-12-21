package propra.imageconverter.util;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * Kapselt einen InputStream und berechnet Prüfsumme
 * 
 * @author pg
 */
public class CheckedOutputStream extends FilterOutputStream {
       
    // Prüfsumme
    private Checksum checksum;
    
    // Prüfsumme aktiviert?
    private boolean checked;
    
    /**
     * 
     * @param in 
     */
    public CheckedOutputStream(OutputStream out) {
        super(out);
        checked = false;
    }
    
    /**
     * 
     * @param in
     * @param checksum 
     */
    public CheckedOutputStream( OutputStream out,
                                Checksum checksum) {
        super(out);
        this.checksum = checksum;
        checked = true;
    }
    
    /**
     * 
     * @param checksum 
     */
    public void setChecksum(Checksum checksum) {
        this.checksum = checksum;
        checked = true;
    }

    /**
     * 
     * @param checked 
     */
    public void checked(boolean checked) {
        this.checked = checked;
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
        out.write(b, off, len);
        if(checksum != null && checked) {
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
        out.write(b);
        if(checksum != null && checked) {
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
