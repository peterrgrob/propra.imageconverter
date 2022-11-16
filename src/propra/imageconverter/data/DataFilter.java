package propra.imageconverter.data;

import propra.imageconverter.util.Validatable;

/**
 *
 * @author pg
 */
public interface DataFilter extends Validatable {
    public void reset();
    public void begin();
    public DataBuffer filter(DataBuffer inOut);
    public DataBuffer filter(DataBuffer in, DataBuffer out);
    public void end();
}
