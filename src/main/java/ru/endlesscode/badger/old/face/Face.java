package ru.endlesscode.badger.old.face;

import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.MetadataException;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import ru.endlesscode.badger.old.misc.Config;
import ru.endlesscode.badger.old.utils.ImageRotateUtil;
import ru.endlesscode.badger.old.utils.Utils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Created by OsipXD on 10.12.2015
 * It is part of the Badger.
 * All rights reserved 2014 - 2015 © «EndlessCode Group»
 */
public class Face {
    private final BufferedImage image;
    private final int x;
    private final int y;
    private final int width;
    private final int height;
    private final int pointX;
    private final int pointY;

    private boolean doubtful = false;

    public Face(File file) throws IOException, UnirestException, FaceNotFoundException, MetadataException, ImageProcessingException {
        BufferedImage image = ImageRotateUtil.orientatedImage(file);

        // Проверяем соотношение сторон и обрезаем до нужного
        if ((float) image.getWidth()/image.getHeight() > Config.RATIO) {
            int extraWidth = image.getWidth() - (int) (image.getHeight()*Config.RATIO);
            image = Utils.cropImage(image, new Rectangle(extraWidth/2, 0, image.getWidth() - extraWidth, image.getHeight()));
        } else if ((float) image.getWidth()/image.getHeight() < Config.RATIO) {
            int extraHeight = image.getHeight() - (int) (image.getWidth()/Config.RATIO);
            image = Utils.cropImage(image, new Rectangle(0, extraHeight/2, image.getWidth(), image.getHeight() - extraHeight));
        }

        // Создаем уменьшенное временное изображение для отправки на распознание лица
        File tempDir = new File("Badger/temp/");
        File tempFile = new File(tempDir, Utils.generateRandomString(16) + ".jpg");
        if (!tempDir.exists() && !tempDir.mkdirs()) {
            throw new IOException("Не удалось создать папку \"" + tempDir.toString() + "\"");
        }

        ImageIO.write(Utils.resizeImage(image, 1.f/Config.SEND_COMPRESSION), "jpg", tempFile);
        this.image = image;

        JSONObject face = Utils.parseFaces(Unirest.post("https://apicloud-facerect.p.mashape.com/process-file.json")
                .header("X-Mashape-Key", "YMmT42Pr6Kmshf1MWRpVh9mK2Wtrp1GdG43jsnDunjJvEMwD1a")
                .field("features", true)
                .field("image", tempFile)
                .asJson()
        );

        //noinspection ResultOfMethodCallIgnored
        tempFile.delete();
        if (face == null) {
            throw new FaceNotFoundException("Фотография \"" + file.toString() + "\" не распознана");
        }

        this.x = face.getInt("x")*Config.SEND_COMPRESSION;
        this.y = face.getInt("y")*Config.SEND_COMPRESSION;
        this.width = face.getInt("width")*Config.SEND_COMPRESSION;
        this.height = face.getInt("height")*Config.SEND_COMPRESSION;

        int pointX, pointY;
        try {
            JSONArray eyes = face.getJSONObject("features").getJSONArray("eyes");
            JSONObject nose = face.getJSONObject("features").getJSONObject("nose");
            JSONObject leftEye = eyes.getJSONObject(0);
            JSONObject rightEye = eyes.getJSONObject(1);

            int eyesX = (leftEye.getInt("x") + leftEye.getInt("width")/2
                    + (rightEye.getInt("x") + rightEye.getInt("width")/2))*Config.SEND_COMPRESSION/2;
            int eyesY = (leftEye.getInt("y") + leftEye.getInt("height")/2
                    + (rightEye.getInt("y") + rightEye.getInt("height")/2))*Config.SEND_COMPRESSION/2;
            int noseX = (nose.getInt("x") + nose.getInt("width")/2)*Config.SEND_COMPRESSION;
            int noseY = (nose.getInt("y") + nose.getInt("height")/2)*Config.SEND_COMPRESSION;

            pointX = (eyesX + noseX)/2;
            pointY = (eyesY + noseY)/2;
        } catch (JSONException e) {
            pointX = this.x + this.width/2;
            pointY = this.y + this.height/2;
        }

        this.pointX = pointX;
        this.pointY = pointY;
    }

    public BufferedImage getImage() {
        int leftSpace = this.x;
        int rightSpace = this.image.getWidth() - this.x - this.width;

        int space = leftSpace < this.width/3 ? leftSpace : this.width/3;

        if (rightSpace < space) {
            space = rightSpace;
        }

        if (space < this.width*Config.CRITICAL_WIDTH) {
            this.doubtful = true;
        }

        int width = this.width + 2*space;
        int height = (int) (width/Config.RATIO);
        int x = this.x - space;
        int y = this.pointY - height/2;
        if (y < 0) {
            y = 0;
            if (this.y < this.height*Config.CRITICAL_HEIGHT) {
                this.doubtful = true;
            }
        } else if (y + height > this.image.getHeight()) {
            y = this.image.getHeight() - height;
            if (y + height - (this.y + this.height) < this.height*Config.CRITICAL_HEIGHT) {
                this.doubtful = true;
            }
        }

        BufferedImage image = Utils.cropImage(this.image, new Rectangle(x, y, width, height));
        return Utils.resizeImage(image, Config.PHOTO_WIDTH/width);
    }

    public boolean isDoubtful() {
        return doubtful;
    }
}
