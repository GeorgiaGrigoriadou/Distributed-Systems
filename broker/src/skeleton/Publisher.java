package skeleton;

import brokerinput.BrokerInfo;
import data.MusicFile;
import data.Value;
import parameters.Parameters;
import utils.SongLoader;

import java.util.ArrayList;
import java.util.List;

public class Publisher extends Node  {
    private BrokerInfo defaultBrokerInfo;
    private Parameters parameters = new Parameters();
    private SongLoader songLoader = new SongLoader();
    private int id;
    private String ip;
    private int port;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public BrokerInfo getDefaultBrokerInfo() {
        return defaultBrokerInfo;
    }

    public Parameters getParameters() {
        return parameters;
    }

    public SongLoader getSongLoader() {
        return songLoader;
    }
}
