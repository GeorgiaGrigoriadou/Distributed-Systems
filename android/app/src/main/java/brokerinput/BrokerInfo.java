package brokerinput;

public class BrokerInfo {
    public String ip;
    public int port;

    @Override
    public String toString() {
        return "BrokerInfo{" +
                "ip='" + ip + '\'' +
                ", PORT=" + port +
                '}';
    }
}
