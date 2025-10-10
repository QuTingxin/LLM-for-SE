package com.watermark.ui;

import com.watermark.model.ImageItem;

import javax.swing.*;
import java.awt.*;

public class ImageListCellRenderer extends JPanel implements ListCellRenderer<ImageItem> {
    private JLabel imageLabel;
    private JLabel nameLabel;

    public ImageListCellRenderer() {
        setLayout(new BorderLayout(0, 5));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(JLabel.CENTER);
        imageLabel.setVerticalAlignment(JLabel.CENTER);
        imageLabel.setPreferredSize(new Dimension(120, 120));
        imageLabel.setBorder(BorderFactory.createEtchedBorder());
        
        nameLabel = new JLabel();
        nameLabel.setHorizontalAlignment(JLabel.CENTER);
        nameLabel.setVerticalAlignment(JLabel.BOTTOM);

        add(imageLabel, BorderLayout.CENTER);
        add(nameLabel, BorderLayout.SOUTH);
        
        // 设置首选大小以确保一致的显示
        setPreferredSize(new Dimension(150, 170));
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends ImageItem> list,
                                                  ImageItem value,
                                                  int index,
                                                  boolean isSelected,
                                                  boolean cellHasFocus) {

        if (value != null) {
            nameLabel.setText(value.getName());

            // 调整图片大小作为缩略图
            ImageIcon originalIcon = value.getIcon();
            if (originalIcon != null) {
                Image img = originalIcon.getImage();
                Image scaledImg = img.getScaledInstance(100, 100, Image.SCALE_SMOOTH);
                imageLabel.setIcon(new ImageIcon(scaledImg));
            } else {
                imageLabel.setIcon(null);
            }
        }

        // 设置选中状态的背景色
        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setBorder(BorderFactory.createLineBorder(list.getSelectionForeground(), 2, true));
            nameLabel.setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            setBorder(BorderFactory.createLineBorder(list.getBackground(), 2, true));
            nameLabel.setForeground(list.getForeground());
        }

        return this;
    }
}