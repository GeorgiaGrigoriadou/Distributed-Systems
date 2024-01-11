package skeleton;

import brokerinput.BrokerInfo;
import brokerinput.BrokerInputLoader;

import java.util.ArrayList;
import java.util.List;

public class Broker extends Node implements Comparable<Broker>  {
    public String IP;
    public int PORT;
    public long hashnumber;

    public void init() { // read broker list
        BrokerInputLoader loader = new BrokerInputLoader();

        try {
            BrokerInfo[] infoList = loader.loadBrokers();

            for (BrokerInfo info : infoList) {
                Broker b = new Broker();
                b.IP = info.ip;
                b.PORT = info.port;
                brokers.add(b);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }

        calculateKeys();

        sortBrokers();

        for (Broker b : brokers) {
            System.out.println(b);
        }
    }

    @Override
    public String toString() {
        return "Broker{ IP='" + IP + '\'' + ", PORT=" + PORT + ", hashnumber=" + hashnumber + '}';
    }

    @Override
    public int compareTo(Broker o) {
        return Long.compare(this.hashnumber, o.hashnumber);
    }
}
