package common;

import common.data.Packet;
import server.Connection;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.List;

import static common.Constants.Contexts.FAIL;

/**
 * Utility class
 */
public class Util {

    /**
     * Log sent/received Packet in Console
     * from: true(received packet), from: false(sent packet)
     */
    public static void logPacket(boolean from, String nickname, Packet packet) {
        StringBuilder stringBuilder=new StringBuilder();
        stringBuilder.append(from ? "from " : "to   ").append(nickname).append("\t: ").append(packet);
        if(packet.getContext().contains(FAIL)) {
            System.err.println(stringBuilder.toString());
        }
        else {
            System.out.println(stringBuilder.toString());
        }
    }

    public static void logPacket(boolean from, Connection con, Packet packet) {
        logPacket(from, (con.isLoggedIn() ? ("(" + con.getLoggedAccount().getUserName() + ")") : "\t\t") + con.getNickname(), packet);
    }

    public static List<String> joinedStringToList(String str){
        return Arrays.asList(str.split(","));
    }

    /** Check if two InetAdresses are in same network */
    /** Source: https://stackoverflow.com/questions/8555847/test-with-java-if-two-ips-are-in-the-same-network*/
    private static boolean sameNetwork(final byte[] x, final byte[] y, final int mask) {
        if(x == y) return true;
        if(x == null || y == null) return false;
        if(x.length != y.length) return false;
        final int bits  = mask &   7;
        final int bytes = mask >>> 3;
        for(int i=0;i<bytes;i++)  if(x[i] != y[i]) return false;
        final int shift = 8 - bits;
        if(bits != 0 && x[bytes]>>>shift != y[bytes]>>>shift) return false;
        return true;
    }
    public static boolean sameNetwork(final InetAddress a, final InetAddress b, final int mask) {
        return sameNetwork(a.getAddress(), b.getAddress(), mask);
    }
}
