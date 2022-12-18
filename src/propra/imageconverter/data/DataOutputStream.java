package propra.imageconverter.data;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import propra.imageconverter.util.Checksum;

/**
 *
 * @author pg
 */
public class DataOutputStream extends BufferedOutputStream {
    
    // Prüfsumme
    protected Checksum checksum;
    
    /**
     *  Konstruktoren
     */
    public DataOutputStream(OutputStream in) {
        super(in);
    }
    
    public DataOutputStream( OutputStream in,
                            Checksum checksum) {
        super(in);
        this.checksum = checksum;
    }

    /**
     *  Write Methoden 
     */
    @Override
    public synchronized void write(byte[] b, int off, int len) throws IOException {
        super.write(b, off, len);
        if(checksum != null) {
            checksum.update(b, off, len);
        }
    }

    @Override
    public synchronized void write(int b) throws IOException {
        super.write(b);
        if(checksum != null) {
            checksum.update((byte)b);
        }
    }
}
