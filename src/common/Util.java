package common;

import common.data.Packet;
import server.Connection;

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
}
