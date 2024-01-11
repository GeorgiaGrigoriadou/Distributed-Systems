package skeleton;

import brokerinput.BrokerInfo;
import brokerinput.BrokerInputLoader;
import data.MusicFile;
import parameters.Parameters;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

public class Broker extends Node implements Comparable<Broker> {
    public List<Consumer> registeredUsers = new ArrayList<>();
    public List<Publisher> registeredPublisher = new ArrayList<>();
    public String IP;
    public int PORT;
    public long hashnumber;
    public List<MusicFile>[] arrayOfLists = new List[2];
    ;
    public LinkedBlockingQueue<Long> orderQueue = new LinkedBlockingQueue<>();

    public int flag;

    public boolean open = true;

    ServerSocket mainSocket;

    public Broker() {

    }

    public Broker(int flag) {
        this.flag = flag;
        System.out.println("Broker: " + flag + " is initializing ");

        for (int i = 0; i < 2; i++) {
            arrayOfLists[i] = new ArrayList<>(); // δημιουργούμε μια λιστα για κάθε broker η οποία θα έχει τα info του τραγουδιού
        }
    }

    public void init() { // read broker list and create 3 brokers from file
        BrokerInputLoader loader = new BrokerInputLoader();

        try {
            BrokerInfo[] infoList = loader.loadBrokers();

            for (BrokerInfo info : infoList) {
                Broker b = new Broker();
                b.IP = info.ip;
                b.PORT = info.port;
                brokers.add(b); // την λίστα την κληρονομεί απο την node
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }

        calculateKeys(); // υπολογίζει το hash των broker

        sortBrokers(); //sort ta hash

        int i = 0;
        for (Broker b : brokers) {
            System.out.println("(" + i + ") " + b);
            i++;
        }

        IP = brokers.get(flag).IP;
        PORT = brokers.get(flag).PORT;

        System.out.println("Total brokers: " + brokers.size());
        System.out.println("My IP is: " + IP);
        System.out.println("My PORT is: " + PORT);
    }


    @Override
    public String toString() {
        return "Broker{ IP='" + IP + '\'' + ", port=" + PORT + ", hashnumber=" + hashnumber + '}';
    }

    @Override
    public int compareTo(Broker o) {
        return Long.compare(this.hashnumber, o.hashnumber);
    }

    public void createMainSocket() throws IOException {
        System.out.println("Opening main socket at port: " + PORT);
        mainSocket = new ServerSocket(PORT);

    }

    public void destroyServerSocket() throws IOException {
        System.out.println("Closing main socket at port: " + PORT);
        mainSocket.close();
    }

    public void waitForClient() throws IOException {
        System.out.println("Waiting for a client ... ");
        Socket conversation = mainSocket.accept();

        System.out.println("A client has connected  ... ");

        ObjectInputStream in = new ObjectInputStream(conversation.getInputStream());
        ObjectOutputStream out = new ObjectOutputStream(conversation.getOutputStream());

        System.out.println("Sending initial token");
        out.writeUTF("Who are you?");
        out.flush();

        String client = in.readUTF();

        if (client.equals("publisher")) {
            Runnable runnable = () -> {
                acceptConnectionForPublisher(in, out);
            };
            Thread pubThread = new Thread(runnable);  //δημιουργία thread
            pubThread.start();
        }

        if (client.equals("subscriber")) {
            Runnable runnable = () -> {
                acceptConnectionForSubscriber(in, out);
            };
            Thread subThread = new Thread(runnable);
            subThread.start();
        }
    }

    public void acceptConnectionForPublisher(ObjectInputStream in, ObjectOutputStream out) {
        System.out.println("Thread created to service a publisher");

        try {
            out.writeUTF("What do you want?");
            out.flush();

            String order = in.readUTF(); // brokelist or songlist

            if (order.equalsIgnoreCase("brokerlist")) {  //αποσύνθεση
                // 3,IP,IP, IP, PORT,PORT,PORT , HASH . HASH , HASH
                int length = brokers.size();
                String s = String.valueOf(length); // "3"

                for (int i = 0; i < length; i++) {
                    s = s + "," + brokers.get(i).IP;
                }

                for (int i = 0; i < length; i++) {
                    s = s + "," + brokers.get(i).PORT;
                }

                for (int i = 0; i < length; i++) {
                    s = s + "," + brokers.get(i).hashnumber;
                }

                out.writeUTF(s); // στέλνει την συμβολοσειρά  3,IP,IP, IP, PORT,PORT,PORT , HASH . HASH , HASH
                out.flush();

                out.close();
                in.close();
            }

            if (order.equalsIgnoreCase("songlist")) {
                out.writeUTF("your id?");
                out.flush();

                String s = in.readUTF();
                int id = Integer.parseInt(s); // δεχεται  αν είναι ο pub 1 or 2
                int index = id - 1;

                String publisher_IP = in.readUTF(); //δεχεται το ip
                int publisher_port = Integer.parseInt(in.readUTF()); //δεχεται το port

                System.out.println("Received list from publisher #" + id +
                        ", ip is " + publisher_IP + " and port is " + publisher_port);

                s = in.readUTF(); //δεχεται το string με τα metadata

                String[] words = s.split(",");

                int length = Integer.parseInt(words[0]);

                // 0,(1,2,3,4),(5,6,7,8),(9,10,11,12),13,14, ....
                // ID, a, a, g, t, a, a, g, t
                for (int i = 0; i < length; i++) { //συνθεση
                    MusicFile mf = new MusicFile();
                    mf.albumInfo = words[1 + i * 4];
                    mf.artistName = words[2 + i * 4];
                    mf.genre = words[3 + i * 4];
                    mf.trackName = words[4 + i * 4];

                    String key = mf.artistName + "_" + mf.albumInfo + "_" + mf.trackName;
                    Long songhashvalue = hash(key);

                    mf.hash = songhashvalue;
                    System.out.println("received..." + mf.artistName + ", " + mf.albumInfo + ", " + mf.trackName);
                    synchronized(arrayOfLists) {
                        arrayOfLists[index].add(mf);
                    }
                }

                System.out.println("Success");

                Publisher publisher = new Publisher();
                publisher.setIp(publisher_IP);
                publisher.setPort(publisher_port);
                publisher.setId(index + 1);

                boolean exists = false;
                synchronized(registeredPublisher) {
                    for (int i = 0; i < registeredPublisher.size(); i++) {
                        if (registeredPublisher.get(i).getId() == id) {
                            exists = true;
                        }
                    }

                    if (!exists) {
                        registeredPublisher.add(publisher);

                        System.out.println("A new publisher has registered.");
                        System.out.println("total publishers known: " + registeredPublisher.size());

                        for (int i = 0; i < registeredPublisher.size(); i++) {
                            System.out.println("ID: " + registeredPublisher.get(i).getId() +
                                    " " + registeredPublisher.get(i).getIp() + " " +
                                    registeredPublisher.get(i).getPort());
                        }
                        System.out.println("------------------------------");
                    } else {
                        System.out.println("A publisher has updated its list.");
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void acceptConnectionForSubscriber(ObjectInputStream in, ObjectOutputStream out) {
        System.out.println("Thread created to service a subscriber");

        try {
            out.writeUTF("What do you want?");
            out.flush();

            String order = in.readUTF();

            if (order.equalsIgnoreCase("brokerlist")) {
                // 3,IP,IP, IP, PORT,PORT,PORT
                int length = brokers.size();
                String s = String.valueOf(length); // "3"

                for (int i = 0; i < length; i++) {
                    s = s + "," + brokers.get(i).IP;
                }

                for (int i = 0; i < length; i++) {
                    s = s + "," + brokers.get(i).PORT;
                }

                for (int i = 0; i < length; i++) {
                    s = s + "," + brokers.get(i).hashnumber;
                }


                out.writeUTF(s);
                out.flush();

                out.close();
                in.close();
            }

            if (order.equalsIgnoreCase("songlist")) {
                out.writeUTF("your id?");
                out.flush();

                String s = in.readUTF();
                int id = Integer.parseInt(s);
                int index = id - 1;

                synchronized(arrayOfLists) {
                    int length = arrayOfLists[0].size() + arrayOfLists[1].size();
                    s = String.valueOf(length); // "3"

                    for (int j = 0; j < arrayOfLists[0].size(); j++) {
                        MusicFile musicFile = arrayOfLists[0].get(j);

                        s = s + "," + musicFile.albumInfo;
                        s = s + "," + musicFile.artistName;
                        s = s + "," + musicFile.genre;
                        s = s + "," + musicFile.trackName;
                    }

                    for (int j = 0; j < arrayOfLists[1].size(); j++) {
                        MusicFile musicFile = arrayOfLists[1].get(j);

                        s = s + "," + musicFile.albumInfo;
                        s = s + "," + musicFile.artistName;
                        s = s + "," + musicFile.genre;
                        s = s + "," + musicFile.trackName;

                    }
                }

                System.out.println("Sending to subscriber #" + index);
                System.out.println(s);
                out.writeUTF(s);
                out.flush();

                out.close();
                in.close();
            }
            if (order.equalsIgnoreCase("song")) {
                String s = in.readUTF();
                int sub_id = Integer.parseInt(s);

                System.out.println("Song request received from subscriber #" + sub_id);

                Long songhashvalue = in.readLong();

                System.out.println("song hash value is " + songhashvalue);

                // put order to queue
                orderQueue.put(songhashvalue);

                // create downloader thread
                System.out.println("Starting downloader thread: ");
                DownloaderThread downloader = new DownloaderThread(sub_id, registeredPublisher, orderQueue, arrayOfLists);
                downloader.start();
                downloader.join(); //waiting ...

                System.out.println("Sending song back to subscriber");

                MusicFile desiredSong = null;

                synchronized(arrayOfLists) {
                    for (int i = 0; i < arrayOfLists.length; i++) {
                        for (MusicFile mf : arrayOfLists[i]) {
                            if (mf.hash.equals(songhashvalue)) {
                                desiredSong = mf;
                                break;
                            }
                        }
                    }
                }

                if (desiredSong == null || desiredSong.musicFileExtract == null) {
                    System.out.println("unknown song");
                    out.writeUTF("error");
                } else {
                    sendSongToSubscriber(desiredSong, out);
                }
                out.flush();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void sendSongToSubscriber(MusicFile mf, ObjectOutputStream outsubscriber) throws IOException {
        System.out.println("song found by the broker. incoming ...");

        Parameters params = new Parameters();

        outsubscriber.writeUTF("ok");
        outsubscriber.flush();

        long remainingsize = mf.musicFileExtract.length;

        System.out.println("remaining size received by publisher is: " + remainingsize);
        outsubscriber.writeLong(remainingsize);
        outsubscriber.flush();

        System.out.println("Sending file to subscriber");

        int loop = 0;
        int n = 0;

        while (remainingsize > 0) {
            if (remainingsize >= params.CHUNK_SIZE) {
                int success_bytes = params.CHUNK_SIZE;

                remainingsize = remainingsize - success_bytes;

                outsubscriber.write(mf.musicFileExtract, n, success_bytes);
                outsubscriber.flush();

                n += success_bytes;
            } else {
                int success_bytes = (int) remainingsize;

                remainingsize -= success_bytes;

                outsubscriber.write(mf.musicFileExtract, n, success_bytes);
                outsubscriber.flush();
                n += success_bytes;
            }

            loop++;
        }

        System.out.println("loops:" + loop + " total bytes sent: " + n);
        System.out.println("Success");
    }
}

