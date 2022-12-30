package propra.imageconverter.util;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;


/**
 * Kapselt einen InputStream und berechnet optional eine Prüfsumme
 */
public class CheckedInputStream extends FilterInputStream {
    
    // Prüfsumme
    private IChecksum checksum;
    
    // Prüfsumme aktiviert?
    boolean checked;
    
    // Letzte Ergebnis
    private int r;
    
    public CheckedInputStream(InputStream in) {
        super(in);
        checked = false;
        checksum = new ChecksumNull();
    }
    
    public CheckedInputStream(InputStream in, IChecksum checksum) {
        super(in);
        PropraException.assertArgument(checksum);
        this.checksum = checksum;
        checked = true;
    }
    
    public void setChecksum(IChecksum checksum) {
        PropraException.assertArgument(checksum);
        this.checksum = checksum; 
        checked = true;
    }
   
    public void enableChecksum(boolean checked) {
        this.checked = checked;
    }
    
    /*
     *  Stream-Methoden
     */
    @Override
    public synchronized int read() throws IOException {
        r = in.read();
        if( checked &&  r != -1) {
            checksum.update((byte)r);
        }
        return r;
    }   

    @Override
    public synchronized int read(byte[] b, int off, int len) throws IOException {
        r = in.read(b, off, len);
        if( checked &&  r != -1) {
            checksum.update(b, off, len);
        }
        return r;
    }   
    
    @Override
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }
    
    public int read(ByteBuffer buff) throws IOException {
        return read(buff.array(), buff.position(), buff.limit());
    }
    
    @Override
    public byte[] readAllBytes() throws IOException {
        byte[] b = super.readAllBytes();
        if(b != null && checked) {
            checksum.update(b, 0, b.length);
        }
        return b;
    }
}
