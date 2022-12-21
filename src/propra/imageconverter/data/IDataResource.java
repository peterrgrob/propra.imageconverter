package propra.imageconverter.data;

import propra.imageconverter.util.CheckedInputStream;
import propra.imageconverter.util.CheckedOutputStream;
import java.io.IOException;

/**
 *
 * @author pg
 */
public interface IDataResource extends AutoCloseable {
    
    /**
     *
     * @return 
     * @throws IOException
     */
    public long length() throws IOException;
    
    /**
     * 
     * @return 
     * @throws IOException 
     */
    public long position() throws IOException;
    
    /**
     * 
     * @param p
     * @throws java.io.IOException
     */
    public void position(long p) throws IOException;
    
    /**
     *  
     * @return  
     */
    public CheckedInputStream getInputStream();
    
    /**
     *  
     * @return  
     */
    public CheckedOutputStream getOutputStream();  
}
