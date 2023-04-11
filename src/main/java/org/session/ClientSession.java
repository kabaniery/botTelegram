package org.session;

public class ClientSession extends UserSession {
    public enum titles {
        GETBOOK,
        GETTITLES;
    }
    public ClientSession(Long id, String command) {
       super(id, command);
    }

}
