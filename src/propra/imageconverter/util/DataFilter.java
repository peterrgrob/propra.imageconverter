package propra.imageconverter.util;

/**
 *
 * @author pg
 */
public interface DataFilter extends Validatable {
    public void reset();
    public void begin();
    public void filter(DataBuffer buffer);
    public void end();
}
