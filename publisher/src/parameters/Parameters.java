package parameters;

public class Parameters {
    // -------------------------------------------------------------
    public final int PUBLISHER_FLAG = 2;   // ID:1, ID:2
    // -------------------------------------------------------------

    public final String BROKER_FILE_PATH = "./defaultbroker.txt";
    public final String BROKER_LIST_FILE_PATH = "./brokerlist.txt";

    public final String [] SONG_FILE_PATH = new String[] {"./songs_1", "./songs_2"};
    public final String []  PUBLISHER_IPS = new String[] {"127.0.0.1", "127.0.0.1"};
    public final int [] PUBLISHER_PORT = new int[] { 25000, 25001};
    public final int CHUNK_SIZE = 10000;


}
