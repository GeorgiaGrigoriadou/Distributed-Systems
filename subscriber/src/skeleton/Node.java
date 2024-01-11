package skeleton;

import javax.xml.bind.DatatypeConverter;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Node {
    public List<Broker> brokers = new ArrayList<Broker>();

    protected void sortBrokers() {
        Collections.sort(brokers);
    }
    public void init() {

    }

    public void calculateKeys() {
        for (Broker k : brokers) {
            k.hashnumber = hashBroker(k);
        }
    }

    public long hash(String s) {
            try {
                MessageDigest md = MessageDigest.getInstance("MD5");
                md.update(s.getBytes());
                byte[] digest = md.digest();
                String myHash = DatatypeConverter.printHexBinary(digest).toUpperCase();
                BigInteger bi = new BigInteger(myHash, 16);
                long hashvalue = Math.abs(bi.longValue());

                return hashvalue;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return 0;
    }

    public long hashBroker(Broker k) {
        return hash(k.IP +":" + k.PORT);
    }

    public int findBroker(List<Broker> brokers, String artistName) {
        long artisthash = hash(artistName);

        if (artisthash < brokers.get(0).hashnumber) {
            long m = artisthash % brokers.size();
            return (int)m;
        }

        for (int i=1;i<brokers.size();i++) {
            if (artisthash < brokers.get(i).hashnumber) {
                return (int)(i-1);
            }
        }

        return (int) (brokers.size() -1);
    }
}
