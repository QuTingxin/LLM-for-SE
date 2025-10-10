package com.watermark.ui;

import javax.swing.*;
import java.awt.*;

public class ExportSettingsDialog extends JDialog {
    private boolean confirmed = false;
    private String namingRule = "original";
    private String prefix = "";
    private String suffix = "";
    private int quality = 90;

    private JRadioButton originalNameRadio;
    private JRadioButton prefixRadio;
    private JRadioButton suffixRadio;
    private JTextField prefixField;
    private JTextField suffixField;
    private JSlider qualitySlider;
    private JLabel qualityLabel;

    public ExportSettingsDialog(Frame parent, String outputFormat) {
        super(parent, "导出设置", true);
        initializeComponents(outputFormat);
        layoutComponents();
        setupEventHandlers();
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        pack();
        setResizable(false);
        setLocationRelativeTo(parent);
    }

    private void initializeComponents(String outputFormat) {
        // 命名规则单选按钮
        originalNameRadio = new JRadioButton("保留原文件名", true);
        prefixRadio = new JRadioButton("添加前缀");
        suffixRadio = new JRadioButton("添加后缀");

        // 创建按钮组
        ButtonGroup namingGroup = new ButtonGroup();
        namingGroup.add(originalNameRadio);
        namingGroup.add(prefixRadio);
        namingGroup.add(suffixRadio);

        // 文本输入框
        prefixField = new JTextField(10);
        prefixField.setPreferredSize(new Dimension(120, 25));
        suffixField = new JTextField(10);
        suffixField.setPreferredSize(new Dimension(120, 25));

        // JPEG质量滑块（仅当输出格式为JPEG时显示）
        qualityLabel = new JLabel("图片质量: 90");
        qualityLabel.setPreferredSize(new Dimension(120, 25));
        qualitySlider = new JSlider(0, 100, 90);
        qualitySlider.setMajorTickSpacing(20);
        qualitySlider.setMinorTickSpacing(5);
        qualitySlider.setPaintTicks(true);
        qualitySlider.setPaintLabels(true);
        qualitySlider.setPreferredSize(new Dimension(250, 50));
        
        // 根据输出格式决定是否显示质量设置
        qualityLabel.setVisible("JPEG".equals(outputFormat));
        qualitySlider.setVisible("JPEG".equals(outputFormat));
    }

    private void layoutComponents() {
        setLayout(new BorderLayout());
        
        // 命名规则面板
        JPanel namingPanel = new JPanel(new GridBagLayout());
        namingPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("文件命名规则"),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0; gbc.gridy = 0;
        namingPanel.add(originalNameRadio, gbc);

        gbc.gridy = 1;
        namingPanel.add(prefixRadio, gbc);
        
        gbc.gridx = 1;
        namingPanel.add(new JLabel("前缀:"), gbc);
        
        gbc.gridx = 2;
        namingPanel.add(prefixField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        namingPanel.add(suffixRadio, gbc);
        
        gbc.gridx = 1;
        namingPanel.add(new JLabel("后缀:"), gbc);
        
        gbc.gridx = 2;
        namingPanel.add(suffixField, gbc);

        // 质量设置面板
        JPanel qualityPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        qualityPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("JPEG质量设置"),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        qualityPanel.add(qualityLabel);
        qualityPanel.add(qualitySlider);
        qualityPanel.setVisible(qualityLabel.isVisible());

        // 主面板
        JPanel mainPanel = new JPanel(new BorderLayout(0, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.add(namingPanel, BorderLayout.CENTER);
        mainPanel.add(qualityPanel, BorderLayout.SOUTH);
        add(mainPanel, BorderLayout.CENTER);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton okButton = new JButton("确定");
        okButton.setPreferredSize(new Dimension(80, 30));
        JButton cancelButton = new JButton("取消");
        cancelButton.setPreferredSize(new Dimension(80, 30));
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 10));
        add(buttonPanel, BorderLayout.SOUTH);

        // 按钮事件处理
        okButton.addActionListener(e -> {
            confirmed = true;
            if (originalNameRadio.isSelected()) {
                namingRule = "original";
            } else if (prefixRadio.isSelected()) {
                namingRule = "prefix";
                prefix = prefixField.getText();
            } else if (suffixRadio.isSelected()) {
                namingRule = "suffix";
                suffix = suffixField.getText();
            }
            quality = qualitySlider.getValue();
            dispose();
        });

        cancelButton.addActionListener(e -> {
            confirmed = false;
            dispose();
        });
    }

    private void setupEventHandlers() {
        // 设置输入框的启用状态
        originalNameRadio.addActionListener(e -> {
            prefixField.setEnabled(false);
            suffixField.setEnabled(false);
        });

        prefixRadio.addActionListener(e -> {
            prefixField.setEnabled(true);
            suffixField.setEnabled(false);
        });

        suffixRadio.addActionListener(e -> {
            prefixField.setEnabled(false);
            suffixField.setEnabled(true);
        });

        // 初始化输入框状态
        prefixField.setEnabled(false);
        suffixField.setEnabled(false);

        // 质量滑块事件
        qualitySlider.addChangeListener(e -> {
            qualityLabel.setText("图片质量: " + qualitySlider.getValue());
        });
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public String getNamingRule() {
        return namingRule;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    public int getQuality() {
        return quality;
    }
}