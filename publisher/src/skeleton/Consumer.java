package skeleton;

import brokerinput.BrokerInfo;
import brokerinput.BrokerInputLoader;
import data.Value;

public class Consumer extends Node {
    private BrokerInfo defaultBrokerInfo;

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
}
