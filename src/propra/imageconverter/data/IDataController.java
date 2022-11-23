package propra.imageconverter.data;

import java.io.IOException;

/**
 *
 * @author pg
 */
public interface IDataController {
    public void setup(IDataCodec input, IDataCodec output) throws IOException;
    public void process() throws IOException;
}
