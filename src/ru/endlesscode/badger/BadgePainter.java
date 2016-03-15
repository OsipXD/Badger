package ru.endlesscode.badger;

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

/**
 * Created by OsipXD on 14.03.2016
 * It is part of the Badger.
 * All rights reserved 2014 - 2016 © «EndlessCode Group»
 */
public class BadgePainter {
    private static final int AREA_X = 75;
    private static final int AREA_Y = 285;
    private static final int AREA_WIDTH = 560;
    private static final int AREA_HEIGHT = 280;

    private static final int PHOTO_X = 660;
    private static final int PHOTO_Y = 80;

    private static final float NAME_MAX_SIZE = 80;
    private static final float NAME_MIN_SIZE = 53;
    private static final float QUOTE_MIN_SIZE = 20;

    private static final int QUOTE_CORRECTION = 2;

    public static void drawBadge(Entry entry) throws IOException {
        BufferedImage pattern = ImageIO.read(new File("Badger/res/images", entry.getType().name() + ".png"));
        Graphics2D g2d = pattern.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        Rectangle addInfoArea = null;
        Rectangle nameArea;
        int nameAreaHeight = (int)(AREA_HEIGHT * 0.7);

        if (entry.hasAddInfo()) {
            int addInfoHeight = nameAreaHeight / 4;
            addInfoArea = new Rectangle(AREA_X, AREA_Y, AREA_WIDTH, addInfoHeight);
            nameArea = new Rectangle(AREA_X, AREA_Y + addInfoHeight, AREA_WIDTH, nameAreaHeight - addInfoHeight);
        } else {
            nameArea = new Rectangle(AREA_X, AREA_Y, AREA_WIDTH, nameAreaHeight);
        }
        Rectangle quoteArea = new Rectangle(AREA_X, AREA_Y + nameAreaHeight, AREA_WIDTH, AREA_HEIGHT - nameAreaHeight);

        if (entry.hasAddInfo()) {
            g2d.setColor(Color.DARK_GRAY);
            drawCentredText(g2d, addInfoArea, entry.getAddInfo(), "Sunday", 25);
        }

        g2d.setColor(Color.BLACK);
        drawName(g2d, nameArea, entry);
        drawQuote(g2d, quoteArea, entry);
        Image photo = ImageIO.read(new File("Badger/result/confirmed", entry.getFileName() + ".jpg"));
        g2d.drawImage(photo, PHOTO_X, PHOTO_Y, null);

        ImageIO.write(pattern, "png", new File("Badger", "test.png"));
    }

    private static void drawQuote(Graphics2D g2d, Rectangle quoteArea, Entry entry) {
        String quote = "\"Очень-очень-преочень длинная цитата. Такая длинная, что вряд ли такие будут, но тем не менее эта есть и она ооочень длинная. Прям не понятно что с такой делать так как она занимает слишком много места и много строчек. И при этом ее еще должно быть видно. В общем ужас...\"";
        if (entry.hasQuote()) {
            quote = entry.getQuote();
        }

        //noinspection ConstantConditions
        AttributedString text = new AttributedString(quote);
        text.addAttribute(TextAttribute.FAMILY, "Intro Head R Base");

        int height;
        float size = QUOTE_MIN_SIZE;
        Font font = new Font("Intro Head R Base", Font.PLAIN, (int) size);
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
            while (lineMeasurer.getPosition() < paragraphEnd) {
                lineMeasurer.nextLayout(quoteArea.width);
                lineCounter++;
            }


            if (lineCounter * (metrics.getHeight() + QUOTE_CORRECTION) - QUOTE_CORRECTION > quoteArea.height) {
                metrics = g2d.getFontMetrics(font);
                height = lineCounter * (metrics.getHeight() + QUOTE_CORRECTION) - QUOTE_CORRECTION;
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
        float posY = quoteArea.y + (quoteArea.height - height + metrics.getAscent()) / 2 + metrics.getHeight();
        while (lineMeasurer.getPosition() < paragraphEnd) {
            TextLayout layout = lineMeasurer.nextLayout(quoteArea.width);
            posX = quoteArea.x + (quoteArea.width - layout.getVisibleAdvance()) / 2;
            layout.draw(g2d, posX, posY);

            posY += metrics.getHeight() - metrics.getAscent() / 2 + QUOTE_CORRECTION;
        }
    }

    private static void drawName(Graphics g, Rectangle nameArea, Entry entry) {
        float surnameSize = NAME_MIN_SIZE;
        float nameSize = NAME_MIN_SIZE;
        FontMetrics metrics;

        String name = entry.getName();
        if (entry.hasSurname()) {
            name += " " + entry.getPatronymic();
        }

        Font nameFont = new Font("Sunday", Font.PLAIN, (int) nameSize);

        // Обрабатываем случай, когда у нас только имя
        if (!entry.hasSurname()) {
            float size = NAME_MAX_SIZE;
            nameFont = nameFont.deriveFont(size);
            metrics = g.getFontMetrics(nameFont);
            int height = metrics.getHeight();
            int width = metrics.stringWidth(name);

            while (width > nameArea.width || height > nameArea.height) {
                size--;
                nameFont = nameFont.deriveFont(size);
                height = metrics.getHeight();
                width = metrics.stringWidth(name);
            }

            int x = nameArea.x + (nameArea.width - width) / 2;
            int y = nameArea.y + nameArea.height - (nameArea.height - height + metrics.getAscent()) / 2;
            g.setFont(nameFont);
            g.drawString(name, x, y);
            nameArea.setSize(nameArea.width, nameArea.height - y + nameArea.y);
            return;
        }

        metrics = g.getFontMetrics(nameFont);
        int nameWidth = metrics.stringWidth(name);
        int nameHeight;
        while (nameWidth > nameArea.width) {
            nameSize--;
            nameFont = nameFont.deriveFont(surnameSize);
            nameWidth = metrics.stringWidth(name);
        }
        nameHeight = metrics.getHeight();

        String surname = entry.getSurname();
        Font surnameFont = new Font("Sunday", Font.PLAIN, (int) surnameSize);

        metrics = g.getFontMetrics(surnameFont);
        int surnameWidth = metrics.stringWidth(surname);
        int surnameHeight = metrics.getHeight();
        while (surnameWidth > nameArea.width || surnameHeight + nameHeight > nameArea.height) {
            surnameSize--;
            surnameFont = surnameFont.deriveFont(surnameSize);
            surnameWidth = metrics.stringWidth(surname);
            surnameHeight = metrics.getHeight();
        }

        while(true) {
            Font tempFont = nameFont.deriveFont(nameSize);
            metrics = g.getFontMetrics(tempFont);

            nameHeight = metrics.getHeight();
            nameWidth = metrics.stringWidth(name);
            if (nameHeight > nameArea.height - surnameHeight || nameWidth > nameArea.width) {
                break;
            }

            nameFont = tempFont;
            nameSize++;
        }

        metrics = g.getFontMetrics(nameFont);
        nameHeight = metrics.getHeight();
        nameWidth = metrics.stringWidth(name);

        int surnameX = nameArea.x + (nameArea.width - surnameWidth) / 2;
        int surnameY = nameArea.y + nameArea.height - (nameArea.height - nameHeight - surnameHeight + metrics.getAscent()) / 2;
        int nameX = nameArea.x + (nameArea.width - nameWidth) / 2;
        int nameY = surnameY - surnameHeight;

        g.setFont(surnameFont);
        g.drawString(surname, surnameX, surnameY);

        g.setFont(nameFont);
        g.drawString(name, nameX, nameY);

        nameArea.setSize(nameArea.width, nameArea.height - surnameY + nameArea.y);
    }

    private static void drawCentredText(Graphics g, Rectangle area, String text, String fontName, int startSize) {
        float fontSize = startSize;
        int height;
        int width;

        Font font = new Font(fontName, Font.PLAIN, startSize);
        FontMetrics metrics;
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
        metrics = g.getFontMetrics(font);
        height = metrics.getHeight();
        width = metrics.stringWidth(text);

        int x = area.x + (area.width - width) / 2;
        int y = area.y + area.height - (area.height - height + metrics.getAscent()) / 2;
        g.setFont(font);
        g.drawString(text, x, y);
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
}
