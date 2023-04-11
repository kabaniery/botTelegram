package org.session;


import org.db.DataBase;

public class UserSession {
    public enum status {
        USER,
        ADMIN;
    }
    public enum userTitles {
        GETBOOK,
        GETTITLES;
    }
    public enum commands {
        FORMATBOOKS,
        ADDNEWBOOK,
        GETBOOK,
        ADDNEWADMIN,
        DELETEADMIN,
        EMPTY;
    }
    Long id;
    private commands lastCommand;
    public String[] currentArguments = null;
    private boolean admin = false;
    int idInList;
    public void tryAdmin() {
        this.admin = DataBase.isAdmin(id);
    }
    public UserSession(Long id, String command) {
        this.id = id;
        switch (command) {
            case "Choose a book\uD83D\uDCD6" -> {
                this.lastCommand = commands.GETBOOK;
            }
            case "/admin" -> tryAdmin();
            default -> this.lastCommand = commands.EMPTY;
        }
    }
    public boolean isAdmin() {
        return this.admin;
    }
    public void trySetNewCommand(String command) {
        switch (command) {
            case "/addNewBook" -> {
                if (this.admin) {
                    this.lastCommand = commands.ADDNEWBOOK;
                } else {
                    this.lastCommand = commands.EMPTY;
                }
            }
            case "/formatBooks" -> {
                if (this.admin) {
                    this.lastCommand = commands.FORMATBOOKS;
                } else
                    this.lastCommand = commands.EMPTY;
            }
            case "/addAdmin" -> {
                if (this.admin)
                    this.lastCommand = commands.ADDNEWADMIN;
                else
                    this.lastCommand = commands.EMPTY;
            }
            case "/deleteAdmin" -> {
                if (this.admin)
                    this.lastCommand = commands.DELETEADMIN;
                else
                    this.lastCommand = commands.EMPTY;
            }
            case "Choose a book\uD83D\uDCD6" -> {
                this.lastCommand = commands.GETBOOK;
            }
            default -> this.lastCommand = commands.EMPTY;
        }
    }

    public commands getLastCommand() {
        return lastCommand;
    }
}
