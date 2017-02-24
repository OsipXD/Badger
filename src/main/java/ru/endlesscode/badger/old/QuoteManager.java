package ru.endlesscode.badger.old;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

/**
 * Created by OsipXD on 17.03.2016
 * It is part of the BadgerConsole.
 * All rights reserved 2014 - 2016 © «EndlessCode Group»
 */
public class QuoteManager {
    private final String fileName;
    private final List<String> quoteList = new ArrayList<>();

    public QuoteManager(String fileName) {
        this.fileName = fileName;
        this.parse();
    }

    private void parse() {
        try (Scanner in = new Scanner(new FileInputStream(new File("BadgerConsole", this.fileName)))) {
            String line;
            String quote = "";
            while (in.hasNextLine()) {
                line = in.nextLine();
                line = line.replaceAll("^\\s*", "");

                if (line.startsWith("#")) {
                    continue;
                }

                if (line.isEmpty()) {
                    if (!quote.isEmpty()) {
                        quoteList.add(quote);
                        quote = "";
                    }
                    continue;
                }

                if (!quote.isEmpty()) {
                    quote += "\n";
                }
                quote += line;
            }

            if (!quote.isEmpty()) {
                quoteList.add(quote);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public String getRandomQuote() {
        if (this.quoteList.size() == 0) {
            return "Нет ни одной цитаты в списке :(";
        }

        return this.quoteList.get(new Random().nextInt(this.quoteList.size() - 1));
    }
}
