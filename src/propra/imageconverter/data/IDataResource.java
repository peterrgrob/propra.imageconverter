package propra.imageconverter.data;

import java.nio.ByteBuffer;
import java.io.Closeable;
import java.io.IOException;

/**
 *
 * @author pg
 */
public interface IDataResource extends  Closeable {
    
    /**
     * 
     * @param pos
     * @throws IOException 
     */
    public void position(long pos) throws IOException;
    
    /**
     * 
     * @return
     * @throws IOException 
     */
    public long position() throws IOException;
    
    /**
     * 
     */
    public long length() throws IOException;
    
    /**
     *  
     */
    public DataInputStream getCheckedInputStream();
    
    /**
     *  
     */
    public DataOutputStream getCheckedOutputStream();
    
    /**
     * 
     * @param buffer
     * @throws IOException 
     */
    public int read(ByteBuffer buffer) throws IOException;
    
    /**
     * 
     * @param offset
     * @param buffer
     * @throws IOException 
     */
    public int read(long offset, ByteBuffer buffer) throws IOException;  
    
    /**
     * 
     * @param offset
     * @param length
     * @return
     * @throws IOException 
     */
    public ByteBuffer read(long offset, int length) throws IOException;
    
    /**
     * 
     * @return
     * @throws IOException 
     */
    public String readLine() throws IOException ;
    
    
    /**
     * 
     * @param buffer
     * @throws IOException 
     */
    public void write(ByteBuffer buffer) throws IOException;
    
    /**
     * 
     * @param offset
     * @param buffer
     * @throws IOException 
     */
    public void write(long offset, ByteBuffer buffer) throws IOException;    
    
    /**
     * 
     * @param buffer
     * @throws IOException 
     */
    public void writeBuffered(ByteBuffer buffer) throws IOException;
    
    /**
     * 
     * @throws IOException
     */
    public void flush() throws IOException;
}
