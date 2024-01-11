package filikietaireia;

import skeleton.Subscriber;

import java.io.IOException;
import java.util.Scanner;

public class MainSubscriber {

    public static void main(String[] args) {
        System.out.println("=================== SUBSCRIBER ===================");

        Subscriber subscriber = new Subscriber();

        subscriber.init();

        try {
            subscriber.getBrokerList(); // register

            subscriber.getSongList();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Scanner scanner = new Scanner(System.in);
        int mode;

        do {
            System.out.print("Select mode (0:streaming, 1:offline): ");
            mode = scanner.nextInt();

            switch (mode) {
                case 0:
                    System.out.println("You are working in streaming mode ");
                    break;
                case 1:
                    System.out.println("You are working in offline mode ");
                    break;
                default:
                    System.out.println("invalid choice");
            }
        } while (mode != 0 && mode != 1);

        // print all songs
        subscriber.printSongList();

        do {
            int song;
            System.out.println("Select the song you want to download (0 for exit):") ;
            song = scanner.nextInt();
            if (song == 0) {
                break;
            } else {
                subscriber.downloadSong(song);
            }
        } while(true);
    }
}
