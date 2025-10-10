package com.watermark.ui;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import java.awt.*;

public class LookAndFeelManager {
    // 统一字体设置
    private static final Font DEFAULT_FONT = new Font("微软雅黑", Font.PLAIN, 14);
    private static final Font BOLD_FONT = new Font("微软雅黑", Font.BOLD, 14);
    private static final Font TITLE_FONT = new Font("微软雅黑", Font.BOLD, 16);

    /**
     * 初始化并应用统一的外观设置
     */
    public static void initLookAndFeel() {
        try {
            // 设置系统外观
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            
            // 统一设置所有组件的默认字体
            setGlobalFont(DEFAULT_FONT);
            
            // 为特定组件设置特殊字体
            UIManager.put("Button.font", DEFAULT_FONT);
            UIManager.put("Label.font", DEFAULT_FONT);
            UIManager.put("ComboBox.font", DEFAULT_FONT);
            UIManager.put("TextField.font", DEFAULT_FONT);
            UIManager.put("TextArea.font", DEFAULT_FONT);
            UIManager.put("Slider.font", DEFAULT_FONT);
            UIManager.put("RadioButton.font", DEFAULT_FONT);
            UIManager.put("CheckBox.font", DEFAULT_FONT);
            UIManager.put("Table.font", DEFAULT_FONT);
            UIManager.put("MenuItem.font", DEFAULT_FONT);
            UIManager.put("TabbedPane.font", DEFAULT_FONT);
            UIManager.put("OptionPane.font", DEFAULT_FONT);
            UIManager.put("Panel.font", DEFAULT_FONT);
            UIManager.put("List.font", DEFAULT_FONT);
            UIManager.put("ToggleButton.font", DEFAULT_FONT);
            
            // 为标题边框设置较大字体
            UIManager.put("TitledBorder.font", TITLE_FONT);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 全局设置字体
     * @param font 要设置的字体
     */
    private static void setGlobalFont(Font font) {
        FontUIResource fontRes = new FontUIResource(font);
        for (Object key : UIManager.getDefaults().keySet()) {
            if (UIManager.get(key) instanceof FontUIResource) {
                UIManager.put(key, fontRes);
            }
        }
    }

    /**
     * 获取默认字体
     * @return 默认字体
     */
    public static Font getDefaultFont() {
        return DEFAULT_FONT;
    }

    /**
     * 获取粗体字体
     * @return 粗体字体
     */
    public static Font getBoldFont() {
        return BOLD_FONT;
    }

    /**
     * 获取标题字体
     * @return 标题字体
     */
    public static Font getTitleFont() {
        return TITLE_FONT;
    }
}