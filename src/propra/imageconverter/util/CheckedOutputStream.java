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
    
    public CheckedOutputStream(OutputStream out) {
        super(out);
        checked = false;
        this.checksum = new ChecksumNull();
    }
    
    public CheckedOutputStream(OutputStream out, IChecksum checksum) {
        super(out);
        PropraException.assertArgument(checksum);
        this.checksum = checksum;
        checked = true;
    }
    
    /**
     * Checksum Objekt setzen
     */
    public void setChecksum(IChecksum checksum) {
        PropraException.assertArgument(checksum);
        this.checksum = checksum;
        checked = true;
    }

    /**
     * Prüfsumme aktivieren
     */
    public void enableChecksum(boolean checked) {
        this.checked = checked;
    }
    
    /*
     *  Stream Methoden 
     */
    @Override
    public synchronized void write(byte[] b, int off, int len) throws IOException {
        out.write(b, off, len);
        if(checked) {
            checksum.update(b, off, len);
        }
    }
    
    @Override
    public synchronized void write(int b) throws IOException {
        out.write(b);
        if(checked) {
            checksum.update((byte)b);
        }
    }

    public ByteBuffer write(ByteBuffer buff) throws IOException {
        write(buff.array(), buff.position(), buff.limit());
        return buff;
    }
}
