package data;

import java.util.Arrays;

public class MusicFile {
    public  String trackName;
    public  String artistName;
    public String genre;
    public String albumInfo;
    public byte[] musicFileExtract;

    @Override
    public String toString() {
        return "MusicFile{" +
                "trackName='" + trackName + '\'' +
                ", artistName='" + artistName + '\'' +
                ", genre='" + genre + '\'' +
                ", albumInfo='" + albumInfo + "\'}";
    }


}
