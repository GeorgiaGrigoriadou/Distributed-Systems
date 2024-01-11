package skeleton;

import data.MusicFile;
import parameters.Parameters;
import skeleton.Publisher;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

public class DownloaderThread extends Thread {
    private int subid;
    public List<Publisher> registeredPublisher;
    public LinkedBlockingQueue<Long> orderQueue;
    public List<MusicFile>[] arrayOfLists;

    public DownloaderThread(int subid, List<Publisher> registeredPublisher, LinkedBlockingQueue<Long> orderQueue, List<MusicFile>[] arrayOfLists) {
        this.subid = subid;
        this.registeredPublisher = registeredPublisher;
        this.orderQueue = orderQueue;
        this.arrayOfLists = arrayOfLists;

        System.out.println("Downloaded thread started ");
    }

    public void run() {
        try {
            System.out.println("Downloader consumes from queue ");

            Long songhashvalue = orderQueue.take();

            System.out.println("Downloader consumed from queue: " + songhashvalue);

            // find publisher ...
            MusicFile desiredSong = null;
            int publisher_index = -1;

            synchronized(arrayOfLists) {
                for (int i = 0; i < arrayOfLists.length; i++) {
                    for (MusicFile mf : arrayOfLists[i]) {
                        if (mf.hash.equals(songhashvalue)) {
                            desiredSong = mf;
                            publisher_index = i;
                            break;
                        }
                    }
                }
            }
            int publisher_id = publisher_index + 1;

            if (desiredSong.musicFileExtract != null) {
                System.out.println("Downloader: already exists hash: " + songhashvalue);
                return;
            }

            if (desiredSong != null) {
                synchronized (registeredPublisher){
                    for (Publisher p : registeredPublisher) {
                        if (p.getId() == publisher_id) {
                            System.out.println("Downloader: downloading song: " + songhashvalue);
                            getSongFromPublisher(songhashvalue, desiredSong, p);
                            System.out.println("Downloader: Done: " + songhashvalue);
                            return;
                        }
                    }
                }
            }

            System.out.println("Downloader: Error, no publisher found for song: " + songhashvalue);
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

    private void getSongFromPublisher(Long songhashvalue, MusicFile mf, Publisher p) throws IOException {
        System.out.println("connecting to publisher " + p.getIp() + " : " + p.getPort());
        Socket conversation = new Socket(p.getIp(), p.getPort());

        ObjectOutputStream out = new ObjectOutputStream(conversation.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(conversation.getInputStream());

        System.out.println("waiting for publisher to send initial token");
        String s = in.readUTF();

        out.writeUTF("broker");
        out.flush();

        s = in.readUTF(); // what do you want to do?

        out.writeUTF("song");
        out.flush();

        s = in.readUTF(); // what is your ID?

        out.writeInt(subid);
        out.flush();

        System.out.println("Sending song request to broker #" + subid);

        System.out.println("song hash value is " + songhashvalue);

        out.writeLong(songhashvalue);
        out.flush();

        s = in.readUTF();

        if (s.equalsIgnoreCase("error")) {
            System.out.println("song not found by the broker");
        } else {
            System.out.println("song found by the broker. incoming ...");

            Parameters params = new Parameters();

            long remainingsize = in.readLong();

            System.out.println("remaining size received by publisher is: " + remainingsize);

            System.out.println("Sending file to subscriber");

            mf.musicFileExtract = null;

            int loop = 0;
            int n = 0;
            while (remainingsize > 0) {
                if (remainingsize >= params.CHUNK_SIZE) {
                    byte[] buffer = new byte[params.CHUNK_SIZE];
                    int success_bytes = in.read(buffer, 0, params.CHUNK_SIZE);

                    remainingsize = remainingsize - success_bytes;

                    n += success_bytes;

                    if (mf.musicFileExtract == null) {
                        mf.musicFileExtract = buffer;
                    } else {
                        byte[] c = new byte[mf.musicFileExtract.length + success_bytes];
                        System.arraycopy(mf.musicFileExtract, 0, c, 0, mf.musicFileExtract.length);
                        System.arraycopy(buffer, 0, c, mf.musicFileExtract.length, success_bytes);
                        mf.musicFileExtract = c;
                    }
                } else {
                    byte[] buffer = new byte[(int) remainingsize];
                    int success_bytes = in.read(buffer, 0, (int) remainingsize);

                    remainingsize -= success_bytes;

                    n += success_bytes;

                    if (mf.musicFileExtract == null) {
                        mf.musicFileExtract = buffer;
                    } else {
                        byte[] c = new byte[mf.musicFileExtract.length + success_bytes];
                        System.arraycopy(mf.musicFileExtract, 0, c, 0, mf.musicFileExtract.length);
                        System.arraycopy(buffer, 0, c, mf.musicFileExtract.length, success_bytes);
                        mf.musicFileExtract = c;
                    }
                }

                loop++;
            }
            System.out.println("Success");
            conversation.close();
        }
    }
}