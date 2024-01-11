package brokerinput;

import parameters.Parameters;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class BrokerInputLoader {
    private Parameters params = new Parameters();

    public BrokerInfo loadDefaultBroker() throws IOException {
        BrokerInfo info = new BrokerInfo();
        File file = new File(params.BROKER_FILE_PATH);
        BufferedReader br = new BufferedReader(new FileReader(file));
        String temp;
        if ((temp = br.readLine()) != null){
          String[] line = temp.split(":");
            info.ip =line[0];
            info.port = Integer.parseInt(line[1]);
        }
        br.close();
        return info;
    }

    public BrokerInfo [] loadBrokers() throws IOException {

        File file = new File(params.BROKER_LIST_FILE_PATH);
        BufferedReader br = new BufferedReader(new FileReader(file));
        String temp;

        String firstline = br.readLine();
        int brokerCount = Integer.parseInt(firstline);
        BrokerInfo [] brokers = new BrokerInfo[brokerCount];

        for (int i=0;i<brokerCount;i++) {
            temp = br.readLine();
            if (temp == null) {
                System.out.println("missing brokers from broker file");
                System.exit(1);
            }
            String[] line = temp.split(":");
            BrokerInfo info = new BrokerInfo();
            info.ip =line[0];
            info.port = Integer.parseInt(line[1]);
            brokers[i] = info;
        }
        br.close();
        return brokers;
    }
}

