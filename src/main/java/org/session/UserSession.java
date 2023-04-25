package org.session;


import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.db.DataBase;
import org.db.ExplorerManager;
import org.db.HtmlRequest;
import org.db.PythonScripts;
import org.example.Bot;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.List;

public class UserSession {
    Long id;
    Bot bot;

    //New system

    //Admin's keyboard


    private int state;
    public boolean isAdmin() {
        return (state & 4) == 1;
    }
    public UserSession(Long id, boolean isChannel, Bot bot)
    {
        this.id = id;
        this.bot = bot;
        if (isChannel) {
            bot.sendMessage(id, "Hello, I am Ginzburg foundation chat bot. Use \"Start draw\" and \"Get result\" to activate/deactivate draws");
        } else {
            bot.sendMessage(id, "Hello, I am Ginzburg foundation bot. What do you want to learn about?", Bot.getKeyboard(languageKeyboard));
        }
        this.state = 0;
    }


    private static final String[][] languageKeyboard = {{"English"}};
    private static final String[][][][] keyboards = {{
            {{"Library management"}, {"Admins panel"}, {"Draw settings"}, {"Turn off"}}, //default admins panel - 0
            {{"Add new table"}, {"Format library"}, {"Back"}}, //Admins library panel - 1
            {{"Add new admin", "Delete existing admin"}, {"Get list of admins"}, {"Back"}}, //Admins Admin panel - 2
            {{"Start draw", "Stop draw"}, {"Back"}}, //Admin draw panel - 3
            {{"Culture", "Medicine", "Science"}, {"Economics", "Climate", "Art"}, {"ChatGPT", "Other", "Reselect language"}}, //User default panel - 4
            {{"Culture", "Medicine"}, {"Наука", "Economics"}, {"Climate", "Art"}, {"Back"}}, //Panel of themes - 5
            {{"Find book", "Recommend book"}, {"Get library"}, {"Back"}}, //User library panel - 6
            {{"Donations"}, {"Buy ADS"}, {"Back"}}, //User other panel - 7
            {{"Quit ChatGPT dialog /\\/\\"}, {"/Clear bot history"}}, //User ChatGPT panel - 8
    }};
    private static final String[][] messages = {
            {
                "Choose a language",
                    "Choose an action",
                    "It's chatGPT assistant. Write an interesting question"
            }
            };

    public void update(Message command) {
        //Проверяем язык
        switch(this.state & 7) {
            //English
            case 1 -> {
                //Проверяем админство
                if (((this.state >> 3) & 1) == 0) {
                    //Панель пользователя
                    //Если команда не выбрана, то выбираем её
                    if (((this.state >> 4) & 15) == 0) {
                        switch (command.getText()) {
                            case "Culture" -> {
                                this.state += 1 << 4;
                                bot.sendMessage(id, "Choose an action with library", Bot.getKeyboard(keyboards[0][6]));
                            }
                            case "Medicine" -> {
                                this.state += 2 << 4;
                                bot.sendMessage(id, "Choose an action with library", Bot.getKeyboard(keyboards[0][6]));
                            }
                            case "Science" -> {
                                this.state += 3 << 4;
                                bot.sendMessage(id, "Choose an action with library", Bot.getKeyboard(keyboards[0][6]));
                            }
                            case "Economics" -> {
                                this.state += 4 << 4;
                                bot.sendMessage(id, "Choose an action with library", Bot.getKeyboard(keyboards[0][6]));
                            }
                            case "Climate" -> {
                                this.state += 5 << 4;
                                bot.sendMessage(id, "Choose an action with library", Bot.getKeyboard(keyboards[0][6]));
                            }
                            case "Art" -> {
                                this.state += 6 << 4;
                                bot.sendMessage(id, "Choose an action with library", Bot.getKeyboard(keyboards[0][6]));
                            }
                            case "ChatGPT" -> {
                                this.state += 7 << 4;
                                bot.sendMessage(id, "Hello, It's a chatGPT assistant. Ask interesting question", Bot.getKeyboard(keyboards[0][8]));
                            }
                            case "Other" -> {
                                this.state += 8 << 4;
                                bot.sendMessage(id, "Bot created by dyda0505", Bot.getKeyboard(keyboards[0][7]));//TODO: Сделать нормальное описание
                            }
                            case "Reselect language" -> {
                                this.state = 0;
                                bot.sendMessage(id, "Choose a language", Bot.getKeyboard(languageKeyboard));
                            }
                            default -> {
                                bot.defaultMessage(id);
                            }
                        }
                    }
                    //Если команда - chatGPT
                    if (((this.state >> 4) & 15) == 7) {
                        switch (command.getText()) {
                            //Выйти из chatGPT
                            case "Quit ChatGPT dialog /\\/\\" -> {
                                this.state -= 7 << 4;
                                bot.sendMessage(id, "Bye!");
                            }
                            //Удалить историю запросов
                            case "/Clear bot history" -> {
                                try {
                                    Files.delete(Path.of("gpthistory/" + id.toString() + ".txt"));
                                } catch (IOException e) {}
                            }
                            default -> {

                            }
                        }
                    }
                }
            }
            //Выбираем язык
            default -> {
                switch (command.getText()) {
                    case "English" -> {
                        this.state += 1;
                    }
                    default -> {
                        bot.sendMessage(id, "Please, choose one of selected buttons");
                    }
                }
            }
        }
    }
}
