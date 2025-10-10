package com.watermark;

import com.watermark.ui.LookAndFeelManager;
import com.watermark.ui.MainWindow;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // 初始化并应用统一的外观设置
            LookAndFeelManager.initLookAndFeel();

            MainWindow mainWindow = new MainWindow();
            mainWindow.createAndShow();
        });
    }
}