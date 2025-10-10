package com.watermark.ui;

import com.watermark.model.ImageItem;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class WatermarkPreviewPanel extends JPanel {
    private ImageItem imageItem;
    private BufferedImage watermarkImage;
    private String textWatermark;
    private Font textFont;
    private Color textColor;
    private Point watermarkPosition = new Point(0, 0);
    private double scale = 1.0;
    private int opacity = 100;
    private double rotation = 0.0; // 旋转角度（弧度）
    private Point dragStartPoint;
    private Point watermarkStartPoint;
    private boolean isDragging = false;
    private WatermarkPosition presetPosition = WatermarkPosition.BOTTOM_RIGHT; // 默认位置
    private boolean usePresetPosition = true; // 是否使用预设位置
    private int imageX, imageY, imageWidth, imageHeight; // 图像在面板中的位置和尺寸

    // 水印位置枚举（九宫格）
    public enum WatermarkPosition {
        TOP_LEFT, TOP_CENTER, TOP_RIGHT,
        CENTER_LEFT, CENTER, CENTER_RIGHT,
        BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT
    }

    public WatermarkPreviewPanel() {
        setPreferredSize(new Dimension(450, 350));
        setBackground(new Color(245, 245, 245)); // 设置浅灰色背景

        // 添加边框以改善视觉效果
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("预览"),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        // 添加鼠标事件监听器以支持拖拽
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (imageItem != null && (watermarkImage != null || textWatermark != null)) {
                    // 检查是否点击在水印上
                    if (isPointOnWatermark(e.getPoint())) {
                        isDragging = true;
                        dragStartPoint = e.getPoint();
                        watermarkStartPoint = new Point(watermarkPosition);
                        usePresetPosition = false; // 开始拖拽后不再使用预设位置
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                isDragging = false;
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (isDragging && imageItem != null) {
                    BufferedImage image = imageItem.getBufferedImage();
                    if (image != null) {
                        // 计算图像在面板上的缩放比例
                        double panelScale = Math.min((double) getWidth() / image.getWidth(), (double) getHeight() / image.getHeight());

                        // 计算拖拽偏移量（面板坐标）
                        int dx = e.getX() - dragStartPoint.x;
                        int dy = e.getY() - dragStartPoint.y;

                        // 将面板坐标偏移量转换为原始图像坐标偏移量
                        int imageDx = (int) (dx / panelScale);
                        int imageDy = (int) (dy / panelScale);

                        // 更新水印位置（原始图像坐标）
                        watermarkPosition.x = watermarkStartPoint.x + imageDx;
                        watermarkPosition.y = watermarkStartPoint.y + imageDy;
                        repaint();
                    }
                }
            }
        });
    }

    // 检查点击点是否在水印上
    private boolean isPointOnWatermark(Point point) {
        if (imageItem == null) return false;

        BufferedImage image = imageItem.getBufferedImage();
        if (image == null) return false;

        // 获取水印尺寸
        int watermarkWidth, watermarkHeight;
        if (watermarkImage != null) {
            watermarkWidth = (int) (watermarkImage.getWidth() * scale);
            watermarkHeight = (int) (watermarkImage.getHeight() * scale);
        } else if (textWatermark != null && textFont != null) {
            FontMetrics fm = getFontMetrics(textFont);
            watermarkWidth = fm.stringWidth(textWatermark);
            watermarkHeight = fm.getHeight();
        } else {
            return false;
        }

        // 计算图像在面板上的缩放比例
        double panelScale = Math.min((double) getWidth() / image.getWidth(), (double) getHeight() / image.getHeight());
        int scaledImageWidth = (int) (image.getWidth() * panelScale);
        int scaledImageHeight = (int) (image.getHeight() * panelScale);
        int x = (getWidth() - scaledImageWidth) / 2;
        int y = (getHeight() - scaledImageHeight) / 2;

        // 计算水印在面板上的位置和尺寸
        int scaledWatermarkX = x + (int) (watermarkPosition.x * panelScale);
        int scaledWatermarkY = y + (int) (watermarkPosition.y * panelScale);
        int scaledWatermarkWidth = (int) (watermarkWidth * panelScale);
        int scaledWatermarkHeight = (int) (watermarkHeight * panelScale);

        // 检查点是否在水印矩形内
        return point.x >= scaledWatermarkX && point.x <= scaledWatermarkX + scaledWatermarkWidth &&
                point.y >= scaledWatermarkY && point.y <= scaledWatermarkY + scaledWatermarkHeight;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();

        // 设置渲染提示
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        // 绘制图片
        if (imageItem != null) {
            BufferedImage image = imageItem.getBufferedImage();
            if (image != null) {
                int panelWidth = getWidth();
                int panelHeight = getHeight();
                int originalImageWidth = image.getWidth();
                int originalImageHeight = image.getHeight();

                // 计算缩放比例以适应面板
                double panelScale = Math.min((double) panelWidth / originalImageWidth, (double) panelHeight / originalImageHeight);
                imageWidth = (int) (originalImageWidth * panelScale);
                imageHeight = (int) (originalImageHeight * panelScale);
                imageX = (panelWidth - imageWidth) / 2;
                imageY = (panelHeight - imageHeight) / 2;

                // 绘制图片
                g2d.drawImage(image, imageX, imageY, imageWidth, imageHeight, null);

                // 如果有水印，则绘制水印
                if (watermarkImage != null || (textWatermark != null && textFont != null && textColor != null)) {
                    // 计算水印在原始图像上的位置
                    calculateWatermarkPosition(originalImageWidth, originalImageHeight);

                    // 应用缩放
                    int scaledX = imageX + (int) (watermarkPosition.x * panelScale);
                    int scaledY = imageY + (int) (watermarkPosition.y * panelScale);
                    int scaledWatermarkWidth, scaledWatermarkHeight;

                    if (watermarkImage != null) {
                        scaledWatermarkWidth = (int) (watermarkImage.getWidth() * this.scale * panelScale);
                        scaledWatermarkHeight = (int) (watermarkImage.getHeight() * this.scale * panelScale);
                    } else {
                        FontMetrics fm = g2d.getFontMetrics(textFont);
                        scaledWatermarkWidth = (int) (fm.stringWidth(textWatermark) * panelScale);
                        scaledWatermarkHeight = (int) (fm.getHeight() * panelScale);
                    }

                    // 保存原始变换
                    AffineTransform originalTransform = g2d.getTransform();

                    // 移动到水印中心点并应用旋转
                    double centerX = scaledX + scaledWatermarkWidth / 2.0;
                    double centerY = scaledY + scaledWatermarkHeight / 2.0;
                    g2d.rotate(rotation, centerX, centerY);

                    // 绘制水印
                    if (watermarkImage != null) {
                        // 设置透明度
                        if (opacity < 100) {
                            float alpha = opacity / 100.0f;
                            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                        }

                        g2d.drawImage(watermarkImage, scaledX, scaledY,
                                scaledX + scaledWatermarkWidth, scaledY + scaledWatermarkHeight,
                                0, 0, watermarkImage.getWidth(), watermarkImage.getHeight(), null);
                    } else if (textWatermark != null) {
                        // 设置透明度
                        if (opacity < 100) {
                            float alpha = opacity / 100.0f;
                            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                        }

                        g2d.setFont(textFont.deriveFont((float)(textFont.getSize() * panelScale)));
                        g2d.setColor(textColor);
                        g2d.drawString(textWatermark, scaledX, scaledY + g2d.getFontMetrics().getAscent());
                    }

                    // 恢复原始变换
                    g2d.setTransform(originalTransform);
                }
            }
        } else {
            // 没有图片时显示提示文本
            g2d.setColor(Color.GRAY);
            Font font = new Font("黑体", Font.PLAIN, 16);
            g2d.setFont(font);
            FontMetrics fm = g2d.getFontMetrics();
            String text = "请选择一张图片进行预览";
            int x = (getWidth() - fm.stringWidth(text)) / 2;
            int y = (getHeight() + fm.getAscent()) / 2;
            g2d.drawString(text, x, y);
        }

        g2d.dispose();
    }

    // 计算水印在原始图像上的位置
    private void calculateWatermarkPosition(int imageWidth, int imageHeight) {
        int watermarkWidth, watermarkHeight;

        if (watermarkImage != null) {
            watermarkWidth = (int) (watermarkImage.getWidth() * scale);
            watermarkHeight = (int) (watermarkImage.getHeight() * scale);
        } else if (textWatermark != null && textFont != null) {
            FontMetrics fm = getFontMetrics(textFont);
            watermarkWidth = fm.stringWidth(textWatermark);
            watermarkHeight = fm.getHeight();
        } else {
            return;
        }

        // 如果正在拖拽或者已经手动设置了位置，则使用当前位置，否则使用预设位置
        if (isDragging || !usePresetPosition) {
            // 使用当前拖拽位置或已设置的位置
            return;
        }

        // 根据预设位置计算水印位置
        switch (presetPosition) {
            case TOP_LEFT:
                watermarkPosition.setLocation(20, 20);
                break;
            case TOP_CENTER:
                watermarkPosition.setLocation((imageWidth - watermarkWidth) / 2, 20);
                break;
            case TOP_RIGHT:
                watermarkPosition.setLocation(imageWidth - watermarkWidth - 20, 20);
                break;
            case CENTER_LEFT:
                watermarkPosition.setLocation(20, (imageHeight - watermarkHeight) / 2);
                break;
            case CENTER:
                watermarkPosition.setLocation((imageWidth - watermarkWidth) / 2, (imageHeight - watermarkHeight) / 2);
                break;
            case CENTER_RIGHT:
                watermarkPosition.setLocation(imageWidth - watermarkWidth - 20, (imageHeight - watermarkHeight) / 2);
                break;
            case BOTTOM_LEFT:
                watermarkPosition.setLocation(20, imageHeight - watermarkHeight - 20);
                break;
            case BOTTOM_CENTER:
                watermarkPosition.setLocation((imageWidth - watermarkWidth) / 2, imageHeight - watermarkHeight - 20);
                break;
            case BOTTOM_RIGHT:
                watermarkPosition.setLocation(imageWidth - watermarkWidth - 20, imageHeight - watermarkHeight - 20);
                break;
        }
    }

    // 设置预览的图片
    public void setImageItem(ImageItem imageItem) {
        this.imageItem = imageItem;
        repaint();
    }

    // 设置图片水印
    public void setWatermarkImage(BufferedImage watermarkImage) {
        this.watermarkImage = watermarkImage;
        repaint();
    }

    // 设置文本水印
    public void setTextWatermark(String textWatermark, Font textFont, Color textColor) {
        this.textWatermark = textWatermark;
        this.textFont = textFont;
        this.textColor = textColor;
        repaint();
    }

    // 设置缩放比例
    public void setScale(double scale) {
        this.scale = scale;
        repaint();
    }

    // 设置透明度
    public void setOpacity(int opacity) {
        this.opacity = opacity;
        repaint();
    }

    // 设置旋转角度（角度转为弧度）
    public void setRotation(double degrees) {
        this.rotation = Math.toRadians(degrees);
        repaint();
    }

    // 设置预设位置
    public void setPresetPosition(WatermarkPosition position) {
        this.presetPosition = position;
        repaint();
    }

    // 获取当前水印位置
    public Point getWatermarkPosition() {
        return new Point(watermarkPosition);
    }

    // 设置水印位置
    public void setWatermarkPosition(Point position) {
        if (position != null) {
            this.watermarkPosition.setLocation(position);
            this.usePresetPosition = false;
            repaint();
        }
    }

    // 获取旋转角度（度数）
    public double getRotation() {
        return Math.toDegrees(rotation);
    }

    // 获取缩放比例
    public double getScale() {
        return scale;
    }

    // 获取透明度
    public int getOpacity() {
        return opacity;
    }

    // 获取预设位置
    public WatermarkPosition getPresetPosition() {
        return presetPosition;
    }

    // 设置是否使用预设位置
    public void setUsePresetPosition(boolean usePresetPosition) {
        this.usePresetPosition = usePresetPosition;
        repaint();
    }

    // 获取是否使用预设位置
    public boolean isUsePresetPosition() {
        return usePresetPosition;
    }
}