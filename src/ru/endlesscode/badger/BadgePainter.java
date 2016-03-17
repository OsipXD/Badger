package ru.endlesscode.badger;

import ru.endlesscode.badger.misc.Config;
import ru.endlesscode.badger.misc.Log;
import ru.endlesscode.badger.misc.ProgressBar;
import ru.endlesscode.badger.misc.Timer;
import ru.endlesscode.badger.utils.ImageRotateUtil;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.List;

/**
 * Created by OsipXD on 14.03.2016
 * It is part of the Badger.
 * All rights reserved 2014 - 2016 © «EndlessCode Group»
 */
public class BadgePainter {
    public static void drawBadge(Entry entry) throws IOException {
        BufferedImage pattern = ImageIO.read(new File("Badger/res/images", entry.getType().name() + ".png"));
        Graphics2D g2d = pattern.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        Rectangle addInfoArea = null;
        Rectangle nameArea;
        int nameAreaHeight = (int)(Config.TEXT_AREA_HEIGHT * 0.7);

        if (entry.hasAddInfo()) {
            int addInfoHeight = nameAreaHeight / 4;
            addInfoArea = new Rectangle(Config.TEXT_AREA_X, Config.TEXT_AREA_Y, Config.TEXT_AREA_WIDTH, addInfoHeight);
            nameArea = new Rectangle(Config.TEXT_AREA_X, Config.TEXT_AREA_Y + addInfoHeight, Config.TEXT_AREA_WIDTH, nameAreaHeight - addInfoHeight);
        } else {
            nameArea = new Rectangle(Config.TEXT_AREA_X, Config.TEXT_AREA_Y, Config.TEXT_AREA_WIDTH, nameAreaHeight);
        }

        if (entry.hasAddInfo()) {
            g2d.setColor(Color.DARK_GRAY);
            drawCentredText(g2d, addInfoArea, entry.getAddInfo(), Config.NAME_FONT, 25);
        }

        g2d.setColor(Color.BLACK);
        drawName(g2d, nameArea, entry);

        Rectangle quoteArea = new Rectangle(Config.TEXT_AREA_X, nameArea.y + nameArea.height, Config.TEXT_AREA_WIDTH, Config.TEXT_AREA_Y + Config.TEXT_AREA_HEIGHT - nameArea.y - nameArea.height);
        drawQuote(g2d, quoteArea, entry);
        File photoFile = new File("Badger/temp", entry.getFileName() + ".jpg");
        Image photo = ImageIO.read(photoFile);
        g2d.drawImage(photo, Config.PHOTO_X, Config.PHOTO_Y, null);
        g2d.dispose();

        ImageIO.write(pattern, "png", new File("Badger/temp/badges", entry.getFileName() + ".png"));
//        photoFile.delete();
    }

    private static void drawQuote(Graphics2D g2d, Rectangle quoteArea, Entry entry) {
        String quote = "\"";
        if (entry.hasQuote()) {
            quote += entry.getQuote();
        } else {
            quote += Badger.getQuoteManager().getRandomQuote();
        }
        quote += "\"";

        //noinspection ConstantConditions
        AttributedString text = new AttributedString(quote);
        text.addAttribute(TextAttribute.FAMILY, Config.QUOTE_FONT);

        int height;
        float size = Config.QUOTE_MIN_SIZE;
        Font font = new Font(Config.QUOTE_FONT, Font.PLAIN, (int) size);
        FontMetrics metrics;

        while (true) {
            Font tempFont = font.deriveFont(size);
            metrics = g2d.getFontMetrics(tempFont);
            text.addAttribute(TextAttribute.SIZE, size);

            AttributedCharacterIterator paragraph = text.getIterator();
            //int paragraphStart = paragraph.getBeginIndex();
            int paragraphEnd = paragraph.getEndIndex();
            FontRenderContext frc = g2d.getFontRenderContext();
            LineBreakMeasurer lineMeasurer = new LineBreakMeasurer(paragraph, frc);

            int lineCounter = 0;
            int pos;
            while ((pos = lineMeasurer.getPosition()) < paragraphEnd) {
                int index = quote.indexOf("\n", pos);
                if (index > pos) {
                    lineMeasurer.nextLayout(quoteArea.width, index, false);
                } else {
                    lineMeasurer.nextLayout(quoteArea.width);
                }

                lineCounter++;
            }

            if (lineCounter * metrics.getHeight() > quoteArea.height || size > Config.QUOTE_MAX_SIZE) {
                metrics = g2d.getFontMetrics(font);
                height = lineCounter * metrics.getHeight();
                size--;
                break;
            }

            size++;
            font = tempFont;
        }
        text.addAttribute(TextAttribute.SIZE, size);

        AttributedCharacterIterator paragraph = text.getIterator();
        //int paragraphStart = paragraph.getBeginIndex();
        int paragraphEnd = paragraph.getEndIndex();
        FontRenderContext frc = g2d.getFontRenderContext();
        LineBreakMeasurer lineMeasurer = new LineBreakMeasurer(paragraph, frc);

        float posX;
        float posY = quoteArea.y + (quoteArea.height - height) / 2 + metrics.getHeight();
        int pos;
        while ((pos = lineMeasurer.getPosition()) < paragraphEnd) {
            TextLayout layout;
            int index = quote.indexOf("\n", pos);
            layout = index > pos ? lineMeasurer.nextLayout(quoteArea.width, index, false) : lineMeasurer.nextLayout(quoteArea.width);
            posX = quoteArea.x + (quoteArea.width - layout.getVisibleAdvance()) / 2;
            layout.draw(g2d, posX, posY);

            posY += metrics.getHeight();
        }
    }

    private static void drawName(Graphics g, Rectangle nameArea, Entry entry) {
        int surnameSize = Config.NAME_MIN_SIZE;
        int nameSize = Config.NAME_MIN_SIZE;
        FontMetrics metrics;

        String name = entry.getName();
        if (entry.hasPatronymic()) {
            name += " " + entry.getPatronymic();
        }

        Font nameFont = new Font(Config.NAME_FONT, Font.PLAIN, nameSize);

        // Обрабатываем случай, когда у нас только имя
        if (!entry.hasSurname()) {
            nameFont = getSizedFontFromHigh(g, nameArea.getSize(), name, nameFont.getFontName(), Config.NAME_MAX_SIZE);
            metrics = g.getFontMetrics(nameFont);
            int x = nameArea.x + (nameArea.width - metrics.stringWidth(name)) / 2;
            int y = nameArea.y + nameArea.height - (nameArea.height - metrics.getHeight() + metrics.getAscent()) / 2;

            g.setFont(nameFont);
            g.drawString(name, x, y);
            nameArea.setSize(nameArea.width, nameArea.height - y + nameArea.y);
            return;
        }

        nameFont = getSizedFontFromHigh(g, nameArea.getSize(), name, nameFont.getFontName(), nameSize);
        metrics = g.getFontMetrics(nameFont);
        int nameHeight = metrics.getHeight();

        String surname = entry.getSurname();
        Font surnameFont = getSizedFontFromHigh(g, new Dimension(nameArea.width, nameArea.height - nameHeight), surname,
                nameFont.getFontName(), surnameSize);
        metrics = g.getFontMetrics(surnameFont);
        int surnameWidth = metrics.stringWidth(surname);
        int surnameHeight = metrics.getHeight();
        int surnameAscent = metrics.getAscent();

        nameFont = getSizedFontFromLow(g, new Dimension(nameArea.width, nameArea.height - surnameHeight), name,
                nameFont.getFontName(), nameFont.getSize());
        metrics = g.getFontMetrics(nameFont);
        nameHeight = metrics.getHeight();
        int nameWidth = metrics.stringWidth(name);

        int surnameX = nameArea.x + (nameArea.width - surnameWidth) / 2;
        int surnameY = nameArea.y + nameArea.height - (nameArea.height - nameHeight - surnameHeight + surnameAscent) / 2 + surnameAscent / 4;
        int nameX = nameArea.x + (nameArea.width - nameWidth) / 2 ;
        int nameY = surnameY - surnameHeight - surnameAscent / 4;

        g.setFont(surnameFont);
        g.drawString(surname, surnameX, surnameY);

        g.setFont(nameFont);
        g.drawString(name, nameX, nameY);

        nameArea.setSize(nameArea.width, surnameY - nameArea.y + surnameAscent / 2);
    }

    private static void drawCentredText(Graphics g, Rectangle area, String text, String fontName, int startSize) {
        int height;
        int width;

        Font font = getSizedFontFromLow(g, area.getSize(), text, fontName, startSize);
        FontMetrics metrics = g.getFontMetrics(font);
        height = metrics.getHeight();
        width = metrics.stringWidth(text);

        int x = area.x + (area.width - width) / 2;
        int y = area.y + area.height - (area.height - height + metrics.getAscent()) / 2;
        g.setFont(font);
        g.drawString(text, x, y);
    }

    private static Font getSizedFontFromLow(Graphics g, Dimension area, String text, String fontName, int lowSize) {
        float fontSize = lowSize;
        int height;
        int width;
        FontMetrics metrics;
        Font font = new Font(fontName, Font.PLAIN, lowSize);

        while(true) {
            Font tempFont = font.deriveFont(fontSize);
            metrics = g.getFontMetrics(tempFont);

            height = metrics.getHeight();
            width = metrics.stringWidth(text);
            if (height > area.height || width > area.width) {
                break;
            }

            font = tempFont;
            fontSize++;
        }

        return font;
    }

    private static Font getSizedFontFromHigh(Graphics g, Dimension area, String text, String fontName, int highSize) {
        return getSizedFontFromHigh(g, area, text, fontName, highSize, 0);
    }

    private static Font getSizedFontFromHigh(Graphics g, Dimension area, String text, String fontName, int highSize, int addHeight) {
        Font font = new Font(fontName, Font.PLAIN, highSize);

        FontMetrics metrics = g.getFontMetrics(font);
        int height = metrics.getHeight();
        int width = metrics.stringWidth(text);
        float size = highSize;

        while (width > area.width || height + addHeight > area.height) {
            size--;
            font = font.deriveFont(size);
            height = metrics.getHeight();
            width = metrics.stringWidth(text);
        }

        return font;
    }

    public static void initFonts() {
        File fontFolder = new File("Badger/res/fonts");

        System.out.println("Подгрузка пользовательских шрифтов...");
        if (!fontFolder.exists() || fontFolder.isFile()) {
            System.out.println("Папка \"fonts\" не найдена.");
            return;
        }

        File[] fontFiles = fontFolder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".ttf") || name.endsWith(".TTF");
            }
        });

        if (fontFiles.length == 0) {
            System.out.println("Папка \"fonts\" пуста.");
            return;
        }

        int counter = 0;
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        for (File fontFile : fontFiles) {
            try {
                ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, fontFile));
                counter++;
            } catch (IOException|FontFormatException e) {
                System.out.println("Ошибка при загрузке шрифта \"" + fontFile.getName() + "\": " + e.getMessage());
            }
        }
        System.out.println("Шрифты успешно загружены (" + counter + " шт.)");
    }

    public static void drawAllBadges() {
        List<Entry> entries = Badger.getEntryManager().getEntryList();
        ProgressBar progressBar = new ProgressBar("Составление бейджиков", entries.size());
        progressBar.start();

        Timer timer = new Timer();
        timer.start();

        for (Entry entry : entries) {
            try {
                drawBadge(entry);
            } catch (IOException e) {
                Log.getLogger().warning("BadgePainter#drawAllBadges(): " + e.getMessage());
            } finally {
                progressBar.increaseProgress();
            }
        }

        progressBar.pause("Бейджи составлены (" + timer.stop() + " s)");
        System.out.println("Проверьте правильность их составления в папке \"temp/badges\" и исправьте при необходимости.");
        Badger.waitEnter();
    }

    public static void packBadgesToPrint() {
        List<Entry> entries = Badger.getEntryManager().getEntryList();
        ProgressBar progressBar = new ProgressBar("Сборка бейджей на печать", entries.size());
        progressBar.start();
        Timer timer = new Timer();
        timer.start();

        int counter = 0;
        int pageCounter = 0;
        BufferedImage badgePack = new BufferedImage(
                (Config.BADGE_HEIGHT + Config.PAGE_SPACE) * 3 - Config.PAGE_SPACE, (Config.BADGE_WIDTH + Config.PAGE_SPACE) * 3 - Config.PAGE_SPACE, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = badgePack.createGraphics();
        g2d.setColor(Color.WHITE);
        g2d.fill(new Rectangle(badgePack.getWidth(), badgePack.getHeight()));

        for (int i = 0; i <= entries.size(); i++) {
            try {
                if (counter == 9 || i == entries.size()) {
                    pageCounter++;
                    BufferedImage page = new BufferedImage(Config.PAGE_WIDTH, Config.PAGE_HEIGHT, BufferedImage.TYPE_INT_RGB);
                    Graphics2D pageGraphics = page.createGraphics();
                    pageGraphics.setColor(Color.WHITE);
                    pageGraphics.fill(new Rectangle(Config.PAGE_WIDTH, Config.PAGE_HEIGHT));

                    int x = (Config.PAGE_WIDTH - badgePack.getWidth()) / 2;
                    int y = (Config.PAGE_HEIGHT - badgePack.getHeight()) / 2;
                    pageGraphics.drawImage(badgePack, x, y, null);
                    pageGraphics.dispose();
                    ImageIO.write(page, "jpg", new File("Badger/result", "page" + pageCounter + ".jpg"));

                    if (i == entries.size()) {
                        break;
                    }

                    g2d.fill(new Rectangle(badgePack.getWidth(), badgePack.getHeight()));
                    counter = 0;
                }

                File badgeFile = new File("Badger/temp/badges", entries.get(i).getFileName() + ".png");
                BufferedImage badgeImage = ImageIO.read(badgeFile);
                badgeImage = ImageRotateUtil.rotateImage(badgeImage, Math.PI / 2);
                int num = counter % 3;
                int row = counter / 3;
                g2d.drawImage(badgeImage, (badgeImage.getWidth() + Config.PAGE_SPACE) * num, (badgeImage.getHeight() + Config.PAGE_SPACE) * row, null);

                counter++;
//                badgeFile.delete();
            } catch (IOException e) {
                progressBar.pause(e.getMessage());
                progressBar.start();
            }

            progressBar.increaseProgress();
        }
        g2d.dispose();

        progressBar.pause("Бейджи сформированы в листы на печать (" + timer.stop() + " s)");
        System.out.println(
                "Всё готово!\n" +
                "Распечатайте файлы из папки \"result\""
        );
        Badger.waitEnter();
    }
}
