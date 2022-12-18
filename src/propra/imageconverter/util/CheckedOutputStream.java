package propra.imageconverter.util;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 *
 */
public class CheckedOutputStream extends BufferedOutputStream {
    
    // Prüfsumme
    private Checksum checksum;
    
    /**
     *  Konstruktoren
     */
    public CheckedOutputStream(OutputStream in) {
        super(in);
    }
    
    public CheckedOutputStream( OutputStream in,
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
   
    public ByteBuffer write(ByteBuffer buff) throws IOException {
        write(buff.array(), buff.position(), buff.limit());
        return buff;
    }
}
