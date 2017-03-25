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
class BadgePainter {
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

    private static void drawBadge(Entry entry) throws IOException {
        BufferedImage badge =  new BufferedImage(Config.BADGE_WIDTH, Config.BADGE_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = badge.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        File photoFile = new File("Badger/temp", entry.getFileName() + ".jpg");
        Image photo = ImageIO.read(photoFile);
        g2d.drawImage(photo, Config.PHOTO_X, Config.PHOTO_Y, null);

        File patternFile = new File("Badger/res/images", entry.getType().name().toLowerCase() + ".png");
        Image pattern = ImageIO.read(patternFile);
        g2d.drawImage(pattern, 0, 0, null);

        Rectangle addInfoArea = null;
        Rectangle fullNameArea;
        int nameAreaHeight = (int) (Config.TEXT_AREA_HEIGHT * 0.5);

        if (entry.hasAddInfo()) {
            int addInfoHeight = nameAreaHeight / 4;
            addInfoArea = new Rectangle(Config.TEXT_AREA_X, Config.TEXT_AREA_Y, Config.TEXT_AREA_WIDTH, addInfoHeight);
            fullNameArea = new Rectangle(Config.TEXT_AREA_X, Config.TEXT_AREA_Y + addInfoHeight + Config.INFO_SPACE,
                    Config.TEXT_AREA_WIDTH, nameAreaHeight - addInfoHeight - Config.INFO_SPACE);
        } else {
            fullNameArea = new Rectangle(Config.TEXT_AREA_X, Config.TEXT_AREA_Y, Config.TEXT_AREA_WIDTH, nameAreaHeight);
        }

        if (entry.hasAddInfo()) {
            g2d.setColor(Config.INFO_COLOR);
            drawCentredText(g2d, addInfoArea, entry.getVisibleAddInfo(), Config.NAME_FONT, Config.INFO_MAX_SIZE);
        }

        g2d.setColor(Config.NAME_COLOR);
        drawFullName(g2d, fullNameArea, entry);

        int nameAreaTopSpace = fullNameArea.y - Config.TEXT_AREA_Y;
        int freeSpace = Config.TEXT_AREA_HEIGHT - fullNameArea.height - nameAreaTopSpace - Config.NAME_SPACE;
        Rectangle quoteArea = new Rectangle(Config.TEXT_AREA_X, fullNameArea.y + fullNameArea.height + Config.NAME_SPACE,
                Config.TEXT_AREA_WIDTH, freeSpace);
        g2d.setColor(Config.QUOTE_COLOR);
        drawQuote(g2d, quoteArea, entry);
        g2d.dispose();

        ImageIO.write(badge, "png", new File("Badger/temp/badges", entry.getFileName() + ".png"));
    }

    private static void drawCentredText(Graphics2D g2d, Rectangle area, String text, String fontName, int maxSize) {
        DrawableText drawableText = new DrawableText(g2d, fontName, text);
        drawableText.fitToAreaWithMax(area.getSize(), maxSize);
        drawableText.drawCentredAt(area);

        if (Config.DEBUG) {
            drawDebugShapes(g2d, Color.BLUE, area);
        }
    }

    private static void drawFullName(Graphics2D g2d, Rectangle fullNameArea, Entry entry) {
        int surnameSize = Config.NAME_MAX_SIZE;
        int nameSize = Config.NAME_MAX_SIZE;

        String name = entry.getVisibleName();

        DrawableText drawableName = new DrawableText(g2d, Config.NAME_FONT, name);

        // Обрабатываем случай, когда у нас только имя
        if (!entry.hasSurname()) {
            drawableName.fitToAreaWithMax(fullNameArea.getSize(), nameSize);
            drawableName.drawCentredAt(fullNameArea);

            Dimension nameBounds = drawableName.getVisibleSize();
            fullNameArea.setSize(fullNameArea.width, fullNameArea.height + (fullNameArea.height - nameBounds.height) / 2);
            return;
        }

        drawableName.fitToAreaWithMax(new Dimension(fullNameArea.width, fullNameArea.height / 3 * 2), nameSize);
        Dimension nameBounds = drawableName.getVisibleSize();

        String surname = entry.getVisibleSurname();
        DrawableText drawableSurname = new DrawableText(g2d, Config.NAME_FONT, surname);
        drawableSurname.fitToAreaWithMax(new Dimension(fullNameArea.width, fullNameArea.height - nameBounds.height), surnameSize);
        Dimension surnameBounds = drawableSurname.getVisibleSize();

        int fullNameHeight = nameBounds.height + Config.NAME_SPACE + surnameBounds.height;
        fullNameArea.setSize(fullNameArea.width, fullNameHeight);

        int nameY = fullNameArea.y;
        Rectangle nameArea = new Rectangle(fullNameArea.x, nameY, fullNameArea.width, nameBounds.height);
        drawableName.drawCentredAt(nameArea);

        int surnameY = nameY + nameArea.height + Config.NAME_SPACE;
        Rectangle surnameArea = new Rectangle(fullNameArea.x, surnameY, fullNameArea.width, surnameBounds.height);
        drawableSurname.drawCentredAt(surnameArea);

        if (Config.DEBUG) {
            drawDebugShapes(g2d, Color.RED, fullNameArea, nameArea, surnameArea);
        }
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

        if (Config.DEBUG) {
            drawDebugShapes(g2d, Color.GREEN, quoteArea);
        }
    }

    private static void drawDebugShapes(Graphics2D g2d, Color color, Shape... shapes) {
        Color oldColor = g2d.getColor();
        g2d.setColor(color);
        for (Shape shape: shapes) {
            g2d.draw(shape);
        }
        g2d.setColor(oldColor);
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
            } catch (IOException | FontFormatException e) {
                System.out.println("Ошибка при загрузке шрифта \"" + fontFile.getName() + "\": " + e.getMessage());
            }
        }
        System.out.println("Шрифты успешно загружены (" + counter + " шт.)");
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
                    drawCutLines(pageGraphics, new Rectangle(x, y, badgePack.getWidth(), badgePack.getHeight()));
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
    }

    private static void drawCutLines(Graphics2D g2d, Rectangle badgesRect) {
        BasicStroke dashedLine = new BasicStroke(Config.PAGE_SPACE, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{4f, 0f, 2f}, 2f);

        int space = 50;
        int startX;
        int startY;
        int endX = 0;
        int endY;

        // Рисуем вертикальные линии
        startY = badgesRect.y - space;
        endY = startY + badgesRect.height + 2 * space;
        for (int i = 0; i <= 3; i++) {
            if (i == 0) {
                startX = endX = badgesRect.x - (Config.PAGE_SPACE + 1) / 2;
            } else {
                startX = endX += Config.BADGE_HEIGHT + Config.PAGE_SPACE;
            }

            g2d.setColor(Color.DARK_GRAY);
            g2d.setStroke(new BasicStroke(Config.PAGE_SPACE));
            g2d.drawLine(startX, 0, endX, startY);
            g2d.drawLine(startX, endY, endX, Config.PAGE_HEIGHT);

            g2d.setColor(Color.LIGHT_GRAY);
            g2d.setStroke(dashedLine);
            g2d.drawLine(startX, startY, endX, endY);
        }

        // Рисуем горизонтальные линии
        startX = badgesRect.x - space;
        endX = startX + badgesRect.width + 2 * space;
        for (int i = 0; i <= 3; i++) {
            if (i == 0) {
                startY = endY = badgesRect.y - (Config.PAGE_SPACE + 1) / 2;
            } else {
                startY = endY += Config.BADGE_WIDTH + Config.PAGE_SPACE;
            }

            g2d.setColor(Color.DARK_GRAY);
            g2d.setStroke(new BasicStroke(Config.PAGE_SPACE));
            g2d.drawLine(0, startY, startX, endY);
            g2d.drawLine(endX, startY, Config.PAGE_WIDTH, endY);

            g2d.setColor(Color.LIGHT_GRAY);
            g2d.setStroke(dashedLine);
            g2d.drawLine(startX, startY, endX, endY);
        }
    }
}
