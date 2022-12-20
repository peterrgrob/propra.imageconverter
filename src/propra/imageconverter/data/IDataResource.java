package propra.imageconverter.data;

import propra.imageconverter.util.CheckedOutputStream;
import propra.imageconverter.util.CheckedInputStream;
import java.io.IOException;

/**
 *
 * @author pg
 */
public interface IDataResource extends AutoCloseable {
    
    /**
     * 
     */
    public long length() throws IOException;
    
    /**
     * 
     */
    public long position() throws IOException;
    
    /**
     * 
     */
    public void position(long p) throws IOException;
    
    /**
     *  
     */
    public CheckedInputStream getInputStream();
    
    /**
     *  
     */
    public CheckedOutputStream getOutputStream();  
}
