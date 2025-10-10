package com.watermark.utils;

import com.watermark.model.WatermarkTemplate;
import java.awt.Color;
import java.awt.Point;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class TemplateManager {
    private static TemplateManager instance;
    private static final String TEMPLATE_DIR = "templates";
    private static final String LAST_SESSION_FILE = "last_session.dat";

    private TemplateManager() {
        // 确保模板目录存在
        File dir = new File(TEMPLATE_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public static TemplateManager getInstance() {
        if (instance == null) {
            instance = new TemplateManager();
        }
        return instance;
    }

    /**
     * 保存模板
     */
    public boolean saveTemplate(WatermarkTemplate template) {
        if (template.getName() == null || template.getName().trim().isEmpty()) {
            return false;
        }

        try {
            String filename = TEMPLATE_DIR + File.separator + template.getName() + ".dat";
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
                oos.writeObject(template);
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 加载所有模板
     */
    public List<WatermarkTemplate> loadAllTemplates() {
        List<WatermarkTemplate> templates = new ArrayList<>();
        File dir = new File(TEMPLATE_DIR);

        if (!dir.exists()) {
            return templates;
        }

        File[] files = dir.listFiles((d, name) -> name.endsWith(".dat") && !name.equals(LAST_SESSION_FILE));
        if (files == null) {
            return templates;
        }

        for (File file : files) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                WatermarkTemplate template = (WatermarkTemplate) ois.readObject();
                templates.add(template);
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        return templates;
    }

    /**
     * 删除模板
     */
    public boolean deleteTemplate(String templateName) {
        if (templateName == null || templateName.trim().isEmpty()) {
            return false;
        }

        String filename = TEMPLATE_DIR + File.separator + templateName + ".dat";
        File file = new File(filename);
        return file.delete();
    }

    /**
     * 保存当前会话（用于下次启动时加载）
     */
    public boolean saveCurrentSession(WatermarkTemplate template) {
        try {
            String filename = TEMPLATE_DIR + File.separator + LAST_SESSION_FILE;
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
                oos.writeObject(template);
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 加载上次会话
     */
    public WatermarkTemplate loadLastSession() {
        String filename = TEMPLATE_DIR + File.separator + LAST_SESSION_FILE;
        File file = new File(filename);

        if (!file.exists()) {
            return null;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (WatermarkTemplate) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 深度复制Color对象，解决序列化问题
     */
    public static Color cloneColor(Color color) {
        if (color == null) {
            return null;
        }
        return new Color(color.getRGB(), true);
    }

    /**
     * 深度复制Point对象，解决序列化问题
     */
    public static Point clonePoint(Point point) {
        if (point == null) {
            return null;
        }
        return new Point(point.x, point.y);
    }
}