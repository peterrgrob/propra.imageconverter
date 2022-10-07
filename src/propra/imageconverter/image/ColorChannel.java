package propra.imageconverter.image;

/**
 * 
 * @author pg
 */
public class ColorChannel implements Comparable<ColorChannel> {
    protected int index;
    protected int mask;

    /**
     * 
     * @param shift
     * @param mask 
     */
    ColorChannel(int index) {
        this.index = index;
    }

    /**
     *
     * @return
     */
    public int getIndex() {
        return index;
    }

    /**
     *
     * @param o
     * @return
     */
    @Override
    public int compareTo(ColorChannel o) {
        if (index == o.index &&
            mask == o.mask) {
            return 0;
        }
        return -1;
    }
}
