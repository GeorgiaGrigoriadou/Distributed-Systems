package utils;

import parameters.Parameters;

import java.io.*;

public class SongLoader {
        public byte[] load(File file){
            long songlength = file.length();
            byte[] buffer = new byte[(int)songlength];

            try {
                InputStream ios = new FileInputStream(file);
                int n = ios.read(buffer);
                System.out.println(n);
                ios.close();

                if (n != songlength) {
                    System.out.println("partial read !!") ;
                    System.exit(1);
                }
            } catch (Exception ex){
                ex.printStackTrace();
                System.exit(1);
            }

            return buffer;
        }
    }
