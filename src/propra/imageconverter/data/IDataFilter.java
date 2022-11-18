package propra.imageconverter.data;

import java.nio.ByteBuffer;

/**
 *
 * @author pg
 */
public interface IDataFilter {
    public void begin();
    public ByteBuffer apply(ByteBuffer inOut);
    public ByteBuffer apply(ByteBuffer in, ByteBuffer out);
    public void end();
}
