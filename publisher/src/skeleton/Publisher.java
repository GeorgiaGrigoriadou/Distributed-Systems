package skeleton;

import brokerinput.BrokerInfo;
import brokerinput.BrokerInputLoader;
import com.sun.xml.internal.messaging.saaj.packaging.mime.internet.ParameterList;
import data.MusicFile;
import data.Value;
import parameters.Parameters;
import utils.SongLoader;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Publisher extends Node  {
    public boolean open = true;
    private BrokerInfo defaultBrokerInfo;
    private Parameters parameters = new Parameters();
    private List<MusicFile> infoSongList = new ArrayList<>();
    private SongLoader songLoader = new SongLoader();
    private ServerSocket mainSocket;
    private String IP;
    private int PORT;
    private Map<Long, MusicFile> songMap = new HashMap<>();


    // load songs and default broker
    public void init() {
        Parameters params = new Parameters();
        // load default broker data
        BrokerInputLoader loader = new BrokerInputLoader();

        try {
            defaultBrokerInfo = loader.loadDefaultBroker();
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }

        IP = params.PUBLISHER_IPS[params.PUBLISHER_FLAG -1];
        PORT = params.PUBLISHER_PORT[params.PUBLISHER_FLAG-1];

        // load song metadata
        File directory = new File(parameters.SONG_FILE_PATH[parameters.PUBLISHER_FLAG-1]);
        File[] artistNames = directory.listFiles();
        for ( File fartist : artistNames) {
            for ( File falbum : fartist.listFiles()) {
                for ( File fsong : falbum.listFiles()) {
                    MusicFile musicData = new MusicFile();
                    musicData.artistName = fartist.getName();
                    musicData.albumInfo = falbum.getName();
                    musicData.genre = "unknown";
                    musicData.trackName = fsong.getName();
                    musicData.musicFileExtract = songLoader.load(fsong);

                    String key = musicData.artistName + "_" + musicData.albumInfo + "_" + musicData.trackName;
                    Long hashvalue = hash(key); // hash
                    songMap.put(hashvalue, musicData); // antistoixei ena id se kathe tragoudi
                    infoSongList.add(musicData); //bazei ta tragoudia sti lista
                }

            }
        }

      //  System.out.println("Music list: ");

      //  for (MusicFile song : infoSongList) {
      //      System.out.println(song);
    //    }

        System.out.println("Default broker: " + defaultBrokerInfo.ip + ": " + defaultBrokerInfo.port); //delete
    }

    public void getBrokerList() throws IOException {
        System.out.println("connecting to: " + defaultBrokerInfo.ip  +  " : " + defaultBrokerInfo.port); //delete
        Socket conversation = new Socket(defaultBrokerInfo.ip, defaultBrokerInfo.port);

        ObjectOutputStream out = new ObjectOutputStream(conversation.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(conversation.getInputStream());

        System.out.println("waiting for server to send initial token") ;
        String s = in.readUTF();

        out.writeUTF("publisher");
        out.flush();

        s = in.readUTF(); //what do you want ?

        out.writeUTF("brokerlist");
        out.flush();

        s = in.readUTF(); // η συμβολοσειρά 3,IP,IP, IP, PORT,PORT,PORT , HASH . HASH , HASH

        String [] words = s.split(",");

        int length = Integer.parseInt(words[0]);

        for (int i=0;i<length;i++) {   //σύνθεση
            Broker b = new Broker();
            b.IP = words[1 + i];
            b.PORT =  Integer.parseInt(words[1 + i + length]);
            b.hashnumber = Long.parseLong(words[1 + i + 2*length]);
            brokers.add(b);
        }

        int i =0;
        for (Broker b : brokers) {
            System.out.println("(" + i + ") " + b.IP +"," + b.PORT +"," + b.hashnumber);
            i++;
        }

        conversation.close();
    }

    public void pushAllSongs() {
        List<MusicFile> [] arrayOfLists = new List[brokers.size()];
        for (int i=0;i<brokers.size();i++) {
            arrayOfLists[i] = new ArrayList<>(); // για κάθε broker δημιουργεί μία λίστα τύπου MusicFile
        }

        for (MusicFile song : infoSongList) {
            String artistname = song.artistName;
            int id = findBroker(brokers, artistname); //αντιστοιχίζει κάθε τραγούδι με έναν broker
            arrayOfLists[id].add(song);
        }

        for (int i=0;i<arrayOfLists.length;i++) {
            try {
                Broker broker = brokers.get(i);
                List<MusicFile> songListForThatBroker = arrayOfLists[i];


                System.out.println("connecting to: " + broker.IP + " : " + broker.PORT);
                Socket conversation = new Socket(broker.IP, broker.PORT);

                ObjectOutputStream out = new ObjectOutputStream(conversation.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(conversation.getInputStream());

                System.out.println("waiting for broker to send initial token");
                String s = in.readUTF();

                out.writeUTF("publisher");
                out.flush();

                s = in.readUTF(); // what do you want?

                out.writeUTF("songlist");
                out.flush();

                s = in.readUTF(); // what is your ID?

                out.writeUTF(String.valueOf(parameters.PUBLISHER_FLAG));
                out.writeUTF(IP);
                out.writeUTF(String.valueOf(PORT));
                out.flush();

                int length = songListForThatBroker.size();
                s =  String.valueOf(length); // "3"

                for (int j=0;j<length;j++) { //αποσύνθεση
                    //albumInfo, artistname,genre,trackname,albumInfo, artistname,genre,trackname

                    MusicFile musicFile = songListForThatBroker.get(j);

                    s = s + "," + musicFile.albumInfo;
                    s = s + "," + musicFile.artistName;
                    s = s + "," + musicFile.genre;
                    s = s + "," + musicFile.trackName;


                }

                System.out.println("Sending to broker #" + i);
                out.writeUTF(s); //του στέλνει την songlist
                out.flush();

                out.close();
                in.close();

                System.out.println("Success");
            } catch (Exception ex) {
                System.out.println("broker skipped ");
            }
        }

    }

    public void createMainSocket() throws IOException {
        System.out.println("Opening main socket at port: " + PORT);
        mainSocket = new ServerSocket(PORT);
    }

    public void waitForClient() throws IOException {
        System.out.println("Waiting for a broker to connect ... ");
        Socket conversation = mainSocket.accept();

        System.out.println("A broker has connected  ... ");

        ObjectInputStream in = new ObjectInputStream(conversation.getInputStream());
        ObjectOutputStream out = new ObjectOutputStream(conversation.getOutputStream());

        System.out.println("Sending initial token");
        out.writeUTF("Who are you?");
        out.flush();

        String client = in.readUTF();

        if (client.equals("broker")) {
            Runnable runnable = () -> {
                acceptConnectionForBroker(in, out);
            };
            Thread brokerThread = new Thread(runnable);
            brokerThread.start();
        }
    }

    private void acceptConnectionForBroker(ObjectInputStream in, ObjectOutputStream out) {
        Parameters params = new Parameters();

        System.out.println("Thread created to service a broker");

        try {
            out.writeUTF("Who do you want?");
            out.flush();

            String s=  in.readUTF(); // song
            out.writeUTF("What is the subscriber ID?");
            out.flush();
            int subscriber_id = in.readInt();

            Long hashvalue = in.readLong(); // hash of song h(arist+album+title)

            System.out.println("Hash value received: " + hashvalue);

            out.writeUTF("ok");

            out.flush();

            MusicFile musicFile = songMap.get(hashvalue);

            byte[] musicFileExtract = musicFile.musicFileExtract;

            long size = musicFileExtract.length; // 4000000
            long remainingsize = size;
            long start_from = 0;

            out.writeLong(remainingsize);
            out.flush();

            System.out.println("Sending file to broker");

            int loop  = 0;

            while (remainingsize > 0) {
                if (remainingsize >= params.CHUNK_SIZE ) {
                    out.write(musicFileExtract, (int) start_from, params.CHUNK_SIZE);
                    remainingsize = remainingsize- params.CHUNK_SIZE;
                    start_from=start_from+ params.CHUNK_SIZE;
                    out.flush();
                } else {
                    out.write(musicFileExtract, (int) start_from,  (int)remainingsize);
                    remainingsize =0;
                    out.flush();
                }
                loop++;
            }
            out.close();
            in.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void destroyServerSocket() throws IOException {
        System.out.println("Closing main socket at port: " + PORT);
        mainSocket.close();
    }

}
