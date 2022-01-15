package analyzers.readers.unit_tests;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.util.HexFormat;

public class InstructionsFileReaderTest {
/*

    public void traceFileReader() throws IOException {

        try {
            BufferedReader br = new BufferedReader(new FileReader(this.pathToFile));
            String line;
            MemBufferBlock memBufferBlock;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("Start:") || line.startsWith("End:")) {

                    String[] entries = line.split("\\s+");

                    System.out.println("A loop has been encountered " + entries[1]);
                    BigInteger bigPCAddr = new BigInteger(entries[1].substring(2), 16);
                    System.out.println(" The Hex pc address is " + bigPCAddr.toString(16));

                    EventType event = line.startsWith("Start") ? EventType.START : EventType.END;

                    memBufferBlock = new MemBufferBlock(event, bigPCAddr);
                    System.out.println("The event is " + EventType.getStringEventType(memBufferBlock.getEvent()));

                } else {
                    assert (line.startsWith("Store") || line.startsWith("Load"));

                    String[] entries = line.split(" ");
                    System.out.println("A load/store has been encountered " + entries[1]);

                    BigInteger bigPCAddr = new BigInteger(entries[1].substring(2), 16);
                    System.out.println(" The Hex pc address is " + toHexString(bigPCAddr));

                    BigInteger approxRefAddress = new BigInteger(entries[2].substring(2), 16);
                    System.out.println(" The approximate reference address is " + toHexString(approxRefAddress));

                    EventType event = line.startsWith("Store") ? EventType.STORE : EventType.LOAD;
                    System.out.println("The event is " + EventType.getStringEventType(event));

                    //memBufferBlock = new MemBufferBlock(event, approxRefAddress, 0, )
                }


            }

        } catch (FileNotFoundException fileNotFoundException) {
            System.out.println("The provided file name shortcut or filepath is invalid.");
            fileNotFoundException.printStackTrace();
            return;
        }
    }


*/
}
