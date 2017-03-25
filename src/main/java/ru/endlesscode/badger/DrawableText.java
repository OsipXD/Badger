package ru.endlesscode.badger;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;

/**
 * @author Osip Fatkullin
 */
public class DrawableText {
    private final Graphics2D graphics;
    private Font font;
    private final String text;

    public DrawableText(Graphics2D graphics, String fontName, String text) {
        this.graphics = graphics;
        this.font = new Font(fontName, Font.PLAIN, 10);
        this.text = text;
    }

    public void fitToAreaWithMax(Dimension area, float maxSize) {
        float fontSize = maxSize;
        this.setFontSize(maxSize);

        Dimension textArea = this.getVisibleSize();
        double widthScale = area.getWidth() / textArea.getWidth();
        double heightScale = area.getHeight() / textArea.getHeight();
        double scale = Math.min(widthScale, heightScale);

        if (scale < 1) {
            fontSize *= scale;
            this.setFontSize(fontSize);
        }
    }

    public void drawCentredAt(Rectangle area) {
        graphics.setFont(this.font);

        Point drawingPoint = getDrawingStart(area);
        graphics.drawString(this.text, drawingPoint.x, drawingPoint.y);
    }

    private Point getDrawingStart(Rectangle area) {
        Dimension textArea = getVisibleSize();

        int neededX = area.x + (area.width - textArea.width) / 2;
        int neededY = area.y + (area.height - textArea.height) / 2;

        int x = neededX;
        int y = neededY + textArea.height;

        Rectangle givenBounds = getVisibleBounds(x, y);
        x -= givenBounds.x - neededX;
        y -= givenBounds.y - neededY;

        return new Point(x, y);
    }

    public Dimension getVisibleSize() {
        return this.getVisibleBounds(0, 0).getSize();
    }

    public Rectangle getVisibleBounds(int x, int y) {
        FontRenderContext frc = graphics.getFontRenderContext();
        TextLayout layout = new TextLayout(text, font, frc);
        layout.getDescent();

        return layout.getPixelBounds(frc, x, y);
    }

    public void setFontSize(float size) {
        this.font = this.font.deriveFont(size);
    }
}
