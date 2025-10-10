package com.watermark.controller;

import com.watermark.model.ImageItem;
import com.watermark.ui.ExportSettingsDialog;
import com.watermark.ui.MainWindow;
import com.watermark.ui.TextWatermarkDialog;
import com.watermark.ui.ImageWatermarkDialog;
import com.watermark.ui.WatermarkPreviewPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.imageio.ImageIO;
import java.awt.geom.AffineTransform;

public class ImageImportController {
    private MainWindow mainWindow;

    public ImageImportController(MainWindow mainWindow) {
        this.mainWindow = mainWindow;
    }

    public void importSingleImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setMultiSelectionEnabled(false);//不可多选
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "图片文件 (JPEG, PNG, BMP, TIFF)", "jpg", "jpeg", "png", "bmp", "tiff", "tif"));

        if (fileChooser.showOpenDialog(mainWindow.getFrame()) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            addImageToList(file);
        }
    }

    public void importMultipleImages() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setMultiSelectionEnabled(true);
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "图片文件 (JPEG, PNG, BMP, TIFF)", "jpg", "jpeg", "png", "bmp", "tiff", "tif"));

        if (fileChooser.showOpenDialog(mainWindow.getFrame()) == JFileChooser.APPROVE_OPTION) {
            File[] files = fileChooser.getSelectedFiles();
            for (File file : files) {
                addImageToList(file);
            }
        }
    }

    public void importFolder() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setMultiSelectionEnabled(false);

        if (fileChooser.showOpenDialog(mainWindow.getFrame()) == JFileChooser.APPROVE_OPTION) {
            File folder = fileChooser.getSelectedFile();
            File[] files = folder.listFiles((dir, name) ->
                    name.toLowerCase().matches(".*\\.(jpg|jpeg|png|bmp|tiff|tif)$"));

            if (files != null) {
                for (File file : files) {
                    addImageToList(file);
                }
            }
        }
    }

    public void addTextWatermark() {
        if (mainWindow.getImageListModel().getSize() == 0) {
            JOptionPane.showMessageDialog(mainWindow.getFrame(), "请先导入图片", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // 获取选中的图片用于预览
        ImageItem selectedImage = mainWindow.getImageListModel().getElementAt(0);

        // 显示文本水印设置对话框
        TextWatermarkDialog dialog = new TextWatermarkDialog(mainWindow.getFrame(), selectedImage);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            // 应用文本水印到所有图片
            applyTextWatermarkToAllImages(
                    dialog.getWatermarkText(),
                    dialog.getFontName(),
                    dialog.getFontSize(),
                    dialog.isBold(),
                    dialog.isItalic(),
                    dialog.getTextColor(),
                    dialog.getWatermarkOpacity(),
                    dialog.hasShadow(),
                    dialog.hasOutline(),
                    dialog.getRotation(),
                    dialog.getPosition(),
                    dialog.getWatermarkPosition() // 添加自定义位置参数
            );
        }
    }

    public void addImageWatermark() {
        if (mainWindow.getImageListModel().getSize() == 0) {
            JOptionPane.showMessageDialog(mainWindow.getFrame(), "请先导入图片", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // 获取选中的图片用于预览
        ImageItem selectedImage = mainWindow.getImageListModel().getElementAt(0);

        // 显示图片水印设置对话框
        ImageWatermarkDialog dialog = new ImageWatermarkDialog(mainWindow.getFrame(), selectedImage);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            // 应用图片水印到所有图片
            applyImageWatermarkToAllImages(
                    dialog.getWatermarkImage(),
                    dialog.getScale(),
                    dialog.getWatermarkOpacity(),
                    dialog.getRotation(),
                    dialog.getPosition(),
                    dialog.getWatermarkPosition() // 添加自定义位置参数
            );
        }
    }

    private void applyTextWatermarkToAllImages(
            String text,
            String fontName,
            int fontSize,
            boolean isBold,
            boolean isItalic,
            Color textColor,
            int opacity,
            boolean hasShadow,
            boolean hasOutline,
            double rotation,
            WatermarkPreviewPanel.WatermarkPosition position,
            Point customPosition) { // 添加自定义位置参数

        try {
            // 创建字体
            int fontStyle = Font.PLAIN;
            if (isBold && isItalic) {
                fontStyle = Font.BOLD | Font.ITALIC;
            } else if (isBold) {
                fontStyle = Font.BOLD;
            } else if (isItalic) {
                fontStyle = Font.ITALIC;
            }

            Font font = new Font(fontName, fontStyle, fontSize);

            // 计算透明度 (0-255)
            int alpha = (int) (255 * (opacity / 100.0));
            Color transparentColor = new Color(
                    textColor.getRed(),
                    textColor.getGreen(),
                    textColor.getBlue(),
                    alpha
            );

            // 对所有图片应用水印
            for (int i = 0; i < mainWindow.getImageListModel().getSize(); i++) {
                ImageItem item = mainWindow.getImageListModel().getElementAt(i);
                File originalFile = item.getFile();

                // 读取原图
                BufferedImage originalImage = ImageIO.read(originalFile);

                // 创建带水印的图片
                BufferedImage watermarkedImage = addAdvancedTextWatermarkToImage(
                        originalImage,
                        text,
                        font,
                        transparentColor,
                        hasShadow,
                        hasOutline,
                        rotation,
                        position,
                        customPosition // 传递自定义位置
                );

                // 更新ImageItem中的图片
                ImageIcon updatedIcon = new ImageIcon(watermarkedImage);
                item.setIcon(updatedIcon);
            }

            // 通知列表更新
            mainWindow.updateImageList();

            JOptionPane.showMessageDialog(mainWindow.getFrame(),
                    "文本水印已添加到所有图片",
                    "完成",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(mainWindow.getFrame(),
                    "添加水印时发生错误：" + e.getMessage(),
                    "错误",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private BufferedImage addAdvancedTextWatermarkToImage(
            BufferedImage originalImage,
            String text,
            Font font,
            Color textColor,
            boolean hasShadow,
            boolean hasOutline,
            double rotation,
            WatermarkPreviewPanel.WatermarkPosition position,
            Point customPosition) { // 添加自定义位置参数

        int width = originalImage.getWidth();
        int height = originalImage.getHeight();

        // 创建新的图片用于绘制水印
        BufferedImage watermarkedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = watermarkedImage.createGraphics();

        // 绘制原图
        g2d.drawImage(originalImage, 0, 0, null);

        // 设置抗锯齿
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // 设置字体和颜色
        g2d.setFont(font);
        g2d.setColor(textColor);

        // 计算文本尺寸
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        int textHeight = fm.getHeight();

        // 计算水印位置
        int x = 0, y = 0;

        // 如果有自定义位置，则使用自定义位置，否则使用预设位置
        if (customPosition != null) {
            x = customPosition.x;
            y = customPosition.y;
        } else {
            int margin = 20;
            switch (position) {
                case TOP_LEFT:
                    x = margin;
                    y = margin + fm.getAscent();
                    break;
                case TOP_CENTER:
                    x = (width - textWidth) / 2;
                    y = margin + fm.getAscent();
                    break;
                case TOP_RIGHT:
                    x = width - textWidth - margin;
                    y = margin + fm.getAscent();
                    break;
                case CENTER_LEFT:
                    x = margin;
                    y = (height + fm.getAscent()) / 2;
                    break;
                case CENTER:
                    x = (width - textWidth) / 2;
                    y = (height + fm.getAscent()) / 2;
                    break;
                case CENTER_RIGHT:
                    x = width - textWidth - margin;
                    y = (height + fm.getAscent()) / 2;
                    break;
                case BOTTOM_LEFT:
                    x = margin;
                    y = height - margin;
                    break;
                case BOTTOM_CENTER:
                    x = (width - textWidth) / 2;
                    y = height - margin;
                    break;
                case BOTTOM_RIGHT:
                    x = width - textWidth - margin;
                    y = height - margin;
                    break;
            }
        }

        // 应用旋转
        AffineTransform origTransform = g2d.getTransform();
        AffineTransform transform = new AffineTransform();
        transform.translate(x + textWidth/2.0, y - fm.getAscent()/2.0);
        transform.rotate(Math.toRadians(rotation));
        transform.translate(-(x + textWidth/2.0), -(y - fm.getAscent()/2.0));
        g2d.setTransform(transform);

        // 绘制阴影效果
        if (hasShadow) {
            Color shadowColor = new Color(0, 0, 0, textColor.getAlpha() / 2);
            g2d.setColor(shadowColor);
            g2d.drawString(text, x + 2, y + 2);
            g2d.setColor(textColor);
        }

        // 绘制描边效果
        if (hasOutline) {
            Color outlineColor = Color.BLACK;
            g2d.setColor(outlineColor);
            // 绘制多个位置的文字以形成描边效果
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    if (dx != 0 || dy != 0) {
                        g2d.drawString(text, x + dx, y + dy);
                    }
                }
            }
            g2d.setColor(textColor);
        }

        // 绘制文字水印
        g2d.drawString(text, x, y);

        // 恢复原始变换
        g2d.setTransform(origTransform);

        g2d.dispose();
        return watermarkedImage;
    }

    private void applyImageWatermarkToAllImages(
            BufferedImage watermarkImage,
            double scale,
            int opacity,
            double rotation,
            WatermarkPreviewPanel.WatermarkPosition position,
            Point customPosition) { // 添加自定义位置参数

        try {
            // 对所有图片应用水印
            for (int i = 0; i < mainWindow.getImageListModel().getSize(); i++) {
                ImageItem item = mainWindow.getImageListModel().getElementAt(i);
                File originalFile = item.getFile();

                // 读取原图
                BufferedImage originalImage = ImageIO.read(originalFile);

                // 创建带水印的图片
                BufferedImage watermarkedImage = addAdvancedImageWatermarkToImage(
                        originalImage,
                        watermarkImage,
                        scale,
                        opacity,
                        rotation,
                        position,
                        customPosition // 传递自定义位置
                );

                // 更新ImageItem中的图片
                ImageIcon updatedIcon = new ImageIcon(watermarkedImage);
                item.setIcon(updatedIcon);
            }

            // 通知列表更新
            mainWindow.updateImageList();

            JOptionPane.showMessageDialog(mainWindow.getFrame(),
                    "图片水印已添加到所有图片",
                    "完成",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(mainWindow.getFrame(),
                    "添加水印时发生错误：" + e.getMessage(),
                    "错误",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private BufferedImage addAdvancedImageWatermarkToImage(
            BufferedImage originalImage,
            BufferedImage watermarkImage,
            double scale,
            int opacity,
            double rotation,
            WatermarkPreviewPanel.WatermarkPosition position,
            Point customPosition) { // 添加自定义位置参数

        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();

        // 创建新的图片用于绘制水印
        BufferedImage watermarkedImage = new BufferedImage(originalWidth, originalHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = watermarkedImage.createGraphics();

        // 绘制原图
        g2d.drawImage(originalImage, 0, 0, null);

        // 设置抗锯齿
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 计算水印尺寸
        int scaledWidth = (int) (watermarkImage.getWidth() * scale / 100);
        int scaledHeight = (int) (watermarkImage.getHeight() * scale / 100);

        // 计算水印位置
        int x = 0, y = 0;

        // 如果有自定义位置，则使用自定义位置，否则使用预设位置
        if (customPosition != null) {
            x = customPosition.x;
            y = customPosition.y;
        } else {
            int margin = 20;
            switch (position) {
                case TOP_LEFT:
                    x = margin;
                    y = margin;
                    break;
                case TOP_CENTER:
                    x = (originalWidth - scaledWidth) / 2;
                    y = margin;
                    break;
                case TOP_RIGHT:
                    x = originalWidth - scaledWidth - margin;
                    y = margin;
                    break;
                case CENTER_LEFT:
                    x = margin;
                    y = (originalHeight - scaledHeight) / 2;
                    break;
                case CENTER:
                    x = (originalWidth - scaledWidth) / 2;
                    y = (originalHeight - scaledHeight) / 2;
                    break;
                case CENTER_RIGHT:
                    x = originalWidth - scaledWidth - margin;
                    y = (originalHeight - scaledHeight) / 2;
                    break;
                case BOTTOM_LEFT:
                    x = margin;
                    y = originalHeight - scaledHeight - margin;
                    break;
                case BOTTOM_CENTER:
                    x = (originalWidth - scaledWidth) / 2;
                    y = originalHeight - scaledHeight - margin;
                    break;
                case BOTTOM_RIGHT:
                    x = originalWidth - scaledWidth - margin;
                    y = originalHeight - scaledHeight - margin;
                    break;
            }
        }

        // 如果需要调整透明度，则创建带透明度的水印图像
        BufferedImage finalWatermarkImage = watermarkImage;
        if (opacity < 100) {
            // 创建带透明度的水印图像
            BufferedImage transparentWatermark = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2dWatermark = transparentWatermark.createGraphics();

            // 设置透明度
            float alpha = opacity / 100.0f;
            g2dWatermark.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

            // 绘制水印图像
            g2dWatermark.drawImage(watermarkImage, 0, 0, scaledWidth, scaledHeight, null);
            g2dWatermark.dispose();

            finalWatermarkImage = transparentWatermark;
        }

        // 应用旋转
        if (rotation != 0) {
            AffineTransform origTransform = g2d.getTransform();
            AffineTransform transform = new AffineTransform();
            transform.translate(x + scaledWidth/2.0, y + scaledHeight/2.0);
            transform.rotate(Math.toRadians(rotation));
            transform.translate(-x - scaledWidth/2.0, -y - scaledHeight/2.0);
            g2d.setTransform(transform);
            g2d.drawImage(finalWatermarkImage, x, y, null);
            g2d.setTransform(origTransform);
        } else {
            // 直接绘制水印图像
            g2d.drawImage(finalWatermarkImage, x, y, scaledWidth, scaledHeight, null);
        }

        g2d.dispose();
        return watermarkedImage;
    }

    public void exportImages() {
        if (mainWindow.getImageListModel().getSize() == 0) {
            JOptionPane.showMessageDialog(mainWindow.getFrame(), "没有图片需要导出", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // 选择输出文件夹
        JFileChooser folderChooser = new JFileChooser();
        folderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        folderChooser.setDialogTitle("选择导出文件夹");

        if (folderChooser.showOpenDialog(mainWindow.getFrame()) == JFileChooser.APPROVE_OPTION) {
            File outputFolder = folderChooser.getSelectedFile();

            // 检查是否与原文件夹相同
            boolean sameFolder = false;
            for (int i = 0; i < mainWindow.getImageListModel().getSize(); i++) {
                ImageItem item = mainWindow.getImageListModel().getElementAt(i);
                if (item.getFile().getParentFile().equals(outputFolder)) {
                    sameFolder = true;
                    break;
                }
            }

            if (sameFolder) {
                int result = JOptionPane.showConfirmDialog(
                        mainWindow.getFrame(),
                        "导出文件夹与原文件夹相同，可能会覆盖原文件。是否继续？",
                        "警告",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );

                if (result != JOptionPane.YES_OPTION) {
                    return;
                }
            }

            // 显示导出设置对话框
            ExportSettingsDialog dialog = new ExportSettingsDialog(mainWindow.getFrame(), mainWindow.getOutputFormat());
            dialog.setVisible(true);

            if (dialog.isConfirmed()) {
                String namingRule = dialog.getNamingRule();
                String prefix = dialog.getPrefix();
                String suffix = dialog.getSuffix();
                int quality = dialog.getQuality();

                // 执行导出
                exportImagesToFolder(outputFolder, namingRule, prefix, suffix, quality);
            }
        }
    }

    private void exportImagesToFolder(File outputFolder, String namingRule, String prefix, String suffix, int quality) {
        try {
            int successCount = 0;
            for (int i = 0; i < mainWindow.getImageListModel().getSize(); i++) {
                ImageItem item = mainWindow.getImageListModel().getElementAt(i);
                File originalFile = item.getFile();

                // 根据命名规则生成新文件名
                String newName = generateNewFileName(originalFile.getName(), namingRule, prefix, suffix);
                File outputFile = new File(outputFolder, newName);

                // 获取带水印的图片
                BufferedImage image = item.getBufferedImage();

                // 导出图片
                if (mainWindow.getOutputFormat().equals("JPEG")) {
                    // 对于JPEG格式，确保没有透明度
                    BufferedImage jpegImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
                    jpegImage.createGraphics().drawImage(image, 0, 0, null);
                    ImageIO.write(jpegImage, "jpeg", outputFile);
                } else {
                    // PNG格式支持透明度
                    ImageIO.write(image, "png", outputFile);
                }
                successCount++;
            }

            JOptionPane.showMessageDialog(mainWindow.getFrame(),
                    "导出完成！成功导出 " + successCount + " 张图片。",
                    "导出完成",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(mainWindow.getFrame(),
                    "导出过程中发生错误：" + e.getMessage(),
                    "导出失败",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private String generateNewFileName(String originalName, String namingRule, String prefix, String suffix) {
        // 移除文件扩展名
        int lastDotIndex = originalName.lastIndexOf('.');
        String nameWithoutExtension = lastDotIndex > 0 ? originalName.substring(0, lastDotIndex) : originalName;
        String extension = mainWindow.getOutputFormat().toLowerCase();

        switch (namingRule) {
            case "prefix":
                return prefix + originalName;
            case "suffix":
                return nameWithoutExtension + suffix + "." + extension;
            case "original":
            default:
                // 保留原文件名，但可能需要更改扩展名
                return nameWithoutExtension + "." + extension;
        }
    }

    public void addDragAndDropSupport(JComponent component) {
        component.setDropTarget(new DropTarget(component, new DropTargetAdapter() {
            @Override
            public void drop(DropTargetDropEvent dtde) {
                try {
                    dtde.acceptDrop(DnDConstants.ACTION_COPY);
                    Transferable transferable = dtde.getTransferable();

                    if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                        List<File> files = (List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);

                        for (File file : files) {

                            if (file.isFile() && isImageFile(file)) {
                                addImageToList(file);
                            } else if (file.isDirectory()) {
                                importFolderImages(file);
                            }
                        }
                    }
                    dtde.dropComplete(true);
                } catch (Exception e) {
                    e.printStackTrace();
                    dtde.dropComplete(false);
                }
            }
        }));
    }

    private void addImageToList(File file) {
        ImageIcon icon = new ImageIcon(file.getAbsolutePath());
        ImageItem imageItem = new ImageItem(file.getName(), icon, file);
        mainWindow.getImageListModel().addElement(imageItem);
    }

    private boolean isImageFile(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".jpg") || name.endsWith(".jpeg") ||
                name.endsWith(".png") || name.endsWith(".bmp") ||
                name.endsWith(".tiff") || name.endsWith(".tif");
    }

    private void importFolderImages(File folder) {
        File[] files = folder.listFiles((dir, name) ->
                name.toLowerCase().matches(".*\\.(jpg|jpeg|png|bmp|tiff|tif)$"));

        if (files != null) {
            for (File file : files) {
                addImageToList(file);
            }
        }
    }

    // 获取支持的图片格式列表（用于显示）
    public static String[] getSupportedFormats() {
        return new String[]{"JPEG", "PNG", "BMP", "TIFF"};
    }

    // 获取支持的文件扩展名
    public static String[] getSupportedExtensions() {
        return new String[]{"jpg", "jpeg", "png", "bmp", "tiff", "tif"};
    }
}