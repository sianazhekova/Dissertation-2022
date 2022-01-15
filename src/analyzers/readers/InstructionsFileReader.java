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

    // The driver program
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

    /*
    public MemBufferBlock parseTraceFileLine(@NotNull String fileLine) {
        String[] entries = fileLine.split(" ");

        if (entries.length == 2) {
            assert(fileLine.startsWith(fileLine.startsWith("Start:") || fileLine.startsWith("End:")));
        }

        return null;
    }*/

    @Contract(pure = true)
    public static @NotNull String toHexString(@NotNull BigInteger decNum) {
        return decNum.toString(16);
    }

    public static void main(String[] args) throws IOException {


        InstructionsFileReader fr = new InstructionsFileReader("C:\\Users\\siana\\Work\\Year 3\\New Dissertation Part ll\\Static Analysis\\Generated text files\\nasa_int_sort_2_loops_stores_only.txt");

        try {
            fr.traceFileReader();
        } catch (IOException ioe) {
            System.out.println("There has been an error in reading the file lines");
        }

    }
}
