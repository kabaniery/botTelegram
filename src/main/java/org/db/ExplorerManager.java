package org.db;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ExplorerManager {
    public static class ChatGPTHistories {
        public static void deleteHistory(Long id) {
            try {
                Files.delete(Path.of("gpthistory/" + id.toString() + ".txt"));
            } catch (IOException e) {}
        }
    }
}
