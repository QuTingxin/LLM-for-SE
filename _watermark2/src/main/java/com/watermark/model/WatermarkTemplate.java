package com.watermark.model;

import lombok.Data;
import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.io.Serializable;

@Data
public class WatermarkTemplate implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum TemplateType {
        TEXT, IMAGE
    }

    // 模板基本信息
    private String name;
    private TemplateType type;

    // 文本水印参数
    private String textWatermark;
    private String fontName;
    private int fontSize = 12;
    private boolean bold = false;
    private boolean italic = false;
    private Color textColor;
    private int textOpacity = 100;
    private boolean hasShadow = false;
    private boolean hasOutline = false;

    // 图片水印参数
    private String imagePath;
    private double imageScale = 100.0;
    private int imageOpacity = 100;

    // 通用参数
    private double rotation = 0.0;
    private WatermarkPosition position;
    private Point customPosition;

    // 位置枚举（与WatermarkPreviewPanel.WatermarkPosition保持一致）
    public enum WatermarkPosition {
        TOP_LEFT, TOP_CENTER, TOP_RIGHT,
        CENTER_LEFT, CENTER, CENTER_RIGHT,
        BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT
    }
}