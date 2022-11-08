package propra.imageconverter.image;

import java.nio.ByteBuffer;
import propra.imageconverter.util.DataBuffer;

/**
 *
 * @author pg
 */
public class ImageTranscoderRLE extends ImageTranscoder {

    int currentPacketHeader;
    int currentRepetitionCount;
    int decodedBytes;

    @Override
    public void begin(ColorFormat inFormat) {
        super.begin(inFormat);
        currentPacketHeader = 0;
        currentRepetitionCount = 0;
        decodedBytes = 0;
    }
    
    /**
     *
     * @return
     */
    @Override
    public boolean isValid() {
        return true;
    }
    
    /** 
     *
     * @return
     */
    protected boolean isRlePacket() {
        return currentPacketHeader > 127;
    }
    
    protected int getRepetitionCount() {
        return (currentPacketHeader & 127) + 1;
    }

    @Override
    protected long _encode(DataBuffer in, DataBuffer out) {

        return 0;
    }

    @Override
    protected long _decode(DataBuffer in, DataBuffer out) {
        byte[] color = new byte[3];
        ByteBuffer inBytes = in.getBuffer();
        ByteBuffer outBytes = out.getBuffer();
        
        while(inBytes.position() < in.getCurrDataLength()) {
            
            currentPacketHeader = inBytes.get() & 0xFF;
            currentRepetitionCount = getRepetitionCount();
            int currentBytes = currentRepetitionCount * 3;

            if(isRlePacket()) {
                inBytes.get(color);
                for(int i=0;i<currentRepetitionCount;i++) {
                    outBytes.put(color);
                }
            } else {
                inBytes.get(outBytes.array(), 
                            decodedBytes, 
                            currentBytes);
                out.skipBytes(currentBytes);
            }

            decodedBytes += currentBytes;
        }
        
        out.setCurrDataLength(decodedBytes);
        out.getBuffer().clear();
        
        return decodedBytes;
    }

    @Override
    protected long _pass(DataBuffer in, DataBuffer out) {
        return 0;
    }
}
