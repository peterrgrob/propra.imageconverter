package propra.imageconverter.util;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * Kapselt einen OutputStream und berechnet optional eine Prüfsumme
 */
public class CheckedOutputStream extends FilterOutputStream {
       
    // Prüfsumme
    private IChecksum checksum;
    
    // Prüfsumme aktiviert?
    private boolean checked;
    
    /**
     * 
     * @param in 
     */
    public CheckedOutputStream(OutputStream out) {
        super(out);
        checked = false;
        this.checksum = new ChecksumNull();
    }
    
    /**
     * 
     */
    public CheckedOutputStream(OutputStream out, IChecksum checksum) {
        super(out);
        PropraException.assertArgument(checksum);
        this.checksum = checksum;
        checked = true;
    }
    
    /**
     * 
     * @param checksum 
     */
    public void setChecksum(IChecksum checksum) {
        PropraException.assertArgument(checksum);
        this.checksum = checksum;
        checked = true;
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
     * @param b
     * @param off
     * @param len
     * @throws IOException 
     */
    @Override
    public synchronized void write(byte[] b, int off, int len) throws IOException {
        out.write(b, off, len);
        if(checked) {
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
        if(checked) {
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
