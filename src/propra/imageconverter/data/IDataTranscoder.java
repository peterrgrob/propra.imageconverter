package propra.imageconverter.data;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

/**
 *
 * @author pg
 */
public interface IDataTranscoder {
    /**
     *
     * @param dataFilter
     */
    public void begin(IDataFilter dataFilter);
     
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
     */
    public void end();
}
