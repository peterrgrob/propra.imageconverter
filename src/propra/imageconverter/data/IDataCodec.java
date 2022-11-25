package propra.imageconverter.data;

import java.io.IOException;

/**
 *
 * @author pg
 */
public interface IDataCodec {
    public void begin(DataFormat.Operation op) throws IOException;
    public void encode(DataBlock data) throws IOException;
    public void decode(DataBlock data, IDataTarget target) throws IOException;   
    public boolean isDataAvailable() throws IOException;
    public void end() throws IOException;
}
