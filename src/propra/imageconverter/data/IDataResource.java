package propra.imageconverter.data;

import java.io.IOException;
import propra.imageconverter.util.CheckedInputStream;
import propra.imageconverter.util.CheckedOutputStream;

/**
 * Interface für Ressourcen
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
