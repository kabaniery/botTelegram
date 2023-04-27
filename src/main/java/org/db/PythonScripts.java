package org.db;

import javax.validation.constraints.NotNull;
import java.io.*;
import java.nio.Buffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class PythonScripts {
    private static int fileCode = 0;
    public static int getGenresCode(@NotNull String[] genres) {
        String filename = "scripts/temp" + String.valueOf(fileCode) + ".txt";
        fileCode++;
        try {
            FileWriter file = new FileWriter(filename);
            file.write(genres[0]);
            for (int i = 1; i < genres.length; ++i) {
                file.write("\n");
                file.write(genres[i]);
            }
            file.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        }
        try {
            Process p = Runtime.getRuntime().exec("py scripts/genresCode.py " + filename);
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            return Integer.parseInt(reader.readLine());
        } catch (IOException e) {
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        }
    }
    public static String[] getGPTMessages(Long id, String text) {
        try {
            BufferedWriter output = new BufferedWriter(new FileWriter("gpthistory/" + id.toString() + ".txt", true));
            output.append(text+"\n");
            output.close();
            try {
                Process p = Runtime.getRuntime().exec("py scripts/chatGPTScript.py " + "gpthistory/" + id.toString() + ".txt");
                BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String temp;
                StringBuilder builder = new StringBuilder();
                while ((temp = reader.readLine()) != null) {
                    builder.append(temp+"\n");
                }
                return builder.toString().split("\n");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
