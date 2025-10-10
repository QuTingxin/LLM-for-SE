package com.watermark.model;

import lombok.Data;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

@Data
public class ImageItem {
    private String name;
    private ImageIcon icon;
    private File file;

    public ImageItem(String name, ImageIcon icon, File file) {
        this.name = name;
        this.icon = icon;
        this.file = file;
    }

    public BufferedImage getBufferedImage() {
        // 从ImageIcon创建BufferedImage
        Image image = icon.getImage();
        BufferedImage bufferedImage = new BufferedImage(
                image.getWidth(null),
                image.getHeight(null),
                BufferedImage.TYPE_INT_ARGB
        );

        Graphics2D g2d = bufferedImage.createGraphics();
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();

        return bufferedImage;
    }

    @Override
    public String toString() {
        return name;
    }
}