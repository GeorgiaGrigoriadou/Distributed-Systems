package filikietaireia;

import skeleton.Publisher;

import java.io.IOException;

public class MainPublisher {

    public static void main(String[] args) {
        System.out.println("=================== PUBLISHER ===================");

        Publisher publisher = new Publisher();

        publisher.init();

        try {
            publisher.getBrokerList();

            publisher.pushAllSongs();

            publisher.createMainSocket();

            while (publisher.open == true){
                publisher.waitForClient();
            }

            publisher.destroyServerSocket();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
