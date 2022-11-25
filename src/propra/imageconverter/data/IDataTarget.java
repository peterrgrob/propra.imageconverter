package propra.imageconverter.data;

import java.io.IOException;

/**
 *
 * @author pg
 */
public interface IDataTarget {
    /**
     * 
     * @param caller
     * @param block 
     */
    public void push(IDataCodec caller, DataBlock block) throws IOException;
}
