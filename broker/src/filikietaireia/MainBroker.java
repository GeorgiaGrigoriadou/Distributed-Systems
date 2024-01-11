package filikietaireia;

import skeleton.Broker;

import java.io.IOException;

public class MainBroker {

    public static void main(String[] args) {
        int broker_flag =2; //DEFAULT 1, flags from 0 to 2

        System.out.println("=================== BROKER  " + broker_flag + " ===================");

        Broker broker = new Broker(broker_flag);

        broker.init();

        broker.calculateKeys();

        try {
            broker.createMainSocket();

            while (broker.open == true){
                broker.waitForClient();
            }

            broker.destroyServerSocket();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
