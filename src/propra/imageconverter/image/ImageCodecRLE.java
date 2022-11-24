package propra.imageconverter.image;

import java.io.IOException;
import java.nio.ByteBuffer;
import propra.imageconverter.checksum.Checksum;
import propra.imageconverter.data.DataBlock;
import propra.imageconverter.data.DataFormat;
import propra.imageconverter.data.IDataCallback;

/**
 *
 * @author pg
 */
public class ImageCodecRLE extends ImageCodec {
    
    private byte[] writeRleColor = new byte[3];
    private byte[] readRleColor = new byte[3];
    private int readColorCounter;
    private int readPacketHeader;
    private ByteBuffer writeRleBuffer = ByteBuffer.allocate(DEFAULT_BLOCK_SIZE);
    private ByteBuffer tmpRleBuffer = ByteBuffer.allocate(DEFAULT_BLOCK_SIZE * 2);

    /**
     * 
     * @param resource
     * @param checksum 
     */
    public ImageCodecRLE(   ImageResource resource, 
                            Checksum checksum) {
        super(resource, checksum);
    }
    
    /**
     * 
     * @param op
     * @param block
     * @throws IOException 
     */
    @Override
    public void processBlock(   DataFormat.Operation op, 
                                DataBlock block,
                                IDataCallback target) throws IOException {
        if(!isValid()
        ||  block == null) {
            throw new IllegalArgumentException();
        }
        
        switch(op) {
            case READ -> {
                decode(block, target);
            }
            case WRITE -> {
                encode(block, target);
            }
        }
    }
    
    /**
     * 
     * @param outBlock
     * @param target
     * @throws java.io.IOException
     */
    public void decode( DataBlock outBlock, IDataCallback target) throws IOException {
        
        ByteBuffer readBlock = ByteBuffer.allocate(DEFAULT_BLOCK_SIZE);
        
        if(outBlock.data == null) {
            outBlock.data = ByteBuffer.allocate(DEFAULT_BLOCK_SIZE);
        }
        
        ByteBuffer dataOut = outBlock.data;
        
        // Blockweise Daten einlesen und dekodieren bis Ausgabeblock gefüllt
        while(resource.position() < resource.length()) {
            
            // Blockgröße anpassen gegen Ende
            if(resource.position() + readBlock.capacity() > resource.length()) {
                readBlock.limit(readBlock.capacity() - (int)((resource.position() + readBlock.capacity()) - resource.length()));
            }
            
            // Block lesen
            resource.read(readBlock);

            // Block gemäß RLE verarbeiten
            while(readBlock.remaining() != 0) {

                if(readColorCounter == 0) {
                    // Paketkopf und Wiederholungen einlesen
                    readPacketHeader =  readBlock.get() & 0xFF;
                    readColorCounter = getRepetitionCount(readPacketHeader);
                }
                
                // Anzahl der zu extrahierenden Bytes
                int currentBytes = readColorCounter * 3;

                // Neuen Leseblock beginnen?
                if(readBlock.remaining() < currentBytes) {
                    resource.position(resource.position() - readBlock.remaining());
                    break;
                } 

                // Wenn gefüllt, Datenblock zum Empfänger senden
                if( dataOut.position() + currentBytes > dataOut.capacity()) {
                    sendData(target, outBlock);
                }

                // RLE oder RAW Paket?
                if(isRlePacket(readPacketHeader)) {

                    // Farbwert auslesen
                    readBlock.get(readRleColor);

                    // Farbwert in Ausgabebuffer schreiben
                    for(int i=0;i<readColorCounter;i++) {
                        dataOut.put(readRleColor);
                    }
                } else {

                    // RAW Farben übertragen
                    dataOut.put(readBlock.array(), 
                                readBlock.position(), 
                                currentBytes);
                    readBlock.position(readBlock.position() + currentBytes);
                }
                
                readColorCounter = 0;
            }

            // Eingabedaten filtern
            if(checksum != null) {
                readBlock.flip();
                checksum.apply(readBlock);
            }
            
            readBlock.clear();
        } 
        
        // Restdaten übertragen
        if(resource.position() == resource.length()) {
            outBlock.lastBlock = true;
        }
        sendData(target, outBlock);
    }
    /**
     * 
     */
    public void encode( DataBlock block, IDataCallback target) throws IOException{

        int colorSize = 3;
        
        ByteBuffer rleLine = ByteBuffer.allocate(192*3);
        tmpRleBuffer.clear();
        
        // Gepufferter Block vorhanden?
        if(writeRleBuffer.position() > 0) {
            writeRleBuffer.flip();
            tmpRleBuffer.put(writeRleBuffer);
            writeRleBuffer.clear();
        }
        tmpRleBuffer.put(block.data);
        tmpRleBuffer.flip();
        ByteBuffer in = tmpRleBuffer;
        
        // Über Bytes iterieren und gemäß RLE verarbeiten
        while(in.position() < in.limit()) {
            
            // Anzahl gleicher Farben zählen
            int writeColorCounter = countRleColor(in);
            if( writeColorCounter == -1
            &&  !block.lastBlock) {
                writeRleBuffer.put(0, in, in.position(), in.remaining());
                writeRleBuffer.position(in.remaining());
                return;
                
            } else if(writeColorCounter > 1 ) {
                
                in.get(writeRleColor);
                
                // RLE Block verarbeiten
                rleLine.put((byte)(127 + writeColorCounter));
                rleLine.put(writeRleColor);

                // Paketkopf und Farbwert schreiben
                rleLine.flip();
                resource.write(rleLine);
                rleLine.clear();

                // Gleiche Farben im Eingabepuffer überspringen
                in.position(in.position() + (writeColorCounter - 1) * colorSize);

                writeColorCounter = 0;

            } else {
                int writeRawCounter = countDifferentColors(in);
                if( writeRawCounter == -1
                &&  !block.lastBlock) {
                    writeRleBuffer.put(0, in, in.position(), in.remaining());
                    writeRleBuffer.position(in.remaining());
                    return;
                } else {
                    if(writeRawCounter == -1) {
                        writeRawCounter = in.remaining() / 3;
                    }
                    // Line speichern
                    rleLine.put((byte)(writeRawCounter - 1));
                    try{
                    rleLine.put(in.array(), in.position(), writeRawCounter * 3);
                    in.position(in.position() + writeRawCounter * 3);
                    rleLine.flip();
                    resource.write(rleLine); 
                    rleLine.clear();
                    writeRawCounter = 0;}
                    catch(Exception e) {
                        return;
                    }
                }
            }
        }
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
    private static boolean isRlePacket(int packet) {
        return packet > 127;
    }
    
    /**
     *
     * @return
     */
    private static int getRepetitionCount(int packet) {
        return (packet & 127) + 1;
    }
    
    /**
     * 
     * @param data
     * @return 
     */
    private int countRleColor(ByteBuffer data) {
        byte[] array = data.array();
        int baseOffset = data.position();
        int runningOffset = baseOffset + 3;
        int counter = 1;

        if(runningOffset == data.limit()) {
            return -1;
        }
        
        // Zählen solange Farben gleich sind
        while (compareColor(array, baseOffset, runningOffset)) {

            counter++;
            runningOffset += 3;

            // Zähler max, oder Ende erreicht?
            if(runningOffset == data.limit()) {
             return -1;
            }
            if( counter > 127) {
                break;
            }
        }

        return counter;
    }
    
        /**
     * 
     * @param data
     * @return 
     */
    private int countDifferentColors(ByteBuffer data) {
        byte[] src = data.array();
        int baseOffset = data.position();
        int runningOffset = baseOffset + 3;
        int counter = 1;
        
        if(runningOffset == data.limit()) {
            return -1;
        }
        try {
        // Zählen solange Farben ungleich sind
        while (!compareColor(src, runningOffset, runningOffset + 3)
            && counter < 128) {

            if(runningOffset == data.limit() - 3) {
                return -1;
            }
            
            counter++;
            runningOffset += 3;
        }
        }catch(Exception e) {
            return 0;
        }
        
        return counter;
    }
   
    /**
     * 
     * @param array
     * @param offset0
     * @param offset1
     * @return 
     */
    boolean compareColor(byte[] array, int offset0, int offset1) {
        return (array[offset0 + 0] == array[offset1 + 0]
            &&  array[offset0 + 1] == array[offset1 + 1]
            &&  array[offset0 + 2] == array[offset1 + 2]);
    }
}
