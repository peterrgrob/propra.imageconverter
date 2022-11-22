package propra.imageconverter.data;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

/**
 *
 * @author pg
 */
public interface DataCoder {
    /**
     *
     * @param op
     * @param dataFilter
     */
    public void begin(DataFormat.Operation op, IDataFilter dataFilter);
     
    /*
     * @param out
     * @return Ausgabepuffer
     */
    public void encode(RandomAccessFile out, ByteBuffer in) throws IOException;
    
    /*
     * @param out
     * @return Ausgabepuffer
     */
    public void decode(RandomAccessFile in, IDataCallback out) throws IOException;
    
    /**
     *
     * @param op
     */
    public void end();
    
    /**
     * @param op
     * @param buffer
     * @return 
     */
    public int codedLength(ByteBuffer buffer);
}
