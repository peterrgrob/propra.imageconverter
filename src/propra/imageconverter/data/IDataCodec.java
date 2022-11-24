package propra.imageconverter.data;

import java.io.IOException;
import propra.imageconverter.checksum.Checksum;
import propra.imageconverter.data.DataFormat.Operation;

/**
 *
 * @author pg
 */
public interface IDataCodec {
    public void setup(  IDataResource resource,
                        Checksum checksum);
    public void begin(Operation op) throws IOException;
    public void processBlock(Operation op, DataBlock data, IDataCallback target) throws IOException;
    public boolean isDataAvailable() throws IOException;
    public void end(Operation op) throws IOException;
}
