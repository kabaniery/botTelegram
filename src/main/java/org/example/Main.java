package org.example;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class Main {
    public static void main(String[] args) {
        TelegramBotsApi botsApi;
        try {
            botsApi = new TelegramBotsApi(DefaultBotSession.class);

            botsApi.registerBot(new Bot("Log.txt"));
        } catch (TelegramApiException e) {
            System.out.println("Something went wrong. Can't start session. Program will stop");
            return;
        }
        System.out.println("Bot started...");
    }
}