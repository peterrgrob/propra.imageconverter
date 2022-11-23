package propra.imageconverter.data;

import java.nio.ByteBuffer;
import java.io.Closeable;
import java.io.IOException;
import propra.imageconverter.checksum.Checksum;

/**
 *
 * @author pg
 */
public interface IDataResource extends Closeable {
    
    public boolean checkChecksum(Checksum checksum);
    
    public void position(long pos) throws IOException;
    public long position() throws IOException;
    public long length() throws IOException;
    
    public void read(ByteBuffer buffer) throws IOException;
    public void read(long offset, ByteBuffer buffer) throws IOException;  
    public ByteBuffer read(long offset, int length) throws IOException;
    public String readLine() throws IOException ;
    
    public void write(ByteBuffer buffer) throws IOException;
    public void write(long offset, ByteBuffer buffer) throws IOException;    
}
