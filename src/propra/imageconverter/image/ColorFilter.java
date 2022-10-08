package propra.imageconverter.image;

/**
 *
 * @author pg
 */
public interface ColorFilter {

    /**
     *
     * @param input
     * @param output
     * @param x
     * @param y
     * @return
     */
    byte[] apply(byte[] input, byte[] output, int x, int y);

    /**
     *
     * @param io
     * @param x
     * @param y
     * @return
     */
    byte[] apply(byte[] io, int x, int y);
}
