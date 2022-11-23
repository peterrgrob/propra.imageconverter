package propra.imageconverter.data;

import java.nio.ByteBuffer;
import propra.imageconverter.image.ColorFormat;

/**
 *
 * @author pg
 */
public class DataBlock {
    public ByteBuffer data;
    public long sourcePosition;
    public long sourceLength;
}
