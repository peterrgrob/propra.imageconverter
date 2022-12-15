package propra.imageconverter.data;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import propra.imageconverter.checksum.Checksum;

/**
 *
 * @author pg
 */
public class DataInputStream extends BufferedInputStream {
    
    // Prüfsumme
    protected Checksum checksum;
    
    /**
     *  Konstruktoren
     */
    public DataInputStream(InputStream in) {
        super(in);
    }
    
    public DataInputStream( InputStream in,
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
}
