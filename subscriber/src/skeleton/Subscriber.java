package skeleton;

import brokerinput.BrokerInfo;
import brokerinput.BrokerInputLoader;
import data.MusicFile;
import data.Value;
import parameters.Parameters;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class Subscriber extends Node {
    private BrokerInfo defaultBrokerInfo;
    private Parameters parameters = new Parameters();
    private List<MusicFile> infoSongList = new ArrayList<>(); // all songs for all brokers

    public void init() {
        BrokerInputLoader loader = new BrokerInputLoader();

        try {
            defaultBrokerInfo = loader.loadDefaultBroker();
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
        System.out.println("Default broker: " + defaultBrokerInfo.ip + ": " + defaultBrokerInfo.port);
    }

    public void getBrokerList() throws IOException  {
        System.out.println("connecting to: " + defaultBrokerInfo.ip  +  " : " + defaultBrokerInfo.port);
        Socket conversation = new Socket(defaultBrokerInfo.ip, defaultBrokerInfo.port);

        ObjectOutputStream out = new ObjectOutputStream(conversation.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(conversation.getInputStream());

        System.out.println("waiting for server to send initial token") ;
        String s = in.readUTF();

        out.writeUTF("subscriber");
        out.flush();

        s = in.readUTF();

        out.writeUTF("brokerlist");
        out.flush();

        s = in.readUTF();

        String [] words = s.split(",");

        int length = Integer.parseInt(words[0]);

        for (int i=0;i<length;i++) {
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

    public void getSongList() {
        int flag = 0;
        for (Broker b : brokers) {
            try {
                getSongListFromBroker(flag, b);
                flag++;
            } catch (IOException e) {
                System.out.println("Broker ignored");
            }
        }
    }

    public void getSongListFromBroker(int flag, Broker b) throws IOException {
        System.out.println("connecting to: " + b.IP  +  " : " + b.PORT);
        Socket conversation = new Socket(b.IP, b.PORT);

        ObjectOutputStream out = new ObjectOutputStream(conversation.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(conversation.getInputStream());

        System.out.println("waiting for server to send initial token") ;
        String s = in.readUTF();

        out.writeUTF("subscriber");
        out.flush();

        s = in.readUTF(); // what do you want to do?

        out.writeUTF("songlist");
        out.flush();

        s = in.readUTF(); // what is your ID?

        out.writeUTF(String.valueOf(parameters.SUBSCRIBER_FLAG));
        out.flush();

        System.out.println("Received song list from broker #" + flag);

        s = in.readUTF();

        String [] words = s.split(",");

        int length = Integer.parseInt(words[0]);

        for (int i=0;i<length;i++) {
            MusicFile mf = new MusicFile();
            mf.albumInfo = words[1 + i*4];
            mf.artistName = words[2 + i*4];
            mf.genre = words[3 + i*4];
            mf.trackName = words[4 + i*4];
            //System.out.println("received..." + mf.albumInfo + " " + mf.artistName + " " +mf.trackName );
            infoSongList.add(mf);
        }
        System.out.println("Success");
        conversation.close();
    }

    public void printSongList() {
        int length = infoSongList.size();
        for (int i=0;i<length;i++) {
            MusicFile musicFile = infoSongList.get(i);
            System.out.println("  ("+ i +")  " + musicFile.albumInfo + "," + musicFile.artistName + "," + musicFile.trackName);
        }
    }

    public void downloadSong(int song) {
        //System.out.println("you are downloading song with id: " + song);
        MusicFile musicFile = infoSongList.get(song);
        System.out.println("trying to download: " );
        System.out.println("  ("+ song +")  " + musicFile.albumInfo + "," + musicFile.artistName + "," + musicFile.trackName);

        String key = musicFile.artistName + "_" + musicFile.albumInfo + "_" + musicFile.trackName;
        Long songhashvalue = hash(key);

        int flag = findBroker(brokers, musicFile.artistName);
        //System.out.println("Sending request to Broker:  " + flag);
        try {
            getSongFromBroker(musicFile, flag, songhashvalue, brokers.get(flag) );
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("error while trying to download the song");
        }
    }

    private void getSongFromBroker(MusicFile musicFile, int flag, Long songhashvalue, Broker b) throws IOException {
        //System.out.println("connecting to: " + b.IP  +  " : " + b.PORT);
        Socket conversation = new Socket(b.IP, b.PORT);

        ObjectOutputStream out = new ObjectOutputStream(conversation.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(conversation.getInputStream());
        //System.out.println("waiting for server to send initial token") ;
        String s = in.readUTF();

        out.writeUTF("subscriber");
        out.flush();

        s = in.readUTF(); // what do you want to do?

        out.writeUTF("song");
        out.flush();

        out.writeUTF(String.valueOf(parameters.SUBSCRIBER_FLAG));
        out.flush();
        //System.out.println("Sending song request to broker #" + flag);
        //System.out.println("song hash value is " + songhashvalue);
        out.writeLong(songhashvalue);
        out.flush();

        s = in.readUTF();

        if (s.equalsIgnoreCase("error")) {
            System.out.println("song not found by the broker");
        } else {
            System.out.println("song found by the broker. incoming ...");
            Parameters params = new Parameters();
            long remainingsize = in.readLong();

           // System.out.println("remaining size received by broker is: " + remainingsize);
            //System.out.println("Receiving file from broker");
            int n = 0;
            int loop = 0;
            String filename =  musicFile.artistName + "-" +musicFile.albumInfo +"-" + musicFile.trackName + ".mp3";
            OutputStream outFile = new FileOutputStream(filename);

            while (remainingsize > 0) {
                if (remainingsize >= params.CHUNK_SIZE ) {
                    byte[] buffer = new byte[params.CHUNK_SIZE];
                    int success_bytes =in.read(buffer, 0, params.CHUNK_SIZE);

                    remainingsize = remainingsize- success_bytes;
                    outFile.write(buffer, 0, success_bytes);
                    outFile.flush();
                    n+=success_bytes;
                } else {
                    byte[] buffer = new byte[(int)remainingsize];
                    int success_bytes = in.read(buffer, 0, (int) remainingsize);
                    n+=success_bytes;
                    remainingsize -= success_bytes;

                    outFile.write(buffer, 0, success_bytes);
                    outFile.flush();
                }
                loop++;
            }
            outFile.close();
            System.out.println("Success");
        }
        conversation.close();
    }
}
