package utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class SongLoader {
        public byte[] load(File file){
            long songlength = file.length();
            byte[] buffer = new byte[(int)songlength];
            try {
                InputStream ios = new FileInputStream(file);
                ios.read(buffer);
                ios.close();
            } catch (Exception ex){
                ex.printStackTrace();
                System.exit(1);
            }
            return buffer;
        }
    }
