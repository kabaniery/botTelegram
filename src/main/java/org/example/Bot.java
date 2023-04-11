package org.example;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.db.DataBase;
import org.db.HtmlRequest;
import org.session.LastSessions;
import org.session.UserSession;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.*;

import javax.validation.constraints.Negative;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Bot extends TelegramLongPollingBot {
    //Count of last Active Session
    private final int maxUsersCount = 1000;
    private String name;
    private int usersCount = 0;
    public boolean logMode = false;
    private boolean limMode = false;
    //Session Properties
    private LastSessions session;
    private DataBase.Draw draw;


    Bot(String name) {
        this.session = new LastSessions(maxUsersCount);
        Path of = Path.of(name);
        if (!Files.isRegularFile(of)) {
            try {
                Files.createFile(of);
            } catch (IOException e) {
                System.out.println("Can't create file. System will working on limited mode");
                this.limMode = true;
            }
        }
        this.name = name;
    }

    @Override
    public String getBotUsername() {
        return "kabanieryBotExample_bot";
    }

    @Override
    public String getBotToken() {
        return "6014408564:AAGzkX8MgIZJbj2L8hH8j0YIo9p7LlB9jkI";
    }

    @Override
    public void onUpdateReceived(Update update) {
        Message msg = update.getMessage();
        User usr = msg.getFrom();
        Long id = usr.getId();
        String txt = msg.getText();

        DataBase base = new DataBase();

        if (draw.getActive()) {
            draw.addNewUser(id, usr.getUserName());
        }

        if (msg.getText() == null) {
            UserSession user = this.session.getUserSession(id);
            switch (user.getLastCommand()) {
                case ADDNEWBOOK -> {
                    String path = HtmlRequest.getFile(getBotToken(), msg.getDocument().getFileId());
                    if (path != null) {
                        try {
                            CSVReader reader = new CSVReader(new FileReader(path));
                            String[] line;
                            while ((line = reader.readNext()) != null) {
                                DataBase.Books.setNewBook(user, line);
                            }
                        } catch (CsvValidationException | IOException e) {
                            System.out.println("Error while updating file");
                            System.out.println(e.getMessage());
                        }
                        try {
                            Files.delete(Path.of(path));
                        } catch (IOException e) {}
                    }
                }
            }
            return;
        }
        if (txt.equals("/logmode") && new UserSession(id, "/admin").isAdmin()) {
            logMode = !logMode;
        }
        if (logMode) {
            System.out.println(id.toString() + ": " + txt);
        }
        switch (txt) {
            case "Choose a book\uD83D\uDCD6" -> {
                UserSession user = new UserSession(id, txt);
                this.session.addNewUserSession(user);
                sendMessage(id, "Write title or id of book", getFindKeyboard());
            }
            case "Book list \uD83D\uDCDA" -> {
                List<ArrayDeque<String>> result = DataBase.Books.getTitles();
                if (result != null && !result.isEmpty()) {
                    //Выводим список. Иначе спим
                    int currentLength = 0;
                    String line;
                    StringBuilder message = new StringBuilder();
                    for (ArrayDeque<String> elem : result) {
                        line = elem.getFirst() + "; " + elem.getLast();
                        if (currentLength + 1 + line.length() < 4096) {
                            message.append(line).append("\n");
                            currentLength += line.length() + 1;
                        } else {
                            createAndSendMessage(id, message.toString(), false);
                            currentLength = line.length();
                            message = new StringBuilder(line);
                        }
                    }
                    if (!message.isEmpty()) {
                        createAndSendMessage(id, message.toString(), false);
                    }
                }

                this.session.removeUserSession(id);
            }
            case "Donations \uD83D\uDCB8" -> {
                //TODO: On developing
                sendMessage(id, "Coming soon");
            }
            case "/start" -> {
                sendMessage(id, "Hello. I am Ginzburg foundation. What does you want?", getDefaulKeyboard());
                this.session.removeUserSession(id);
            }
            case "/admin" -> {
                if (DataBase.isAdmin(id)) {
                    sendMessage(id, "Hello, boss", getAdminKeyboard());
                    this.session.addNewUserSession(new UserSession(id, "/admin"));
                }
            }
            default -> {
                UserSession user = this.session.getUserSession(id);
                if (user == null) {
                    sendMessage(id, "Doesn't understand what do you mean", getDefaulKeyboard());
                    this.session.removeUserSession(id);
                } else if (user.isAdmin()) {
                    DataBase.Admins.Warn(this, txt, String.valueOf(id));
                    switch (txt) {
                        case "Add new book" -> {
                            sendMessage(id, "Send Table");
                            user.trySetNewCommand("/addNewBook");
                        }
                        case "Format database" -> {
                            sendMessage(id, "Are you sure? Print \"Yes\" to format all library");
                            user.trySetNewCommand("/formatBooks");
                        }
                        case "Add new admin" -> {
                            sendMessage(id, "Write his id");
                            user.trySetNewCommand("/addAdmin");
                        }
                        case "Remove existing admin" -> {
                            sendMessage(id, "Write his id");
                            user.trySetNewCommand("/deleteAdmin");
                        }
                        case "Get Admins List" -> {
                            List<String> list = DataBase.Admins.getAdmins(user);
                            if (list != null) {
                                createAndSendMessage(id, list, true);
                            } else {
                                sendMessage(id, "I can't understand why there is no administrators...");
                            }
                        }
                        case "Start draw" -> {
                            this.draw = new DataBase.Draw();
                        }
                        case "Stop draw" -> {
                            sendMessage(id, "Draw winner is " + draw.getResults());
                        }

                        default -> {
                            switch (user.getLastCommand()) {
                                case FORMATBOOKS -> {
                                    if (txt.equals("Yes")) {
                                        DataBase.Books.rebaseTable(user);
                                    } else {
                                        sendMessage(id, "Okay");
                                    }
                                    user.trySetNewCommand("");
                                }
                                case ADDNEWADMIN -> DataBase.Admins.addNewAdmin(user, Long.parseLong(txt));
                                case DELETEADMIN -> DataBase.Admins.removeAdmin(user, Long.parseLong(txt));
                                default -> {
                                    sendMessage(id, "Returning to the default keyboard", getDefaulKeyboard());
                                    this.session.removeUserSession(id);
                                }
                            }

                        }
                    }
                } else {
                    switch (user.getLastCommand()) {
                        case GETBOOK -> {
                            if (txt.equals("Back...")) {
                                sendMessage(id, "Please, choose the command", getDefaulKeyboard());
                                return;
                            }
                            ArrayDeque<String[]> result = DataBase.Books.getBookByName(txt);
                            if (result != null && !result.isEmpty()) {
                                for (String[] elem : result) {
                                    createAndSendMessage(id, elem[0] + "\n Link for downloading: " + elem[1], false);
                                }
                            } else {
                                sendMessage(id, "There is no results...", getDefaulKeyboard());
                            }
                            this.session.removeUserSession(id);
                        }

                        default -> {sendMessage(id, "Doesn't understand what do you mean", getDefaulKeyboard());}
                    }
                }
            }

        }
    }

    public void sendMessage(Long id, String txt) {
        SendMessage m = new SendMessage(id.toString(), txt);
        try {
            execute(m);
        } catch (TelegramApiException e) {
            System.out.println(e.getMessage());
        }
    }

    private void sendMessage(Long id, String txt, ReplyKeyboard keyboardMarkup) {
        SendMessage message = new SendMessage(id.toString(),0, txt, "", false,
                false, 0, keyboardMarkup, null, true, false);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            System.out.println(e.getMessage());
        }
    }

    private int createAndSendMessage(Long id, String result, boolean safeKeyboard) {
        if (!safeKeyboard) {
            if (result == null) {
                sendMessage(id, "Query function is not avaible right now. Please try later...", getDefaulKeyboard());
                return -1;
            } else {
                while (result.length() >= 4096) {
                    sendMessage(id, result.substring(0, 4094), getDefaulKeyboard());
                    result = result.substring(4095, result.length() - 1);
                }
                sendMessage(id, result, getDefaulKeyboard());
                return 0;
            }
        } else {
            if (result == null) {
                sendMessage(id, "Query function is not available right now. Please try later...");
                return -1;
            } else {
                while (result.length() >= 4096) {
                    sendMessage(id, result.substring(0, 4094));
                    result = result.substring(4095, result.length() - 1);
                }
                sendMessage(id, result);
                return 0;
            }
        }
    }
    private void createAndSendMessage(Long id, List<String> result, boolean safeKeyboard) {
        StringBuilder temp = new StringBuilder();
        int length = 0;
        for (String elem: result) {
            if (length + elem.length() + 1 < 4096) {
                temp.append(elem).append("\n");
                length += elem.length() + 1;
            } else {
                createAndSendMessage(id, temp.toString(), safeKeyboard);
                temp = new StringBuilder(elem);
                length = elem.length();
            }
        }

        if (!temp.isEmpty()) {
            createAndSendMessage(id, temp.toString(), safeKeyboard);
        }
    }

    private ReplyKeyboardMarkup getDefaulKeyboard() {
        ReplyKeyboardMarkup res = new ReplyKeyboardMarkup(getButtons(new String[][]{{"Book list \uD83D\uDCDA", "Choose a book\uD83D\uDCD6"}, {"Donations \uD83D\uDCB8"}}));
        res.setResizeKeyboard(true);
        return res;
        /*
            List<KeyboardRow> rows = new ArrayList<>();
            rows.add(new KeyboardRow(List.of(new KeyboardButton("book list \uD83D\uDCDA"))));
            rows.add(new KeyboardRow(List.of(new KeyboardButton("choose a book\uD83D\uDCD6"))));
            return new ReplyKeyboardMarkup(rows);
         */
    }
    private static ReplyKeyboardRemove keyboardRemove() {
        return new ReplyKeyboardRemove(true);
    }
    private static ReplyKeyboardMarkup getFindKeyboard() {
        ReplyKeyboardMarkup res = new ReplyKeyboardMarkup(getButtons(new String[][]{{"Back..."}}));
        res.setResizeKeyboard(true);
        return res;
    }
    private static ReplyKeyboardMarkup getAdminKeyboard() {
        ReplyKeyboardMarkup res = new ReplyKeyboardMarkup(getButtons(new String[][]{{"Add new book", "Format database"}, {"Add new admin", "Remove existing admin", "Get Admins List"}, {"Start draw", "Stop draw"}}), true, false, false, null, false);
        return res;
        /*
        List<KeyboardRow> rows = new ArrayList<>();
        rows.add(new KeyboardRow(List.of(new KeyboardButton("Add new book"))));
        return new ReplyKeyboardMarkup(rows);
         */
    }
    private static List<KeyboardRow> getButtons(String[][] values) {
        List<KeyboardRow> result = new ArrayList<>();
        KeyboardRow keyboardRow;
        for (String[] row: values) {
            keyboardRow = new KeyboardRow();
            for (String elem: row) {
                keyboardRow.add(new KeyboardButton(elem));
            }
            result.add(keyboardRow);
        }
        return result;
    }
}