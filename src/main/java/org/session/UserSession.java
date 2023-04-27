package org.session;


import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.db.DataBase;
import org.db.ExplorerManager;
import org.db.HtmlRequest;
import org.db.PythonScripts;
import org.example.Bot;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class UserSession {
    Long id;
    Bot bot;

    //New system

    //Admin's keyboard


    private int state;
    public boolean isAdmin() {
        return (state & 8) == 8;
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


    private static final String[][] languageKeyboard = {{"English \uD83C\uDDEC\uD83C\uDDE7", "Arabic \uD83C\uDDE6\uD83C\uDDEA"}, {"Spanish \uD83C\uDDEA\uD83C\uDDF8", "Chinese \uD83C\uDDE8\uD83C\uDDF3"}, {"Russian \uD83C\uDDF7\uD83C\uDDFA", "French \uD83C\uDDEB\uD83C\uDDF7"}};
    private static final String[][][][] keyboards = {
            {//English
                    {{"Library management"}, {"Admins panel"}, {"Draw settings"}, {"Turn off"}}, //default admins panel - 0
                    {{"Add new table"}, {"Format library"}, {"Back"}}, //Admins library panel - 1
                    {{"Add new admin", "Delete existing admin"}, {"Get list of admins"}, {"Back"}}, //Admins Admin panel - 2
                    {{"Start draw", "Stop draw"}, {"Back"}}, //Admin draw panel - 3
                    {{"Culture", "Medicine", "Science"}, {"Economics", "Climate", "Art"}, {"ChatGPT", "Other", "Reselect language"}}, //User default panel - 4
                    {{"Culture", "Medicine"}, {"Наука", "Economics"}, {"Climate", "Art"}, {"Back"}}, //Panel of themes - 5
                    {{"Find book", "Recommend book"}, {"Get library"}, {"Back"}}, //User library panel - 6
                    {{"Donations"}, {"Buy ADS"}, {"Back"}}, //User other panel - 7
                    {{"Quit ChatGPT dialog /\\/\\"}, {"/Clear bot history"}}, //User ChatGPT panel - 8
            },
            {//Arabic
                    {{"Library management"}, {"Admins panel"}, {"Draw settings"}, {"Turn off"}}, //default admins panel - 0
                    {{"Add new table"}, {"Format library"}, {"Back"}}, //Admins library panel - 1
                    {{"Add new admin", "Delete existing admin"}, {"Get list of admins"}, {"Back"}}, //Admins Admin panel - 2
                    {{"Start draw", "Stop draw"}, {"Back"}}, //Admin draw panel - 3
                    {{"Culture", "Medicine", "Science"}, {"Economics", "Climate", "Art"}, {"ChatGPT", "Other", "Reselect language"}}, //User default panel - 4
                    {{"Culture", "Medicine"}, {"Наука", "Economics"}, {"Climate", "Art"}, {"Back"}}, //Panel of themes - 5
                    {{"Find book", "Recommend book"}, {"Get library"}, {"Back"}}, //User library panel - 6
                    {{"Donations"}, {"Buy ADS"}, {"Back"}}, //User other panel - 7
                    {{"Quit ChatGPT dialog /\\/\\"}, {"/Clear bot history"}}, //User ChatGPT panel - 8
            },
            {//Spanish
                    {{"Library management"}, {"Admins panel"}, {"Draw settings"}, {"Turn off"}}, //default admins panel - 0
                    {{"Add new table"}, {"Format library"}, {"Back"}}, //Admins library panel - 1
                    {{"Add new admin", "Delete existing admin"}, {"Get list of admins"}, {"Back"}}, //Admins Admin panel - 2
                    {{"Start draw", "Stop draw"}, {"Back"}}, //Admin draw panel - 3
                    {{"Culture", "Medicine", "Science"}, {"Economics", "Climate", "Art"}, {"ChatGPT", "Other", "Reselect language"}}, //User default panel - 4
                    {{"Culture", "Medicine"}, {"Наука", "Economics"}, {"Climate", "Art"}, {"Back"}}, //Panel of themes - 5
                    {{"Find book", "Recommend book"}, {"Get library"}, {"Back"}}, //User library panel - 6
                    {{"Donations"}, {"Buy ADS"}, {"Back"}}, //User other panel - 7
                    {{"Quit ChatGPT dialog /\\/\\"}, {"/Clear bot history"}}, //User ChatGPT panel - 8
            },
            {//Chinese
                    {{"Library management"}, {"Admins panel"}, {"Draw settings"}, {"Turn off"}}, //default admins panel - 0
                    {{"Add new table"}, {"Format library"}, {"Back"}}, //Admins library panel - 1
                    {{"Add new admin", "Delete existing admin"}, {"Get list of admins"}, {"Back"}}, //Admins Admin panel - 2
                    {{"Start draw", "Stop draw"}, {"Back"}}, //Admin draw panel - 3
                    {{"Culture", "Medicine", "Science"}, {"Economics", "Climate", "Art"}, {"ChatGPT", "Other", "Reselect language"}}, //User default panel - 4
                    {{"Culture", "Medicine"}, {"Наука", "Economics"}, {"Climate", "Art"}, {"Back"}}, //Panel of themes - 5
                    {{"Find book", "Recommend book"}, {"Get library"}, {"Back"}}, //User library panel - 6
                    {{"Donations"}, {"Buy ADS"}, {"Back"}}, //User other panel - 7
                    {{"Quit ChatGPT dialog /\\/\\"}, {"/Clear bot history"}}, //User ChatGPT panel - 8
            },
            {//Russian
                    {{"Library management"}, {"Admins panel"}, {"Draw settings"}, {"Turn off"}}, //default admins panel - 0
                    {{"Add new table"}, {"Format library"}, {"Back"}}, //Admins library panel - 1
                    {{"Add new admin", "Delete existing admin"}, {"Get list of admins"}, {"Back"}}, //Admins Admin panel - 2
                    {{"Start draw", "Stop draw"}, {"Back"}}, //Admin draw panel - 3
                    {{"Culture", "Medicine", "Science"}, {"Economics", "Climate", "Art"}, {"ChatGPT", "Other", "Reselect language"}}, //User default panel - 4
                    {{"Culture", "Medicine"}, {"Наука", "Economics"}, {"Climate", "Art"}, {"Back"}}, //Panel of themes - 5
                    {{"Find book", "Recommend book"}, {"Get library"}, {"Back"}}, //User library panel - 6
                    {{"Donations"}, {"Buy ADS"}, {"Back"}}, //User other panel - 7
                    {{"Quit ChatGPT dialog /\\/\\"}, {"/Clear bot history"}}, //User ChatGPT panel - 8
            },
            {//French
                    {{"Library management"}, {"Admins panel"}, {"Draw settings"}, {"Turn off"}}, //default admins panel - 0
                    {{"Add new table"}, {"Format library"}, {"Back"}}, //Admins library panel - 1
                    {{"Add new admin", "Delete existing admin"}, {"Get list of admins"}, {"Back"}}, //Admins Admin panel - 2
                    {{"Start draw", "Stop draw"}, {"Back"}}, //Admin draw panel - 3
                    {{"Culture", "Medicine", "Science"}, {"Economics", "Climate", "Art"}, {"ChatGPT", "Other", "Reselect language"}}, //User default panel - 4
                    {{"Culture", "Medicine"}, {"Наука", "Economics"}, {"Climate", "Art"}, {"Back"}}, //Panel of themes - 5
                    {{"Find book", "Recommend book"}, {"Get library"}, {"Back"}}, //User library panel - 6
                    {{"Donations"}, {"Buy ADS"}, {"Back"}}, //User other panel - 7
                    {{"Quit ChatGPT dialog /\\/\\"}, {"/Clear bot history"}}, //User ChatGPT panel - 8
            }

    };
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
                if (command.getText().equals("/admin") && DataBase.isAdmin(id) && !this.isAdmin()) {
                    this.state += 1 << 3;
                    bot.sendMessage(id, "Administrator panel is on", Bot.getKeyboard(keyboards[0][0]));
                }
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
                        return;
                    }
                    //Если команда - chatGPT
                    if (((this.state >> 4) & 15) == 7) {
                        switch (command.getText()) {
                            //Выйти из chatGPT
                            case "Quit ChatGPT dialog /\\/\\" -> {
                                this.state -= 7 << 4;
                                bot.sendMessage(id, "Bye!", Bot.getKeyboard(keyboards[0][4]));
                            }
                            //Удалить историю запросов
                            case "/Clear bot history" -> {
                                try {
                                    Files.delete(Path.of("gpthistory/" + id.toString() + ".txt"));
                                } catch (IOException ignored) {}
                            }
                            default -> {
                                bot.sendMessage(id, "ChatGPT is thinking on answer to your question");
                                String[] result = PythonScripts.getGPTMessages(id, command.getText());
                                bot.createAndSendMessage(id, List.of(result), command.getMessageId());
                            }
                        }
                    }
                    //Если команда - other
                    else if (((this.state >> 4) & 15) == 8) {
                        switch (command.getText()) {
                            case "Donations" -> bot.sendMessage(id, "Please, use an Ethereum wallet for donations. 0x45C02ECe2e6a3b6f0d52456eD483f320F77a19F9. Thanks"); //TODO: Уточнить все сообщения
                            case "Buy ADS" -> bot.sendMessage(id, "Please contact our manager @kabaniery to order an advertisement");
                            case "Back" -> {
                                this.state -= 8 << 4;
                                bot.sendMessage(id, "Choose an interesting subject", Bot.getKeyboard(keyboards[0][4]));
                            }
                            default -> bot.defaultMessage(id);
                        }
                    }
                    //Если команда - библиотека
                    else {
                        switch ((this.state >> 8) & 7) {
                            //Найти книгу
                            case 1 -> {
                                this.state -= 1 << 8;
                                ArrayDeque<String[]> result = DataBase.Books.getBookByName(command.getText(), (this.state >> 4) & 15);
                                if (result == null || result.isEmpty()) {
                                    bot.sendMessage(id, "There is no results");
                                    return;
                                }
                                List<String> print = new ArrayList<>();
                                for (String[] elem: result) {
                                    print.add(elem[0] + ". Link for downloading: " + elem[1]);
                                }
                                bot.createAndSendMessage(id, print, null);
                            }
                            //Порекомендовать книгу
                            case 2 -> {
                                this.state -= 2 << 8;
                                DataBase.Books.Book book = DataBase.Books.getBook(command.getText(), (this.state >> 4) & 15);
                                int[] results;
                                if (book == null) {
                                    results = DataBase.Books.getRecommendation(new String[]{command.getText()}, (this.state >> 4) & 15);
                                } else {
                                    results = DataBase.Books.getRecommendation(book.genres.split(", "), (this.state >> 4) & 15);
                                }
                                if (results == null) {
                                    bot.sendMessage(id, "There is no recommendation by your request");
                                    return;
                                }
                                List<String> print = new ArrayList<>();
                                DataBase.Books.Book data;
                                for (int elem: results) {
                                    data = DataBase.Books.getBook(String.valueOf(elem), (this.state >> 4) & 15);
                                    if (data != null) {
                                        print.add(data.id + ". " + data.title + ". Link for downloading: " + data.link);
                                    }
                                }
                                bot.createAndSendMessage(id, print, null);
                            }
                            default -> {
                                switch (command.getText()) {
                                    case "Find book" -> {
                                        this.state += 1 << 8;
                                        bot.sendMessage(id, "Write title or id of book, please");
                                    }
                                    case "Recommend book" -> {
                                        this.state += 2 << 8;
                                        bot.sendMessage(id, "Write name of genre or existing book, please");
                                    }
                                    case "Get library" -> {
                                        List<ArrayDeque<String>> result = DataBase.Books.getTitles((this.state >> 4) & 15);
                                        if (result == null || result.isEmpty()) {
                                            bot.sendMessage(id, "Library is empty now");
                                            return;
                                        }
                                        List<String> print = new ArrayList<>();
                                        for (ArrayDeque<String> elem: result) {
                                            print.add(elem.getFirst() + "; " + elem.getLast());
                                        }
                                        bot.createAndSendMessage(id, print, null);
                                    }
                                    case "Back" -> {
                                        this.state -= this.state & (15 << 4);
                                        bot.sendMessage(id, "Choose an interesting subject", Bot.getKeyboard(keyboards[0][4]));
                                    }
                                    default -> bot.defaultMessage(id);
                                 }
                            }
                        }
                    }
                }
                //Панель администратора
                else {
                    switch ((this.state >> 4) & 7) {
                        //Управление библиотекой
                        case 1 -> {
                            //Если мы не выбрали библиотеку
                            if (((this.state >> 7) & 7) == 0) {
                                switch (command.getText()) {
                                    case "Art":
                                        this.state += 1 << 7;
                                    case "Climate":
                                        this.state += 1 << 7;
                                    case "Economics":
                                        this.state += 1 << 7;
                                    case "Science":
                                        this.state += 1 << 7;
                                    case "Medicine":
                                        this.state += 1 << 7;
                                    case "Culture":
                                        this.state += 1 << 7;
                                        bot.sendMessage(id, "Choose an action with a library", Bot.getKeyboard(keyboards[0][1]));
                                        break;
                                    case "Back":
                                        this.state -= 1 << 4;
                                        bot.sendMessage(id, "Choose an interesting action", Bot.getKeyboard(keyboards[0][0]));
                                        break;
                                    default:
                                        bot.defaultMessage(id);
                                }
                            }
                            else {
                                switch ((this.state >> 10) & 7) {
                                    //Добавить новую таблицу
                                    case 1 -> {
                                        this.state -= 1 << 10;
                                        String path = HtmlRequest.getFile(bot.getBotToken(), command.getDocument().getFileId());
                                        if (path == null) {
                                            bot.sendMessage(id, "Something went wrong");
                                        } else {
                                            try {
                                                CSVReader csvReader = new CSVReader(new FileReader(path));
                                                String[] line;
                                                csvReader.readNext();
                                                csvReader.readNext();
                                                while ((line = csvReader.readNext()) != null) {
                                                    DataBase.Books.setNewBook(this, line, (this.state >> 7) & 7);
                                                }
                                                bot.sendMessage(id, "Success");
                                            } catch (CsvValidationException | IOException e) {
                                                bot.sendMessage(id, "Something went wrong");
                                            }
                                        }
                                    }
                                    //Отформатировать таблицу
                                    case 2 -> {
                                        this.state -= 2 << 10;
                                        if (command.getText().equals("Yes")) {
                                            DataBase.Books.rebaseTable(this, (this.state >> 7) & 7);
                                            bot.sendMessage(id, "Success");
                                        } else {
                                            bot.sendMessage(id, "Ok");
                                        }
                                    }
                                    default -> {
                                        switch (command.getText()) {
                                            case "Add new table" -> {
                                                this.state += 1 << 10;
                                                bot.sendMessage(id, "Send a table");
                                            }
                                            case "Format library" -> {
                                                this.state += 2 << 10;
                                                bot.sendMessage(id, "Are you sure? Print \"Yes\" to format library");
                                            }
                                            case "Back" -> {
                                                this.state -= this.state & (7 << 7);
                                                bot.sendMessage(id, "Choose a subject", Bot.getKeyboard(keyboards[0][5]));
                                            }
                                            default -> bot.defaultMessage(id);
                                        }
                                    }
                                }
                            }
                        }
                        //Управление админской панелью
                        case 2 -> {
                            switch ((this.state >> 7) & 7) {
                                //Добавить нового админа
                                case 1 -> {
                                    this.state -= 1 << 7;
                                    DataBase.Admins.addNewAdmin(this, Long.parseLong(command.getText()));
                                }
                                //Удалить существующего админа
                                case 2 -> {
                                    this.state -= 2 << 7;
                                    DataBase.Admins.removeAdmin(this, Long.parseLong(command.getText()));
                                }
                                default -> {
                                    switch (command.getText()) {
                                        case "Add new admin" -> {
                                            this.state += 1 << 7;
                                            bot.sendMessage(id, "Write id of new administrator");
                                        }
                                        case "Delete existing admin" -> {
                                            this.state += 2 << 7;
                                            bot.sendMessage(id, "Write id of existing administrator");
                                        }
                                        case "Get list of admins" -> {
                                            bot.createAndSendMessage(id, Objects.requireNonNull(DataBase.Admins.getAdmins(this)), null);
                                        }
                                        case "Back" -> {
                                            this.state -= 2 << 4;
                                            bot.sendMessage(id, "Choose an interesting action", Bot.getKeyboard(keyboards[0][0]));
                                        }
                                        default -> bot.defaultMessage(id);
                                    }
                                }
                            }
                        }
                        //Управление розыгрышами
                        case 3 -> {
                            switch (command.getText()) {
                                case "Start draw" -> {
                                    bot.draw = new DataBase.Draw();
                                    bot.sendMessage(id, "New draw started!");
                                }
                                case "Stop draw" -> {
                                    if (bot.draw == null || !bot.draw.getActive()) {
                                        bot.sendMessage(id, "Draw isn't started");
                                    } else {
                                        bot.sendMessage(id, "Winner in draw is @" + bot.draw.getResults());
                                    }
                                }
                                case "Back" -> {
                                    this.state -= 3 << 4;
                                    bot.sendMessage(id, "Choose an interesting action", Bot.getKeyboard(keyboards[0][0]));
                                }
                                default -> bot.defaultMessage(id);
                            }
                        }
                        default -> {
                            switch (command.getText()) {
                                case "Library management" -> {
                                    this.state += 1 << 4;
                                    bot.sendMessage(id, "Choose a subject", Bot.getKeyboard(keyboards[0][5]));
                                }
                                case "Admins panel" -> {
                                    this.state += 2 << 4;
                                    bot.sendMessage(id, "Choose an interesting action", Bot.getKeyboard(keyboards[0][2]));
                                }
                                case "Draw settings" -> {
                                    this.state += 3 << 4;
                                    bot.sendMessage(id, "Choose an interesting action", Bot.getKeyboard(keyboards[0][3]));
                                }
                                case "Turn off" -> {
                                    this.state -= 1 << 3;
                                    bot.sendMessage(id, "Choose an interesting subject", Bot.getKeyboard(keyboards[0][4]));
                                }
                                default -> bot.defaultMessage(id);
                            }
                        }
                    }
                }
            }
            //Выбираем язык
            default -> {
                switch (command.getText()) {
                    case "English \uD83C\uDDEC\uD83C\uDDE7" -> {
                        this.state += 1;
                        bot.sendMessage(id, "Choose one of interesting subject", Bot.getKeyboard(keyboards[0][4]));
                    }
                    case "Arabic \uD83C\uDDE6\uD83C\uDDEA" -> {
                        this.state += 2;
                        bot.sendMessage(id, "اختر الموضوع الذي تهتم به", Bot.getKeyboard(keyboards[1][4]));
                    }
                    case "Spanish \uD83C\uDDEA\uD83C\uDDF8" -> {
                        this.state += 3;
                        bot.sendMessage(id, "Seleccione un tema de interés", Bot.getKeyboard(keyboards[2][4]));
                    }
                    case "Chinese \uD83C\uDDE8\uD83C\uDDF3" -> {
                        this.state += 4;
                        bot.sendMessage(id, "选择一个感兴趣的主题", Bot.getKeyboard(keyboards[3][4]));
                    }
                    case "Russian \uD83C\uDDF7\uD83C\uDDFA" -> {
                        this.state += 5;
                        bot.sendMessage(id, "Выберите интересующую вас тему", Bot.getKeyboard(keyboards[4][4]));
                    }
                    case "French \uD83C\uDDEB\uD83C\uDDF7" -> {
                        this.state += 6;
                        bot.sendMessage(id, "Sélectionner un sujet d'intérêt", Bot.getKeyboard(keyboards[5][4]));
                    }
                    default -> {
                        bot.sendMessage(id, "Please, choose one of selected buttons");
                    }
                }
            }
        }
    }
}
