package net.mangolise.kulorised.leaderboard;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.*;

public class GameTimeStorage {
    private final List<GameCompletion> data = new ArrayList<>();
    private final File dbFile = new File("completions.db");

    public GameTimeStorage() {
        try {
            boolean ignored = dbFile.createNewFile();

            // Read each line and parse
            String[] lines = Files.readAllLines(dbFile.toPath()).toArray(new String[0]);

            for (String line : lines) {
                String[] parts = line.split(" ");
                if (parts.length != 2) {
                    System.out.println("Invalid line in data file: " + line);
                    continue;
                }

                String[] playerNames = parts[0].split(",");
                long time = Long.parseLong(parts[1]);
                data.add(new GameCompletion(Set.of(playerNames), time));
            }
        } catch (IOException e) {
            // uhhh idk
            System.out.println("Failed to create new data file");
        }
    }

    public void addCompletion(GameCompletion game) {
        data.add(game);

        // Append the line
        try {
            Files.write(dbFile.toPath(), ("\n" + String.join(",", game.players()) + " " + game.time()).getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.out.println("Failed to write to data file");
        }
    }

    public List<GameCompletion> completions() {
        return Collections.unmodifiableList(data);
    }
}
