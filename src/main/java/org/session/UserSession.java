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
import java.util.List;

public class UserSession {
    Long id;
    Bot bot;

    //New system

    //Admin's keyboard
    private static final String[][] defaultAdminKb = {{"Library management"}, {"Admins panel"}, {"Draw settings"}, {"Turn off"}};
    private static final String[][] adminBookKb = {{"Add new table"}, {"Format library"}, {"Back"}};
    private static final String[][] adminAdminsKb = {{"Add new admin", "Delete existing admin"}, {"Get list of admins"}, {"Back"}};
    private static final String[][] adminDrawKb = {{"Start draw", "Stop draw"}, {"Back"}};
    //User's keyboard
    private static final String[][] defaulUserKb = {{"Go to library"}, {"ChatGPT", "Other"}};
    private static final String[][] libraryThemes = {{"Культура", "Медицина"}, {"Наука", "Экономика"}, {"Климат", "Искусство"}, {"Back"}};
    private static final String[][] userBookKb = {{"Find book", "Recommend book"}, {"Get library"}, {"Back"}};
    private static final String[][] userOtherKb = {{"Donations"}, {"Buy ADS"}, {"Back"}};
    private static final String[][] userChatgptKb = {{"Quit ChatGPT dialog /\\/\\"}, {"/Clear bot history"}};

    private int state;
    public boolean isAdmin() {
        return (state & 1) == 1;
    }
    public UserSession(Long id, boolean isAdmin, Bot bot)
    {
        this.id = id;
        this.bot = bot;
        if (isAdmin && DataBase.isAdmin(id)) {
            this.state = 64;
            Message msg = new Message();
            msg.setText("/admin");
            this.update(msg);
            return;
        }
        if (isAdmin) {
            bot.sendMessage(id, "Hello, I am Ginzburg foundation chat bot. Use \"Start draw\" and \"Get result\" to activate/deactivate draws");
        } else {
            bot.sendMessage(id, "Hello, I am Ginzburg foundation bot. What do you want to learn about?", Bot.getKeyboard(defaulUserKb));
        }
        this.state = 0;
    }



    public void update(Message command) {
        System.out.println(this.state);
        if ((this.state & 1) == 1) {
            switch(((this.state >> 1) & 7)) {
                case 1 -> {
                    if (((this.state >> 4) & 7) == 0) {
                        switch (command.getText()) {
                            case "Культура":
                                this.state += (1 << 4);
                                break;
                            case "Медицина":
                                this.state += (2 << 4);
                                break;
                            case "Наука":
                                this.state += (3 << 4);
                                break;
                            case "Экономика":
                                this.state += (4 << 4);
                                break;
                            case "Климат":
                                this.state += (5 << 4);
                                break;
                            case "Искусство":
                                this.state += (6 << 4);
                                break;
                            case "Back":
                                this.state = 1;
                        }
                        //TODO:Удалить, как добавят новые бд
                        if (this.state == 1) {
                            bot.sendMessage(id, "Type a command", Bot.getKeyboard(defaultAdminKb));
                        } /*else if (((this.state >> 4) & 7) != 3) {
                            bot.sendMessage(id, "On developing \uD83D\uDEE0", Bot.getKeyboard(libraryThemes));
                            this.state -= ((this.state >> 4) & 7) << 4;
                        } */else {
                            bot.sendMessage(id, "What do you learn about?", Bot.getKeyboard(adminBookKb));
                        }
                    } else {
                        switch ((this.state >> 7) & 7) {
                            case 1 -> {
                                this.state -= 1 << 7;
                                String path = HtmlRequest.getFile(bot.getBotToken(), command.getDocument().getFileId());
                                System.out.println("asv");
                                if (path != null) {
                                    try {
                                        CSVReader reader = new CSVReader(new FileReader(path));
                                        String[] line;
                                        reader.readNext();
                                        reader.readNext();
                                        while ((line = reader.readNext()) != null) {
                                            DataBase.Books.setNewBook(this, line, (this.state >> 4) & 7);
                                        }
                                    } catch (CsvValidationException | IOException e) {
                                        System.out.println("Error while updating file");
                                        System.out.println(e.getMessage());
                                    }
                                    try {
                                        Files.delete(Path.of(path));
                                    } catch (IOException e) {}
                                    bot.sendMessage(id, "Success!", Bot.getKeyboard(adminBookKb));
                                }
                                bot.sendMessage(id, "Something went wrong");
                            }
                            case 2 -> {
                                this.state -= 2 << 7;
                                if (command.getText().equals("Yes"))
                                    DataBase.Books.rebaseTable(this, (this.state >> 4) & 7);
                            }
                            default -> {
                                switch (command.getText()) {
                                    case "Add new table" -> {
                                        this.state += 1 << 7;
                                        bot.sendMessage(id, "Send table");
                                    }
                                    case "Format library" -> {
                                        this.state += 2 << 7;
                                        bot.sendMessage(id, "Are you sure? Type \"Yes\" to format library");
                                    }
                                    case "Back" -> {
                                        this.state = 1 + (1 << 3);
                                        bot.sendMessage(id, "Type command", Bot.getKeyboard(defaultAdminKb));
                                    }
                                    default -> {
                                        bot.defaultMessage(id);
                                    }
                                }
                            }
                    }

                    }
                }
                case 2 -> {
                    switch ((this.state >> 4) & 7) {
                        case 1 -> {
                            this.state = 1 + (2 << 1);
                            DataBase.Admins.addNewAdmin(this, Long.parseLong(command.getText()));
                        }
                        case 2 -> {
                            this.state = 1 + (2 << 1);
                            DataBase.Admins.removeAdmin(this, Long.parseLong(command.getText()));
                        }
                        default -> {
                            switch (command.getText()) {
                                case "Add new admin" -> {
                                    this.state = 1 + (2 << 1) + (1 << 4);
                                    bot.sendMessage(id, "Write his id");
                                }
                                case "Delete existing admin" -> {
                                    this.state = 1 + (2 << 1) + (2 << 4);
                                    bot.sendMessage(id, "Write his id");
                                }
                                case "Get list of admins" -> {
                                    List<String> val = DataBase.Admins.getAdmins(this);
                                    if (val != null)
                                        bot.SendMessages(id, val, Bot.getKeyboard(adminAdminsKb));
                                }
                                case "Back" -> {
                                    this.state = 1;
                                    bot.sendMessage(id, "Type command", Bot.getKeyboard(defaultAdminKb));
                                }
                                default -> {
                                    bot.defaultMessage(id);
                                }
                            }
                        }
                    }
                }
                case 3 -> {
                    switch (command.getText()) {
                        case "Start draw" -> {
                            bot.draw = new DataBase.Draw();
                        }
                        case "Stop draw" -> {
                            bot.sendMessage(id, "Winner of draw is: " + bot.draw.getResults());
                        }
                        case "Back" -> {
                            this.state = 1;
                            bot.sendMessage(id, "Type command", Bot.getKeyboard(defaultAdminKb));
                        }
                        default -> {
                            bot.defaultMessage(id);
                        }
                    }
                }
                default -> {
                    switch (command.getText()) {
                        case "Library management" -> {
                            this.state = 1 + (1 << 1);
                            bot.sendMessage(id, "Choose a theme of library", Bot.getKeyboard(libraryThemes));
                        }
                        case "Admins panel" -> {
                            this.state = 1 + (2 << 1);
                            bot.sendMessage(id, "Be careful on editing this case", Bot.getKeyboard(adminAdminsKb));
                        }
                        case "Draw settings" -> {
                            this.state = 1 + (3 << 1);
                            bot.sendMessage(id, "Type action", Bot.getKeyboard(adminDrawKb));
                        }
                        case "Turn off" -> {
                            this.state = 0;
                            bot.sendMessage(id, "Touch button to interact", Bot.getKeyboard(defaulUserKb));
                        }
                        default -> {
                            bot.defaultMessage(id);
                        }
                    }
                }
            }
        }
        else {
            switch ((this.state >> 1) & 7) {
                case 1 -> {
                    if (((this.state >> 4) & 7) == 0) {
                        switch (command.getText()) {
                            case "Культура" -> this.state += (1 << 4);
                            case "Медицина" -> this.state += (2 << 4);
                            case "Наука" -> this.state += (3 << 4);
                            case "Экономика" -> this.state += (4 << 4);
                            case "Климат" -> this.state += (5 << 4);
                            case "Искусство" -> this.state += (6 << 4);
                            case "Back" -> this.state = 0;
                        }
                        //TODO:Удалить, как добавят новые бд
                        if (this.state == 0) {
                            bot.sendMessage(id, "Type a command", Bot.getKeyboard(defaulUserKb));
                        } /*else if (((this.state >> 4) & 7) != 3) {
                            bot.sendMessage(id, "On developing \uD83D\uDEE0", Bot.getKeyboard(libraryThemes));
                            this.state -= ((this.state >> 4) & 7) << 4;
                        } */else {
                            bot.sendMessage(id, "What do you learn about?", Bot.getKeyboard(userBookKb));
                        }
                    } else {

                        switch ((this.state >> 11) & 7) {
                            case 1 -> {
                                this.state -= (1 << 11);
                                ArrayDeque<String[]> result = DataBase.Books.getBookByName(command.getText(), (this.state >> 4) & 7);
                                if (result != null && !result.isEmpty()) {
                                    for (String[] elem : result) {
                                        bot.createAndSendMessage(id, elem[0] + "\n Link for downloading: " + elem[1], Bot.getKeyboard(userBookKb));
                                    }
                                } else {
                                    bot.sendMessage(id, "There is no results...", Bot.getKeyboard(userBookKb));
                                }
                            }
                            case 2 -> {
                                this.state -= (2 << 11);
                                DataBase.Books.Book book = DataBase.Books.getBook(command.getText(), (this.state >> 4) & 7);
                                int[] res;
                                if (book != null) {
                                    res = DataBase.Books.getRecommendation(book.genres.split(", "), (this.state >> 4) & 7);
                                } else {
                                    res = DataBase.Books.getRecommendation(new String[]{command.getText()}, (this.state >> 4) & 7);
                                }
                                if (res != null) {
                                    DataBase.Books.Book temp;
                                    for (int i : res) {
                                        temp = DataBase.Books.getBook(String.valueOf(i), (this.state >> 4) & 7);
                                        bot.sendMessage(id, temp.title + ". " + temp.description, Bot.getKeyboard(userBookKb));
                                    }
                                } else
                                    bot.sendMessage(id, "There is no recommendation for you...");
                            }
                            default -> {
                                switch (command.getText()) {
                                    case "Find book" -> {
                                        state += (1 << 11);
                                        bot.sendMessage(id, "Write title or id of book");
                                    }
                                    case "Recommend book" -> {
                                        state += (2 << 11);
                                        bot.sendMessage(id, "Write id of book or genre");
                                    }
                                    case "Get library" -> {
                                        List<ArrayDeque<String>> result = DataBase.Books.getTitles((this.state >> 4) & 7);
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
                                                    bot.sendMessage(id, message.toString());
                                                    currentLength = line.length();
                                                    message = new StringBuilder(line);
                                                }
                                            }
                                            if (!message.isEmpty()) {
                                                bot.sendMessage(id, message.toString());
                                            }
                                        }
                                    }
                                    case "Back" -> {
                                        state = (1 << 1);
                                        bot.sendMessage(id, "Touch needed command", Bot.getKeyboard(libraryThemes));
                                    }
                                    default -> {
                                        bot.defaultMessage(id);
                                    }
                                }
                            }
                        }
                    }
                }
                case 2 -> {
                    switch (command.getText()) {
                        case "Donations" -> {
                            bot.sendMessage(id, "We rely on the generosity of people like you to support our mission. Your donation will make a real difference in the lives of those we serve. Thank you for your support. \n Crypto wallet for donations (etherium): 0x01D429C88Acf17C719a258162671e8c8461a3D5C");
                        }
                        case "Buy ADS" -> {
                            bot.sendMessage(id, "Please write to the administrator @kabaniery to publish advertising");
                        }
                        case "Back" -> {
                            state = 0;
                            bot.sendMessage(id, "Type the command", Bot.getKeyboard(defaulUserKb));
                        }
                        default -> {
                            bot.defaultMessage(id);
                        }
                    }
                }
                case 3 -> {
                    switch (command.getText()) {
                        case "Quit ChatGPT dialog /\\/\\":
                            this.state = 0;
                            bot.sendMessage(id, "Type the command", Bot.getKeyboard(defaulUserKb));
                            break;
                        case "/Clear bot history":
                            // Очистить сообщения бота
                            ExplorerManager.ChatGPTHistories.deleteHistory(id);
                            break;
                        default:
                            bot.sendMessage(id, "ChatGPT is thinking about answer to your message...");
                            //Получить список сообщений и вывести результат
                            bot.createAndSendMessage(id, List.of(PythonScripts.getGPTMessages(id, command.getText())), command.getMessageId());

                    }
                }
                default -> {
                    switch (command.getText()) {
                        case "Go to library":
                            state = 1 << 1;
                            bot.sendMessage(id, "Please, choose a theme", Bot.getKeyboard(libraryThemes));
                            break;
                        case "ChatGPT":
                            state = 3 << 1;
                            bot.sendMessage(id, "Welcome to ChatGPT helper. Ask something me", Bot.getKeyboard(userChatgptKb));
                            break;
                        case "Other":
                            state = 2 << 1;
                            bot.sendMessage(id, "Bot created by dyda0505", Bot.getKeyboard(userOtherKb));
                            break;
                        case "/admin":
                            if (DataBase.isAdmin(id)) {
                                this.state = 1;
                                bot.sendMessage(id, "Welcome back", Bot.getKeyboard(defaultAdminKb));
                                break;
                            }
                        default:
                            bot.defaultMessage(id);
                    }
                }
            }
        }
    }
}
