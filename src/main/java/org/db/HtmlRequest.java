package org.db;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class HtmlRequest {
    private static InputStream openConnection(String adress) {
        URL url = null;
        try {
            url = new URL(adress);
        } catch (MalformedURLException e) {
            System.out.println(e.getMessage());
            System.out.println("Error in creating url connection");
            return null;
        }
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            System.out.println(e.getMessage());
            System.out.println("Error in Open connection");
            return null;
        }
        try {
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-type", "application/json");
            connection.setConnectTimeout(2000);
            connection.setReadTimeout(2000);

        } catch (ProtocolException e) {
            System.out.println("Protocol error");
            System.out.println(e.getMessage());
            return null;
        }
        try {
            return connection.getInputStream();
        } catch (IOException e) {
            System.out.println("Last Error");
            System.out.println(e.getMessage());
            return null;
        }

    }
    private static String getFilePath(String token, String fileId) {
        InputStream stream = openConnection("https://api.telegram.org/bot"+token+"/getFile?file_id="+fileId);
        if (stream != null) {
            StringBuilder result = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            String line;
            try {
                while ((line = reader.readLine())!= null) {
                    result.append(line);
                }
            } catch (IOException e) {}
            JSONObject object = new JSONObject(result.toString());
            if (object.getBoolean("ok")) {
                return object.getJSONObject("result").getString("file_path");
            }
        }
        return null;
    }

    private static String downloadFile(String token, String path, String newPath) {
        try {
            InputStream in = new URL("https://api.telegram.org/file/bot" + token + "/" + path).openStream();
            Files.copy(in, Paths.get(newPath+"/"+path), StandardCopyOption.REPLACE_EXISTING);
            return newPath+"/"+path;
        } catch (IOException e) {
            System.out.println(e.getMessage());
            System.out.println("Can't get file...");
            return null;
        }
    }
    public static String getFile(String token, String fileId) {
        String path = getFilePath(token, fileId);
        if (path != null) {
            String newPath = downloadFile(token, path, "integrated");
            return newPath;
        }
        return null;
    }

    public static String getChatMember(Long chatId, Long userId, String token) {
        InputStream stream = openConnection("https://api.telegram.org/bot" + token + "/getChatMember?chat_id="+chatId+"&user_id="+userId);
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        try {
            JSONObject object = new JSONObject(reader.readLine());
            JSONObject resultObj = object.getJSONObject("result");
            return resultObj.getString("status");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
