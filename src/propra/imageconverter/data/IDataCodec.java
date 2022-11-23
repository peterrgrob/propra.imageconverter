package propra.imageconverter.data;

import propra.imageconverter.checksum.Checksum;
import propra.imageconverter.data.DataFormat.Operation;

/**
 *
 * @author pg
 */
public interface IDataCodec {
    public void setup(  IDataResource resource,
                        Checksum checksum);
    public void begin(Operation op);
    public void processBlock(Operation op, DataBlock data);
    public boolean isDataAvailable();
    public void end(Operation op);
}
