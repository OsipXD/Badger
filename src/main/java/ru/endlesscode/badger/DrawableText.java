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

        Dimension textArea = this.getBounds();
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
        Dimension textArea = getBounds();
        int x = area.x + (area.width - textArea.width) / 2;
        int y = area.y + area.height - (area.height - textArea.height) / 2;

        return new Point(x, y);
    }

    public Dimension getBounds() {
        FontRenderContext frc = graphics.getFontRenderContext();
        TextLayout layout = new TextLayout(text, font, frc);
        Rectangle textBounds = layout.getPixelBounds(frc, 0, 0);

        return textBounds.getSize();
    }

    public void setFontSize(float size) {
        this.font = this.font.deriveFont(size);
    }
}
