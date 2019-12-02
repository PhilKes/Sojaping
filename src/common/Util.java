package common;

import client.presentation.GUIController;
import common.data.Packet;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.MenuButton;
import javafx.scene.image.Image;
import org.apache.commons.lang3.mutable.MutableInt;
import server.Connection;
import server.TranslationService;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static common.Constants.Contexts.FAIL;

/**
 * Utility class
 */
public class Util {

    private static final Image DEFAULT_AVATAR=new Image(GUIController.class.getResourceAsStream("resources/default_avatar_min.png"));
    private static final Image DEFAULT_ICON = new Image(GUIController.class.getResourceAsStream("resources/icon.png"));
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

    public static Image getDefaultAvatar() {
        return DEFAULT_AVATAR;
    }

    public static Image getDefaultIcon() {
        return DEFAULT_ICON;
    }

    public static void fillLanguageMenu(final MenuButton menuLanguages, List<String> selectedLanguages, MutableInt languageCounter) {
        final List<CheckMenuItem> items = TranslationService.getSupportedLanguages().keySet()
                .stream().sorted().map(CheckMenuItem::new).collect(Collectors.toList());
        menuLanguages.getItems().addAll(items);

        /** Add languages to selectedLangauges and highlight in item List*/
        for (final CheckMenuItem item : items) {
            item.selectedProperty().addListener((observableValue, oldValue, newValue) -> {
                if (newValue) {
                    /** Add language */
                    selectedLanguages.add(item.getText());
                    languageCounter.increment();
                    item.setText(languageCounter.getValue() + "." + item.getText());
                    menuLanguages.setStyle(""); //TODO Styling via .css
                } else {
                    /** Remove language*/
                    String t = item.getText();
                    String[] s = t.split("\\.");
                    item.setText(s[1]);
                    selectedLanguages.remove(s[1]);
                    languageCounter.decrement();
                    /** Update selected numbers*/
                    for (int i = 0; i < languageCounter.getValue(); i++) {
                        String lang = selectedLanguages.get(i);
                        for (CheckMenuItem menuItem : items.stream().filter(CheckMenuItem::isSelected)
                                .collect(Collectors.toList())) {
                            if (menuItem.getText().split("\\.")[1].equals(lang)) {
                                menuItem.setText((i + 1) + "." + lang);
                                break;
                            }
                        }
                    }
                    if (selectedLanguages.isEmpty()) {
                        //TODO Styling via .css
                        menuLanguages.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
                    }
                }
                menuLanguages.setText(String.join(",", selectedLanguages));
            });
        }
        /** Select English as default language*/
        for (CheckMenuItem item : items) {
            if (item.getText().equals(TranslationService.ENGLISH_KEY)) {
                item.setSelected(true);
                break;
            }
        }

    }
}
