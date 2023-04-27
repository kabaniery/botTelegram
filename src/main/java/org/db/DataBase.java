package org.db;

import org.example.Bot;
import org.session.UserSession;
import org.telegram.telegrambots.meta.api.objects.User;

import java.sql.*;
import java.util.*;

public class DataBase {
    private static class bookVal {
        public int count;
        public int id;
        public bookVal(int count, int id) {
            this.id = id;
            this.count = count;
        }
    }
    private static String getValueWithQuotes(String value) {
        return "\"" + value + "\"";
    }
    private static String formatTitle(String value) {
        String result = "";
        for (char elem: value.toCharArray()) {
            if (elem != '\"')
                result += elem;
        }
        return result;
    }

    private static String url = "jdbc:mysql://localhost:3306/botinformation";
    private static String name = "telegrambot";
    private static String password = "dZD3hKQs84ztnnun";
    private static String searchDB = "searchinfo";

    private static Connection con = null;
    private static Statement st = null;
    private static boolean safemode = false;

    private static int reconnect() {
        try {
            con = DriverManager.getConnection(url, name, password);
            st = con.createStatement();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            System.out.println(e.getErrorCode());
            return -1;
        }
        return 0;
    }
    public static class Draw {
        static Long id = 0L;
        long count;
        boolean isActive = false;
        public Draw() {
            if (st == null) {
                if (reconnect() == -1) {
                    return;
                }
            }
            try {
                id += 1;
                this.drawDB = "draw" + id.toString();
                st.executeUpdate("CREATE TABLE `" + drawDB + "` (\n" +
                        "  `id` BIGINT(20) NOT NULL,\n" +
                        "  `username` MEDIUMTEXT NULL,\n" +
                        "  PRIMARY KEY (`id`),\n" +
                        "  UNIQUE INDEX `id_UNIQUE` (`id` ASC) VISIBLE);");

            } catch (SQLException e) {
                System.out.println("Can't create a draw");
                System.out.println(e.getMessage());
                return;
            }
            this.count = 0;
            this.isActive = true;
        }
        public boolean getActive() {
            return this.isActive;
        }

        public int addNewUser(Long id, String username) {
            if (this.isActive) {
                if (st == null) {
                    if (reconnect() == -1) {
                        return -1;
                    }
                }
                try {
                    st.executeUpdate("insert into " + drawDB + "\n values (" + id.toString() + ", \"" + username + "\");");
                } catch (SQLException e) {
                    System.out.println(e.getMessage());
                    return -2;
                }
            }
            return 0;
        }

        public String getResults() {
            if (this.isActive) {
                if (st == null) {
                    if (reconnect() == -1) {
                        return null;
                    }
                }
                List<String> users = new ArrayList<>();
                int count = 0;
                try {
                    ResultSet rt = st.executeQuery("select username\n from " + drawDB);
                    while (rt.next()) {
                        users.add(rt.getNString("username"));
                        count++;
                    }
                } catch (SQLException e) {
                    this.isActive = false;
                    System.out.println("Error while getting results: " + e.getMessage());
                    return null;
                }
                try {
                    st.executeUpdate("drop table if exists " + drawDB + ";");
                } catch (SQLException e) {}
                this.isActive = false;
                return users.get((int) (Math.random() * count));
            }
            return null;
        }

        private String drawDB;

    }
    public static class Books {
        public static class Book {
            public int id;
            public String title;
            public int year;
            public String authors;
            public String genres;
            public String language;
            public String link;
            public int genresCode;
            public String description;
        }

        public static Book getBook(String id, int theme) {
            if (st == null) {
                if (DataBase.reconnect() == -1)
                    return null;
            }
            try {
                ResultSet rt = st.executeQuery("select *\n from " + booksDB[theme] + "\n where id = " + id);
                if (rt.next()) {
                    Book result = new Book();
                    result.id = rt.getInt("id");
                    result.title = rt.getNString("title");
                    result.authors = rt.getNString("authors");
                    result.description = rt.getNString("description");
                    result.genresCode = rt.getInt("genresCode");
                    result.genres = rt.getNString("genres");
                    result.language = rt.getNString("language");
                    result.link = rt.getNString("link");
                    result.year = rt.getInt("year");
                    return result;
                }
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
            return null;
        }


        private static final String[] booksDB = {null, "culture", "medicine", "books", "economic", "climate", "art"};

        //-1 means can't connect to the database; -2 means error in executing query
        public static int setNewBook(UserSession user, String[] values, int theme) {
            if (st == null) {
                if (DataBase.reconnect() == -1) {
                    return -1;
                }
            }
            if (user.isAdmin() && values.length == 7 && !values[1].equals("") && !values[6].equals("http://www.apple.com#")) {
                if (values[2].equals("не указано"))
                    values[2] = "-1";
                try {
                    st.executeUpdate("insert into " + booksDB[theme] + "\n values (NULL, " + getValueWithQuotes(formatTitle(values[1])) + ", " +
                            values[2] + ", " +
                            getValueWithQuotes(values[3]) + ", " +
                            getValueWithQuotes(values[4]) + ", " +
                            getValueWithQuotes(values[5]) + ", " +
                            getValueWithQuotes(values[6]) + ", " +
                            PythonScripts.getGenresCode(values[4].split(", ")) + ", " +
                            getValueWithQuotes("Genre - " + values[4] + "; Autors - " + values[3] + "; Year - " + values[2]) + ");");
                } catch (SQLException e) {
                    System.out.println("insert into " + booksDB[theme] + "\n values (NULL, " + getValueWithQuotes(values[1]) + ", " +
                            values[2] + ", " +
                            getValueWithQuotes(values[3]) + ", " +
                            getValueWithQuotes(values[4]) + ", " +
                            getValueWithQuotes(values[5]) + ", " +
                            getValueWithQuotes(values[6]) + ", " +
                            PythonScripts.getGenresCode(values[4].split(", ")) + ", " +
                            getValueWithQuotes("Genre - " + values[4] + "; Autors - " + values[3] + "; Year - " + values[2]) + ");");
                    System.out.println(e.getMessage());
                    System.out.println(e.getErrorCode());
                    return -2;
                }
            }
            return 0;
        }

        public static int[] getRecommendation(String[] genres, int theme) {
            int[] codes = new int[genres.length];
            int sum = 0;
            for (int i = 0; i < codes.length; ++i) {
                codes[i] = PythonScripts.getGenresCode(new String[]{genres[i]});
                sum += codes[i];
            }
            if (st == null) {
                if (DataBase.reconnect() == -1)
                    return null;
            }
            try {
                ResultSet rt = st.executeQuery("select id, genresCode\n from " + booksDB[theme]);
                bookVal[] mass = new bookVal[10];
                int index = 0;
                while (rt.next()) {
                    int code = rt.getInt("genresCode");
                    if ((code & sum) != 0) {
                        int count = 0;
                        for (int c : codes) {
                            if ((c & code) == c) {
                                count++;
                            }
                        }
                        if (index < 10) {
                            mass[index] = new bookVal(count, rt.getInt("id"));
                            index++;
                        } else {
                            Arrays.sort(mass, Comparator.comparingInt((bookVal elem) -> elem.count));
                            if (mass[0].count < count)
                                mass[0] = new bookVal(count, rt.getInt("id"));
                        }
                    }
                }
                if (index < 10) {
                    mass = Arrays.copyOf(mass, index + 1);
                }
                Arrays.sort(mass, (bookVal elem1, bookVal elem2) -> elem2.count - elem1.count);
                int[] result = new int[mass.length];
                for (int i = 0; i < mass.length; ++i) {
                    result[i] = mass[i].id;
                }
                return result;
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
            return null;
        }

        public static ArrayDeque<String[]> getBookByName(String title, int theme) {
            if (st == null) {
                if (DataBase.reconnect() == -1) {
                    return null;
                }
            }
            ArrayDeque<String[]> result = new ArrayDeque<>();
            try {
                ResultSet rt = st.executeQuery("select description, link\n from " + booksDB[theme] + "\n where title = \"" + title + "\";");
                if (rt.next()) {
                    result.add(new String[]{rt.getNString("description"), rt.getNString("link")});
                }
            } catch (SQLException e) {
                System.out.println(e.getMessage());
                System.out.println(e.getErrorCode());
            }
            try {
                ResultSet rt = st.executeQuery("select description, link\n from " + booksDB[theme] + "\n where id = " + title + ";");
                if (rt.next()) {
                    result.add(new String[]{rt.getNString("description"), rt.getNString("link")});
                }
            } catch (SQLException e) {
                System.out.println(e.getMessage());
                System.out.println(e.getErrorCode());
            }
            return result;
        }

        //Get name and description
        public static List<ArrayDeque<String>> getTitles(int theme) {
            if (st == null) {
                if (DataBase.reconnect() == -1) {
                    return null;
                }
            }
            try {
                ResultSet rt = st.executeQuery("select id, title, description\n from " + booksDB[theme]);
                ArrayDeque<String> tmp = new ArrayDeque<String>();
                List<ArrayDeque<String>> result = new ArrayList<ArrayDeque<String>>();
                if (rt.next()) {
                    tmp.add(rt.getInt("id") + ". " + rt.getNString("title"));
                    tmp.add(rt.getNString("description"));
                    result.add(tmp);
                    tmp = new ArrayDeque<String>();
                } else {
                    tmp.add("Error");
                    tmp.add("library is empty");
                    result.add(tmp);
                }
                while (rt.next()) {
                    tmp.add(rt.getInt("id") + ". " + rt.getNString("title"));
                    tmp.add(rt.getNString("description"));
                    result.add(tmp);
                    tmp = new ArrayDeque<String>();
                }
                return result;
            } catch (SQLException e) {
                System.out.println(e.getMessage());
                System.out.println(e.getErrorCode());
                return null;
            }
        }

        public static void rebaseTable(UserSession user, int theme) {
            if (user.isAdmin()) {
                if (st == null) {
                    if (DataBase.reconnect() == -1) {
                        return;
                    }
                }
                try {
                    //st.executeUpdate("drop table " + booksDB[theme]);
                    st.executeUpdate("CREATE TABLE `botinformation`.`" + booksDB[theme] + "` (\n" +
                            "  `id` INT NOT NULL AUTO_INCREMENT,\n" +
                            "  `title` VARCHAR(200) NULL,\n" +
                            "  `year` INT NULL,\n" +
                            "  `authors` LONGTEXT NULL,\n" +
                            "  `genres` MEDIUMTEXT NULL,\n" +
                            "  `language` VARCHAR(45) NULL,\n" +
                            "  `link` MEDIUMTEXT NULL,\n" +
                            "  `genresCode` INT NULL,\n" +
                            "  `description` LONGTEXT NULL,\n" +
                            "  PRIMARY KEY (`id`),\n" +
                            "  UNIQUE INDEX `id_UNIQUE` (`id` ASC) VISIBLE);");
                } catch (SQLException e) {
                    System.out.println("Can't refresh base");
                    System.out.println(e.getMessage());
                }
            }
        }
    }
    public static class Admins {
        private static final long majorAdmin = 1659875204;
        public static void Warn(Bot bot, String command, String id) {
            bot.sendMessage(majorAdmin, "User " + id + " used a command: " + command);
        }
        public static List<String> getAdmins(UserSession user) {
            if (user.isAdmin()) {
                if (st == null) {
                    if (reconnect() != 0) {
                        return null;
                    }
                }
                try {
                    ResultSet rt = st.executeQuery("select *\n from adminusers");
                    List<String> result = new ArrayList<>();
                    while (rt.next()) {
                        result.add(String.valueOf(rt.getInt(1)));
                    }
                    return result;
                } catch (SQLException e) {
                    System.out.println(e.getMessage());
                }
            }
            return null;
        }
        public static void removeAdmin(UserSession user, long id) {
            if (id == majorAdmin) {
                return;
            }
            if (user.isAdmin()) {
                if (st == null) {
                    if (reconnect() != 0) {
                        return;
                    }
                }
                try {
                    st.executeUpdate("delete\n from adminusers\n where id =" + Long.valueOf(id).toString());
                } catch (SQLException e) {
                    System.out.println(e.getMessage());
                }
            }
        }

        public static void addNewAdmin(UserSession user, long id) {
            if (user.isAdmin()) {
                if (st == null) {
                    if (reconnect() != 0) {
                        return;
                    }
                }
                try {
                    st.executeUpdate("insert into adminusers\n values (" + Long.valueOf(id).toString() + ");");
                } catch (SQLException e) {
                    System.out.println(e.getMessage());
                }
            }
        }
    }

    public DataBase() {
        try {
            con = DriverManager.getConnection(url, name, password);
            st = con.createStatement();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            System.out.println(e.getErrorCode());
        }
    }

    public String getValueByQuery(String name) {
        ResultSet rt = null;
        try {
            rt = st.executeQuery("select value\n from " + searchDB + "\n where nameOf = \"" + name + "\";");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            System.out.println(e.getErrorCode());
            return null;
        }
        try {
            StringBuilder result = new StringBuilder();
            while (rt.next()) {
                result.append(rt.getNString("value"));
            }
            return result.toString();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            System.out.println(e.getErrorCode());
            return null;
        }
    }

    public List<String> getArticlesTitle() {
        ResultSet rt = null;
        try {
            rt = st.executeQuery("select nameof\n from "+searchDB);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            System.out.println(e.getErrorCode());
            return null;
        }
        List<String> res = new ArrayList<String>();
        try {
            while (rt.next()) {
                res.add(rt.getNString(1));
            }
            return res;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            System.out.println(e.getErrorCode());
            return null;
        }
    }

    public static boolean isAdmin(Long id) {
        ResultSet rt = null;
        try {
            rt = st.executeQuery("select id\n from adminusers\n where id = " + id.toString());
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            System.out.println(e.getErrorCode());
        }
        try {
            boolean result = false;
            while (rt.next()) {
                result = true;
            }
            return result;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            System.out.println(e.getErrorCode());
        }
        return false;
    }
}