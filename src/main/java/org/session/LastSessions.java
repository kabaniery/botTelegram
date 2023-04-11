package org.session;

import java.util.ArrayList;
import java.util.List;

public class LastSessions {
    private static int maxCount;

    private static List<Long> lastActiveUsers = new ArrayList<>();
    private static List<UserSession> users = new ArrayList<>();
    private static int lastItem = 0;


    public LastSessions(int count) {
        maxCount = count;
    }
    public void addNewUserSession(UserSession user) {
        if (lastItem != 0) {
            int index = lastActiveUsers.indexOf(user.id);
            if (index == -1) {
                addUserSessionToList(user);
            } else {
                removeUserSessionFromList(index);
                addUserSessionToList(user);
            }
        } else {
            addUserSessionToList(user);
        }
    }

    private void addUserSessionToList(UserSession user) {
        if (lastItem >= maxCount) {
            removeUserSessionFromList(0);
        }
        lastItem++;
        lastActiveUsers.add(user.id);
        users.add(user);
    }
    private void removeUserSessionFromList(int index) {
        users.remove(index);
        lastActiveUsers.remove(index);
        lastItem--;
    }
    public UserSession getUserSession(Long id) {
        if (lastItem != 0) {
            int index = lastActiveUsers.indexOf(id);
            if (index != -1) {
                return users.get(index);
            }
        }
        return null;
    }

    public void removeUserSession(Long id) {
        if (lastItem != 0) {
            int index = lastActiveUsers.indexOf(id);
            if (index != -1) {
                removeUserSessionFromList(index);
            }
        }
    }
}
