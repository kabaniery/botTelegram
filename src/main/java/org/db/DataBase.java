package org.db;

import org.example.Bot;
import org.session.UserSession;
import org.telegram.telegrambots.meta.api.objects.User;

import java.sql.*;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

public class DataBase {
    private static String url = "jdbc:mysql://localhost:3306/botinformation";
    private static String name = "root";
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
                this.drawDB = "draw" + id.toString();
                st.executeUpdate("CREATE TABLE `" + drawDB + "` (\n" +
                        "  `id` BIGINT(20) NOT NULL,\n" +
                        "  `username` MEDIUMTEXT NULL,\n" +
                        "  PRIMARY KEY (`id`),\n" +
                        "  UNIQUE INDEX `id_UNIQUE` (`id` ASC) VISIBLE);");
                id += 1;
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
        private static String booksDB = "books";
        //-1 means can't connect to the database; -2 means error in executing query
        public static int setNewBook(UserSession user, String[] values) {
            if (st == null) {
                if (DataBase.reconnect() == -1) {
                    return -1;
                }
            }
            if (user.isAdmin() && values.length == 7 && !values[1].equals("") && !values[6].equals("http://www.apple.com#")) {
                try {
                        st.executeUpdate("insert into " + booksDB + "\n values (NULL, \"" + values[1] + "\", \"" +("Genre - " + values[4] + "; Autors - " + values[3] + "; Year - " + values[2])+ "\", \"" + values[6] + "\");");
                } catch (SQLException e) {
                    System.out.println(e.getMessage());
                    System.out.println(e.getErrorCode());
                    return -2;
                }
            }
            return 0;
        }

        public static ArrayDeque<String[]> getBookByName(String title) {
            if (st == null) {
                if (DataBase.reconnect() == -1) {
                    return null;
                }
            }
            ArrayDeque<String[]> result = new ArrayDeque<>();
            try {
                ResultSet rt = st.executeQuery("select description, link\n from " + booksDB + "\n where title = \"" + title + "\";");
                if (rt.next()) {
                    result.add(new String[]{rt.getNString("description"), rt.getNString("link")});
                }
            } catch (SQLException e) {
                System.out.println(e.getMessage());
                System.out.println(e.getErrorCode());
            }
            try {
                ResultSet rt = st.executeQuery("select description, link\n from " + booksDB + "\n where id = " + title + ";");
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
        public static List<ArrayDeque<String>> getTitles() {
            if (st == null) {
                if (DataBase.reconnect() == -1) {
                    return null;
                }
            }
            try {
                ResultSet rt = st.executeQuery("select id, title, description\n from " + booksDB);
                ArrayDeque<String> tmp = new ArrayDeque<String>();
                List<ArrayDeque<String>> result = new ArrayList<ArrayDeque<String>>();
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

        public static void rebaseTable(UserSession user) {
            if (user.isAdmin()) {
                if (st == null) {
                    if (DataBase.reconnect() == -1) {
                        return;
                    }
                }
                try {
                    st.executeUpdate("drop table " + booksDB);
                    st.executeUpdate("CREATE TABLE `botinformation`.`" + booksDB + "` (\n" +
                            "  `id` INT NOT NULL AUTO_INCREMENT,\n" +
                            "  `title` VARCHAR(200) NULL,\n" +
                            "  `description` LONGTEXT NULL,\n" +
                            "  `link` MEDIUMTEXT NULL,\n" +
                            "  PRIMARY KEY (`id`));\n");
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
