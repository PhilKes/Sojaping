package common;

public class Constants {
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
                LOGIN_SUCCESS="loginSuccess",
                LOGOFF="logoff",
                INFO="info",
                MESSAGE_SENT="messageSent",
                MESSAGE_RECEIVED="messageRec",
                MESSAGE="message",
                BRDCAST_MSG="broadcastMsg",
                BRDCAST_USERS="broadcastUsers",
                USERLIST="userlist",
                SHUTDOWN="shutdown";
    }
}
