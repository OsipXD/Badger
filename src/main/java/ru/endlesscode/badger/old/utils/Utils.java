package ru.endlesscode.badger.old.utils;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

/**
 * Created by OsipXD on 10.12.2015
 * It is part of the Badger.
 * All rights reserved 2014 - 2015 © «EndlessCode Group»
 */
public class Utils {
    private static final char[] SYMBOLS;

    static {
        StringBuilder tmp = new StringBuilder();
        for (char ch = '0'; ch <= '9'; ++ch) {
            tmp.append(ch);
        }
        for (char ch = 'A'; ch <= 'Z'; ++ch) {
            tmp.append(ch);
        }
        for (char ch = 'a'; ch <= 'z'; ++ch) {
            tmp.append(ch);
        }

        SYMBOLS = tmp.toString().toCharArray();
    }

    public static String generateRandomString(int length) {
        Random random = new Random();
        char[] buf = new char[length];

        for (int i = 0; i < buf.length; ++i) {
            buf[i] = SYMBOLS[random.nextInt(SYMBOLS.length)];
        }

        return new String(buf);
    }

    public static BufferedImage resizeImage(BufferedImage img, float scale) {
        int scaledWidth = (int) (img.getWidth()*scale);
        int scaledHeight = (int) (img.getHeight()*scale);
        Image tmp = img.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH);
        BufferedImage resizedImg = new BufferedImage(scaledWidth, scaledHeight, img.getType());

        Graphics2D g2d = resizedImg.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();

        return resizedImg;
    }

    public static BufferedImage cropImage(BufferedImage src, Rectangle rect) {
        return src.getSubimage(rect.x, rect.y, rect.width, rect.height);
    }

    @Nullable
    public static JSONObject parseFaces(HttpResponse<JsonNode> response) {
        JSONArray faces = response.getBody().getObject().getJSONArray("faces");
        int width = response.getBody().getObject().getJSONObject("image").getInt("width");

        // Удаляем ложные "лица" из списка опредеенных
        // Ложные лица можно определить по двум критериям:
        // 1. Ориентация в профиль, а не анфас (те фотографии, которые сделаны по просьбе в профиль, придется обработать вручную)
        // 2. Определенное лицо занимает слишком мало места на фотографии (тот неловкий момент, когда стена больше похожа на лицо чем ты :с)
        for (int i = faces.length() - 1; i >= 0; i--) {
            JSONObject face = faces.getJSONObject(i);
            if (!face.getString("orientation").equals("frontal") || face.getInt("width") < width*0.25) {
                faces.remove(i);
            }
        }

        if (faces.length() == 0) {
            return null;
        }

        // Выбираем самое большое лицо в надежде, что оно наверняка окажется главным :D
        int mainFace = 0, maxArea = 0;
        for (int i = 0; i < faces.length(); i++) {
            JSONObject face = faces.getJSONObject(i);
            int area = face.getInt("width")*face.getInt("height");
            if (area > maxArea) {
                mainFace = i;
                maxArea = area;
            }
        }

        return faces.getJSONObject(mainFace);
    }

}
