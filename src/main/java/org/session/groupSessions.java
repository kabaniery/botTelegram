package org.session;

import java.util.HashMap;
import java.util.HashSet;

public class groupSessions {
    HashMap<Long, chatSession> session;

    public groupSessions() {
        this.session = new HashMap<>();
    }
    public void addSession(chatSession session) {
        this.session.put(session.id, session);
    }
    public chatSession getChat(Long id) {
        return this.session.get(id);
    }
}
