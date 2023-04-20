package org.example;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.db.DataBase;
import org.db.HtmlRequest;
import org.session.LastSessions;
import org.session.UserSession;
import org.session.chatSession;
import org.session.groupSessions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
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
import java.io.*;
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
    private groupSessions chats;
    public DataBase.Draw draw;


    Bot(String name) {
        this.session = new LastSessions(maxUsersCount);
        this.chats = new groupSessions();
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
        if (!msg.getChat().isUserChat()) {
            //TODO: Создать конструктор чатов
            if (this.chats.getChat(msg.getChatId()) == null) {
                this.chats.addSession(new chatSession(msg.getChatId(), this));
            }
            this.chats.getChat(msg.getChatId()).update(msg);
            //sendMessage(msg.getChatId(), "Hi");
        } else {
            if (draw != null && draw.getActive()) {
                draw.addNewUser(id, usr.getUserName());
            }
            if (txt.equals("/start")) {
                this.session.addNewUserSession(new UserSession(id, false, this));

            } else {
                //Getting userCommand
                UserSession user = this.session.getUserSession(id);
                if (user == null) {
                    //TODO: Вытащить пользователя из базы данных
                    this.session.addNewUserSession((user = new UserSession(id, false, this)));
                }
                user.update(msg);
            }
        }
    }

    public void sendChatMesage(Long id, String txt) {

    }

    public void sendMessage(Long id, String txt) {
        SendMessage m = new SendMessage(id.toString(), txt);
        try {
            execute(m);
        } catch (TelegramApiException e) {
            System.out.println(e.getMessage());
        }
    }

    public void sendMessage(Long id, String txt, ReplyKeyboard keyboardMarkup) {
        SendMessage message = new SendMessage(id.toString(),0, txt, "", false,
                false, 0, keyboardMarkup, null, true, false);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            System.out.println(e.getMessage());
        }
    }
    public void sendMessage(Long id, String txt, int messageId) {
        SendMessage message = new SendMessage(id.toString(),0, txt, "", false,
                false, messageId, null, null, true, false);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            System.out.println(e.getMessage());
        }
    }

    public int createAndSendMessage(Long id, String result, int messageId) {
        if (result == null) {
            sendMessage(id, "Query function is not available right now. Please try later...", messageId);
            return -1;
        } else {
            while (result.length() >= 4096) {
                sendMessage(id, result.substring(0, 4094), messageId);
                result = result.substring(4095, result.length() - 1);
            }
            sendMessage(id, result, messageId);
            return 0;
        }
    }
    public void createAndSendMessage(Long id, List<String> result, int messageId) {
        StringBuilder temp = new StringBuilder();
        int length = 0;
        for (String elem: result) {
            if (length + elem.length() + 1 < 4096) {
                temp.append(elem).append("\n");
                length += elem.length() + 1;
            } else {
                createAndSendMessage(id, temp.toString(), messageId);
                temp = new StringBuilder(elem);
                length = elem.length();
            }
        }

        if (!temp.isEmpty()) {
            createAndSendMessage(id, temp.toString(), messageId);
        }
    }

    public int createAndSendMessage(Long id, String result, ReplyKeyboardMarkup keyboardMarkup) {
        if (keyboardMarkup != null) {
            if (result == null) {
                sendMessage(id, "Query function is not avaible right now. Please try later...", keyboardMarkup);
                return -1;
            } else {
                while (result.length() >= 4096) {
                    sendMessage(id, result.substring(0, 4094), keyboardMarkup);
                    result = result.substring(4095, result.length() - 1);
                }
                sendMessage(id, result, keyboardMarkup);
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
    public void createAndSendMessage(Long id, List<String> result, ReplyKeyboardMarkup keyboardMarkup) {

    }

    public ReplyKeyboardMarkup getDefaulKeyboard() {
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
    public static ReplyKeyboardRemove keyboardRemove() {
        return new ReplyKeyboardRemove(true);
    }
    public static ReplyKeyboardMarkup getFindKeyboard() {
        ReplyKeyboardMarkup res = new ReplyKeyboardMarkup(getButtons(new String[][]{{"Back..."}}));
        res.setResizeKeyboard(true);
        return res;
    }
    public static ReplyKeyboardMarkup getAdminKeyboard() {
        ReplyKeyboardMarkup res = new ReplyKeyboardMarkup(getButtons(new String[][]{{"Add new book", "Format database"}, {"Add new admin", "Remove existing admin", "Get Admins List"}, {"Start draw", "Stop draw"}}), true, false, false, null, false);
        return res;
        /*
        List<KeyboardRow> rows = new ArrayList<>();
        rows.add(new KeyboardRow(List.of(new KeyboardButton("Add new book"))));
        return new ReplyKeyboardMarkup(rows);
         */
    }

    public static ReplyKeyboardMarkup getKeyboard(String[][] buttons) {
        ReplyKeyboardMarkup res = new ReplyKeyboardMarkup(getButtons(buttons), true, false, false, null, false);
        return res;
    }
    
    public static List<KeyboardRow> getButtons(String[][] values) {
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

    //New
    public void defaultMessage(Long id) {
        sendMessage(id, "Please type correct button");
    }
    public void SendMessages(Long id, List<String> Text, ReplyKeyboard keyboard) {
        StringBuilder result = new StringBuilder();
        int length = 0;
        for (String elem: Text) {
            if (elem.length() >= 4096) {
                if (!result.isEmpty())
                    sendMessage(id, result.toString(), (ReplyKeyboard) null);
                while (elem.length() >= 4096) {
                    sendMessage(id, elem.substring(0, 4095), (ReplyKeyboard) null);
                    elem = elem.substring(4095, elem.length()-1);
                }
                continue;
            }
            if (length + elem.length() + 1 < 4096) {
                result.append(elem).append("\n");
                length += elem.length() + 1;
            }
            else {
                sendMessage(id, result.toString(), (ReplyKeyboard) null);
                result = new StringBuilder(elem);
                length = elem.length();
            }
        }
        if (!result.isEmpty()) {
            sendMessage(id, result.toString(), (ReplyKeyboard) null);
        }
    }
    public void SendMessages(Long id, List<String> Text, String pathToImage, ReplyKeyboard keyboard) {
        String image = String.valueOf(pathToImage);
        StringBuilder result = new StringBuilder();
        int length = 0;
        for (String elem: Text) {
            if (elem.length() >= 4096) {
                if (!result.isEmpty()) {
                    sendMessage(id, result.toString(), image, keyboard);
                    image = null;
                }
                while (elem.length() >= 4096) {
                    sendMessage(id, elem.substring(0, 4095), image, keyboard);
                    image = null;
                    elem = elem.substring(4095, elem.length()-1);
                }
                continue;
            }
            if (length + elem.length() + 1 < 4096) {
                result.append(elem).append("\n");
                length += elem.length() + 1;
            }
            else {
                sendMessage(id, result.toString(), image, keyboard);
                image = null;
                result = new StringBuilder(elem);
                length = elem.length();
            }
        }
        if (!result.isEmpty()) {
            sendMessage(id, result.toString(), image, keyboard);
        }
    }

    private int sendMessage(Long id, String text, String url, ReplyKeyboard keyboard) {
        SendMessage message;
        if (url != null) {
            message = new SendMessage(id.toString(), "[]("+url+")"+text);
            message.setParseMode("markdown");
            message.setReplyMarkup(keyboard);
        } else {
            message = new SendMessage(id.toString(), text);
            message.setReplyMarkup(keyboard);
        }
        try {
            execute(message);
        } catch (TelegramApiException e) {
            System.out.println(e.getMessage());
            return -1;
        }
        return 0;
    }

    public int sendImage(Long id, String pathToImage) {
        SendPhoto photo = new SendPhoto(id.toString(), new InputFile(new File(pathToImage)));
        try {
            execute(photo);
        } catch (TelegramApiException e) {
            System.out.println(e.getMessage());
            return -1;
        }
        return  0;
    }

    //Из другого бота
    public void SendMessages(Long id, List<String> Text, String pathToImage) {
        String image = String.valueOf(pathToImage);
        StringBuilder result = new StringBuilder();
        int length = 0;
        for (String elem: Text) {
            if (elem.length() >= 4096) {
                if (!result.isEmpty()) {
                    sendMessage(id, result.toString(), image);
                    image = null;
                }
                while (elem.length() >= 4096) {
                    sendMessage(id, elem.substring(0, 4095), image);
                    image = null;
                    elem = elem.substring(4095, elem.length()-1);
                }
                continue;
            }
            if (length + elem.length() + 1 < 4096) {
                result.append(elem).append("\n");
                length += elem.length() + 1;
            }
            else {
                sendMessage(id, result.toString(), image);
                image = null;
                result = new StringBuilder(elem);
                length = elem.length();
            }
        }
        if (!result.isEmpty()) {
            sendMessage(id, result.toString(), image);
        }
    }

    private int sendMessage(Long id, String text, String url) {
        SendMessage message;
        if (url != null) {
            message = new SendMessage(id.toString(), "[]("+url+")"+text);
            message.setParseMode("markdown");
        } else {
            message = new SendMessage(id.toString(), text);
        }
        try {
            execute(message);
        } catch (TelegramApiException e) {
            System.out.println(e.getMessage());
            return -1;
        }
        return 0;
    }

}