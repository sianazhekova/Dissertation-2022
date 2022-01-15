package analyzers.readers;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;


public class InstructionsFileReader {

    private String pathToFile;

    public InstructionsFileReader(String filePathName) {
        // For now, just try with a single file path
        // TODO: Add a configuration .json file, mapping the shortcut file names with the respective file paths.
        //  In the args section, only specify the path name shortcut
        pathToFile = filePathName;

    }

    public static MemBufferBlock parseTraceFileLine(@NotNull String fileLine) {
        MemBufferBlock memBufferBlock;
        if (fileLine.startsWith("Start:") || fileLine.startsWith("End:")) {

            String[] entries = fileLine.split("\\s+");

            //System.out.println("A loop has been encountered " + entries[1]);
            BigInteger bigPCAddr = new BigInteger(entries[1].substring(2), 16);
            //System.out.println(" The Hex pc address is " + bigPCAddr.toString(16));

            EventType event = fileLine.startsWith("Start") ? EventType.START : EventType.END;

            memBufferBlock = new MemBufferBlock(event, bigPCAddr);
            //System.out.println("The event is " + EventType.getStringEventType(memBufferBlock.getEvent()));

        } else {
            assert (fileLine.startsWith("Store") || fileLine.startsWith("Load"));

            String[] entries = fileLine.split(" ");
            //System.out.println("A load/store has been encountered " + entries[1]);

            BigInteger bigPCAddr = new BigInteger(entries[1].substring(2), 16);
            //System.out.println(" The Hex pc address is " + toHexString(bigPCAddr));

            BigInteger approxRefAddress = new BigInteger(entries[2].substring(2), 16);
            //System.out.println(" The approximate reference address is " + toHexString(approxRefAddress));

            BigInteger sizeOfAccess = new BigInteger(entries[3]);
            //System.out.println(" The size of access is " + sizeOfAccess.toString(10));

            EventType event = fileLine.startsWith("Store") ? EventType.STORE : EventType.LOAD;
            //System.out.println("The event is " + EventType.getStringEventType(event));

            memBufferBlock = new MemBufferBlock(event, approxRefAddress, sizeOfAccess, bigPCAddr);

        }

        return memBufferBlock;
    }

    @Contract(pure = true)
    public static @NotNull String toHexString(@NotNull BigInteger decNum) {
        return decNum.toString(16);
    }

    /*
    public static void main(String[] args) throws IOException {
        InstructionsFileReader fr = new InstructionsFileReader("C:\\Users\\siana\\Work\\Year 3\\New Dissertation Part ll\\Static Analysis\\Generated text files\\nasa_int_sort_2_loops_stores_only.txt");
        try {
            fr.traceFileReader();
        } catch (IOException ioe) {
            System.out.println("There has been an error in reading the file lines");
        }
    } */
}
