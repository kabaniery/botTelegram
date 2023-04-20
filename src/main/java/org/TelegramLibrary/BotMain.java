package org.TelegramLibrary;

import org.db.DataBase;
import org.session.LastSessions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;


import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;

public class BotMain extends TelegramLongPollingBot {
    public BotMain() {

    }
    DataBase.Draw draw = null;
    @Override
    public String getBotUsername() {
        return "kabanieryBotExample_bot";
    }

    public static String getToken() {
        return "6014408564:AAGzkX8MgIZJbj2L8hH8j0YIo9p7LlB9jkI";
    }

    @Override
    public void onUpdateReceived(Update update) {
        Message msg = update.getMessage();
        User usr = msg.getFrom();
        Chat chat = msg.getChat();
        msg.getChatId();
        //Most import data
        Long id = usr.getId();
        String txt = msg.getText();


    }


    //Методы вывода данных
    public void SendMessages(Long id, List<String> Text) {
        StringBuilder result = new StringBuilder();
        int length = 0;
        for (String elem: Text) {
            if (elem.length() >= 4096) {
                if (!result.isEmpty())
                    sendMessage(id, result.toString(), null);
                while (elem.length() >= 4096) {
                    sendMessage(id, elem.substring(0, 4095), null);
                    elem = elem.substring(4095, elem.length()-1);
                }
                continue;
            }
            if (length + elem.length() + 1 < 4096) {
                result.append(elem).append("\n");
                length += elem.length() + 1;
            }
            else {
                sendMessage(id, result.toString(), null);
                result = new StringBuilder(elem);
                length = elem.length();
            }
        }
        if (!result.isEmpty()) {
            sendMessage(id, result.toString(), null);
        }
    }
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
}
