package common;

import java.util.HashMap;
import java.util.Map;

public class Constants {
    public static String SERVER_HOST="192.168.178.26";
    public static int SERVER_PORT = 9999;//443;c

    public final static int CLIENT_LOADING_STEPS=6;

    public static class Translation {
        public final static String ENGLISH_VALUE = "en",
                ENGLISH_KEY = "English";
        /**
         * All Options for languagesc
         */
        public final static Map<String, String> languages;

        /** Fill languages Map*/
        static {
            languages=new HashMap<>();
            languages.put("Afrikaans", "af");
            languages.put("Arabic", "ar");
            languages.put("Azerbaijani", "az");
            languages.put("Bashkir", "ba");
            languages.put("Belarusian", "be");
            languages.put("Bulgarian", "bg");
            languages.put("Bengali", "bn");
            languages.put("Catalan", "ca");
            languages.put("Czech", "cs");
            languages.put("Chuvash", "cv");
            languages.put("Danish", "da");
            languages.put("German", "de");
            languages.put("Greek", "el");
            languages.put(ENGLISH_KEY, ENGLISH_VALUE);
            languages.put("Esperanto", "eo");
            languages.put("Spanish", "es");
            languages.put("Estonian", "et");
            languages.put("Basque", "eu");
            languages.put("Persian", "fa");
            languages.put("Finnish", "fi");
            languages.put("French", "fr");
            languages.put("Irish", "ga");
            languages.put("Gujarati", "gu");
            languages.put("Hebrew", "he");
            languages.put("Hindi", "hi");
            languages.put("Croatian", "hr");
            languages.put("Haitian", "ht");
            languages.put("Hungarian", "hu");
            languages.put("Armenian", "hy");
            languages.put("Icelandic", "is");
            languages.put("Italian", "it");
            languages.put("Japanese", "ja");
            languages.put("Georgian", "ka");
            languages.put("Kazakh", "kk");
            languages.put("Central Khmer", "km");
            languages.put("Korean", "ko");
            languages.put("Kurdish", "ku");
            languages.put("Kirghiz", "ky");
            languages.put("Lithuanian", "lt");
            languages.put("Latvian", "lv");
            languages.put("Malayalam", "ml");
            languages.put("Mongolian", "mn");
            languages.put("Malay", "ms");
            languages.put("Maltese", "mt");
            languages.put("Norwegian Bokmal", "nb");
            languages.put("Dutch", "nl");
            languages.put("Norwegian Nynorsk", "nn");
            languages.put("Panjabi", "pa");
            languages.put("Polish", "pl");
            languages.put("Pushto", "ps");
            languages.put("Portuguese", "pt");
            languages.put("Romanian", "ro");
            languages.put("Russian", "ru");
            languages.put("Slovakian", "sk");
            languages.put("Slovenian", "sl");
            languages.put("Somali", "so");
            languages.put("Albanian", "sq");
            languages.put("Serbian", "sr");
            languages.put("Swedish", "sv");
            languages.put("Tamil", "ta");
            languages.put("Telugu", "te");
            languages.put("Thai", "th");
            languages.put("Turkish", "tr");
            languages.put("Ukrainian", "uk");
            languages.put("Urdu", "ur");
            languages.put("Vietnamese", "vi");
            languages.put("Simplified Chinese", "zh");
            languages.put("Traditional Chinese", "zh-TW");
        }

        public static Map<String, String> getSupportedLanguages() {
            Map<String, String> map=new HashMap<>();
            map.put("German", "de");
            map.put("English", "en");
            map.put("Spanish", "es");
            map.put("Italian", "it");
            map.put("Japanese", "ja");
            map.put("French", "fr");
            map.put("Korean", "ko");
            map.put("Dutch", "nl");
            map.put("Russian", "ru");
            map.put("Polish", "pl");
            map.put("Portuguese", "pt");
            map.put("Swedish", "sv");
            map.put("Simplified Chinese", "zh");
            return map;
        }
    }

    public static class Json {
        public static final String
                PRIMITIVE_FIELD="primitive",
                CLASS_FIELD="_class",
                LIST_CLASS="_list",
                DATA_FIELD="data",
                METHOD_FIELD="method";
        public static final String SEPERATOR=":";
    }

    public static class Contexts {
        public static final String
                FAIL="fail",
                CONNECT="connect",
                CONNECT_SUCCESS="connectSuccess",
                REGISTER="register",
                REGISTER_SUCCESS="registerSuccess",
                LOGIN="login",
                LOGIN_SUCCESS = "loginSuccess",
                LOGOFF = "logoff",
                INFO = "info",
                MESSAGE_SENT = "messageSent",
                MESSAGE_RECEIVED = "messageRec",
                MESSAGE_FETCH = "messageFetch",
                MESSAGE = "message",
                BRDCAST_MSG = "broadcastMsg",
                BRDCAST_USERS = "broadcastUsers",
                USERLIST = "userlist",
                USERLIST_SINGLE="userlistSingle",
                GROUPLIST = "grouplist",
                GROUP_ADD = "groupAdd",
                SHUTDOWN = "shutdown",
                BROADCAST = "broadcast",
                PROFILE_UPDATE = "profileUpdate",
                DELETE_ACCOUNT = "deleteAccount",
                ADD_FRIEND = "addFriend",
                REMOVE_FRIEND="removeFriend",
                BLOCK = "block",
                UNBLOCK = "unblock",
                FRIEND_LIST = "friendList",
                GROUP_UPDATE = "groupUpdate",
                INVITATION_EMAIL = "inviteEmail";
    }

    public static class Windows {
        public static final String
                LOGIN="login",
                REGISTER="register",
                USER_PROFILE="UserProfile",
                PUBLIC_PROFILE="PublicProfile",
                GUI="gui";
    }
}
