package ru.endlesscode.badger.old;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Created by OsipXD on 14.03.2016
 * It is part of the BadgerConsole.
 * All rights reserved 2014 - 2016 © «EndlessCode Group»
 */
public class EntryManager {
    private final String inputFile;
    private final List<Entry> entryList = new ArrayList<>();

    public EntryManager(String inputFile) {
        this.inputFile = inputFile;
        this.parse();
    }

    public void parse() {
        entryList.clear();
        try (Scanner in = new Scanner(new FileInputStream(new File("BadgerConsole", this.inputFile)))) {
            String line;
            int counter = 0;
            while (in.hasNextLine()) {
                try {
                    line = in.nextLine();
                    line = line.replaceAll("^\\s*", "");

                    if (line.startsWith("#") || line.isEmpty()) {
                        continue;
                    }

                    String[] splittedData = line.split(" - ");
                    String data = line.split(" - ")[0];
                    String quote = null;
                    if (splittedData.length == 2) {
                        quote = line.split(" - ")[1];
                    }

                    splittedData = data.split(" : ");
                    if (splittedData.length < 2 || splittedData.length > 3) {
                        throw new IllegalArgumentException("Неверный формат строки \"" + line + "\"");
                    }

                    String fullName = splittedData[0];
                    String type = splittedData[1];
                    String addInfo = null;
                    if (splittedData.length == 3) {
                        addInfo = splittedData[2];
                    }

                    splittedData = fullName.split(" ");
                    if (splittedData.length > 3) {
                        throw new IllegalArgumentException("Неверный формат строки \"" + line + "\"");
                    }
                    String surname = splittedData[0];
                    String name = null;
                    String patronymic = null;

                    if (splittedData.length > 1) {
                        name = splittedData[1];
                    }

                    if (splittedData.length == 3) {
                        patronymic = splittedData[2];
                    }

                    this.entryList.add(new Entry(++counter, surname, name, patronymic, type, addInfo, quote));
                } catch (IllegalArgumentException e) {
                    System.out.println(e.getMessage() + " - Строка будет пропущена!");
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public List<Entry> getEntryList() {
        return this.entryList;
    }
}
