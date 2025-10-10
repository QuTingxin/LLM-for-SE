package com.watermark.ui;

import com.watermark.model.ImageItem;
import com.watermark.ui.WatermarkPreviewPanel.WatermarkPosition;
import com.watermark.utils.TemplateManager;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TextWatermarkDialog extends JDialog {
    private boolean confirmed = false;
    
    // 文本水印设置
    private JTextField textWatermarkField;
    private JComboBox<String> fontComboBox;
    private JSpinner fontSizeSpinner;
    private JCheckBox boldCheckBox;
    private JCheckBox italicCheckBox;
    private JButton colorButton;
    private JSlider opacitySlider;
    private JCheckBox shadowCheckBox;
    private JCheckBox outlineCheckBox;
    
    // 高级设置控件
    private JSlider rotationSlider;
    private JComboBox<String> positionComboBox;
    private WatermarkPreviewPanel previewPanel;
    
    // 模板管理控件
    private JButton saveTemplateButton;
    private JButton loadTemplateButton;
    
    // 默认值
    private String watermarkText = "水印文本";
    private String fontName = "宋体";
    private int fontSize = 80;
    private boolean isBold = false;
    private boolean isItalic = false;
    private Color textColor = Color.BLACK;
    private int watermarkOpacity = 100; // 0-100%
    private boolean hasShadow = false;
    private boolean hasOutline = false;
    
    // 高级设置默认值
    private double rotation = 0.0; // 旋转角度
    private WatermarkPosition position = WatermarkPosition.BOTTOM_RIGHT;

    public TextWatermarkDialog(Frame parent, ImageItem previewImage) {
        super(parent, "文本水印设置", true);
        initializeComponents();
        layoutComponents();
        setupEventHandlers();
        
        // 设置预览图片
        if (previewImage != null) {
            previewPanel.setImageItem(previewImage);
            updatePreview();
        }
        
        // 尝试加载上次会话
        loadLastSession();
        
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        pack();
        setResizable(true);
        setLocationRelativeTo(parent);
    }

    private void initializeComponents() {
        // 文本水印内容
        textWatermarkField = new JTextField(watermarkText, 15);
        textWatermarkField.setPreferredSize(new Dimension(200, 30));
        
        // 字体选择
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] fontNames = ge.getAvailableFontFamilyNames();
        fontComboBox = new JComboBox<>(fontNames);
        fontComboBox.setSelectedItem(fontName);
        fontComboBox.setPreferredSize(new Dimension(150, 30));
        
        // 字号选择
        fontSizeSpinner = new JSpinner(new SpinnerNumberModel(fontSize, 8, 120, 1));
        fontSizeSpinner.setPreferredSize(new Dimension(80, 30));
        JComponent editor = fontSizeSpinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            ((JSpinner.DefaultEditor) editor).getTextField().setFont(LookAndFeelManager.getDefaultFont());
        }
        
        // 粗体和斜体
        boldCheckBox = new JCheckBox("粗体", isBold);
        italicCheckBox = new JCheckBox("斜体", isItalic);
        
        // 颜色选择按钮
        colorButton = new JButton("选择颜色");
        colorButton.setBackground(textColor);
        colorButton.setForeground(textColor);
        colorButton.setPreferredSize(new Dimension(100, 30));
        
        // 透明度滑块
        opacitySlider = new JSlider(0, 100, watermarkOpacity);
        opacitySlider.setMajorTickSpacing(20);
        opacitySlider.setMinorTickSpacing(5);
        opacitySlider.setPaintTicks(true);
        opacitySlider.setPaintLabels(true);
        opacitySlider.setPreferredSize(new Dimension(250, 50));
        
        // 阴影和描边效果
        shadowCheckBox = new JCheckBox("阴影效果", hasShadow);
        outlineCheckBox = new JCheckBox("描边效果", hasOutline);
        
        // 高级设置组件
        previewPanel = new WatermarkPreviewPanel();
        previewPanel.setPreferredSize(new Dimension(450, 350));
        previewPanel.setBorder(BorderFactory.createTitledBorder("预览"));
        
        // 旋转滑块
        rotationSlider = new JSlider(-180, 180, (int)rotation);
        rotationSlider.setMajorTickSpacing(90);
        rotationSlider.setMinorTickSpacing(15);
        rotationSlider.setPaintTicks(true);
        rotationSlider.setPaintLabels(true);
        rotationSlider.setPreferredSize(new Dimension(250, 50));
        
        // 位置选择下拉框
        String[] positions = {
            "左上角", "顶部居中", "右上角",
            "左侧居中", "正中央", "右侧居中",
            "左下角", "底部居中", "右下角"
        };
        positionComboBox = new JComboBox<>(positions);
        positionComboBox.setSelectedIndex(8); // 默认右下角
        positionComboBox.setPreferredSize(new Dimension(150, 30));
        
        // 模板管理按钮
        saveTemplateButton = new JButton("保存为模板");
        saveTemplateButton.setPreferredSize(new Dimension(120, 35));
        loadTemplateButton = new JButton("加载模板");
        loadTemplateButton.setPreferredSize(new Dimension(120, 35));
    }

    private void layoutComponents() {
        setLayout(new BorderLayout());
        
        // 主设置面板
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("文本水印设置"),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        
        // 文本内容
        gbc.gridx = 0; gbc.gridy = 0;
        mainPanel.add(new JLabel("水印文本:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3;
        mainPanel.add(textWatermarkField, gbc);
        
        // 字体设置
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        mainPanel.add(new JLabel("字体:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 1;
        mainPanel.add(fontComboBox, gbc);
        
        gbc.gridx = 2;
        mainPanel.add(new JLabel("字号:"), gbc);
        gbc.gridx = 3;
        mainPanel.add(fontSizeSpinner, gbc);
        
        // 样式设置
        gbc.gridx = 0; gbc.gridy = 2;
        mainPanel.add(new JLabel("样式:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(boldCheckBox, gbc);
        gbc.gridx = 2; gbc.gridwidth = 2;
        mainPanel.add(italicCheckBox, gbc);
        
        // 颜色设置
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 1;
        mainPanel.add(new JLabel("颜色:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3;
        mainPanel.add(colorButton, gbc);
        
        // 透明度设置
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 1;
        mainPanel.add(new JLabel("透明度:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3;
        mainPanel.add(opacitySlider, gbc);
        
        // 效果设置
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 1;
        mainPanel.add(new JLabel("效果:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(shadowCheckBox, gbc);
        gbc.gridx = 2; gbc.gridwidth = 2;
        mainPanel.add(outlineCheckBox, gbc);
        
        // 高级设置 - 旋转
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 1;
        mainPanel.add(new JLabel("旋转:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3;
        mainPanel.add(rotationSlider, gbc);
        
        // 高级设置 - 位置
        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 1;
        mainPanel.add(new JLabel("位置:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3;
        mainPanel.add(positionComboBox, gbc);
        
        // 模板管理按钮
        JPanel templateButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        templateButtonPanel.add(saveTemplateButton);
        templateButtonPanel.add(loadTemplateButton);
        gbc.gridx = 0; gbc.gridy = 8; gbc.gridwidth = 4;
        mainPanel.add(templateButtonPanel, gbc);
        
        // 创建包含主设置和预览的中间面板
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        centerPanel.add(mainPanel, BorderLayout.WEST);
        centerPanel.add(previewPanel, BorderLayout.CENTER);
        
        add(centerPanel, BorderLayout.CENTER);
        
        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton okButton = new JButton("确定");
        okButton.setPreferredSize(new Dimension(80, 35));
        JButton cancelButton = new JButton("取消");
        cancelButton.setPreferredSize(new Dimension(80, 35));
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 10));
        add(buttonPanel, BorderLayout.SOUTH);
        
        // 按钮事件处理
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                confirmed = true;
                watermarkText = textWatermarkField.getText();
                fontName = (String) fontComboBox.getSelectedItem();
                fontSize = (Integer) fontSizeSpinner.getValue();
                isBold = boldCheckBox.isSelected();
                isItalic = italicCheckBox.isSelected();
                watermarkOpacity = opacitySlider.getValue();
                hasShadow = shadowCheckBox.isSelected();
                hasOutline = outlineCheckBox.isSelected();
                
                // 高级设置值
                rotation = rotationSlider.getValue();
                position = WatermarkPosition.values()[positionComboBox.getSelectedIndex()];
                
                // 保存当前会话
                saveCurrentSession();
                
                dispose();
            }
        });
        
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                confirmed = false;
                dispose();
            }
        });
    }

    private void setupEventHandlers() {
        colorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Color newColor = JColorChooser.showDialog(
                    TextWatermarkDialog.this,
                    "选择水印颜色",
                    textColor
                );
                if (newColor != null) {
                    textColor = newColor;
                    colorButton.setBackground(textColor);
                    colorButton.setForeground(textColor);
                    updatePreview(false); // 不强制更新位置
                }
            }
        });
        
        // 添加所有控件的事件监听器以更新预览
        textWatermarkField.addActionListener(e -> updatePreview(false)); // 不强制更新位置
        fontComboBox.addActionListener(e -> updatePreview(false)); // 不强制更新位置
        fontSizeSpinner.addChangeListener(e -> updatePreview(false)); // 不强制更新位置
        boldCheckBox.addActionListener(e -> updatePreview(false)); // 不强制更新位置
        italicCheckBox.addActionListener(e -> updatePreview(false)); // 不强制更新位置
        opacitySlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                watermarkOpacity = opacitySlider.getValue();
                updatePreview(false); // 不强制更新位置
            }
        });
        shadowCheckBox.addActionListener(e -> updatePreview(false)); // 不强制更新位置
        outlineCheckBox.addActionListener(e -> updatePreview(false)); // 不强制更新位置
        
        // 高级设置控件事件监听器
        rotationSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                // 确保即使在调整过程中也更新预览
                rotation = rotationSlider.getValue();
                updatePreview(false); // 不强制更新位置
            }
        });
        
        positionComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                position = WatermarkPosition.values()[positionComboBox.getSelectedIndex()];
                updatePreview(true); // 强制更新位置
            }
        });
        
        // 模板管理按钮事件
        saveTemplateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveTemplate();
            }
        });
        
        loadTemplateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadTemplate();
            }
        });
    }
    
    // 更新预览
    private void updatePreview() {
        updatePreview(false);
    }
    
    // 更新预览，forceUpdatePosition为true时强制更新位置
    private void updatePreview(boolean forceUpdatePosition) {
        // 获取当前文本水印设置
        String text = textWatermarkField.getText();
        String fontName = (String) fontComboBox.getSelectedItem();
        int fontSize = (Integer) fontSizeSpinner.getValue();
        boolean isBold = boldCheckBox.isSelected();
        boolean isItalic = italicCheckBox.isSelected();
        
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
        
        // 更新预览面板
        previewPanel.setTextWatermark(text, font, textColor);
        previewPanel.setOpacity(watermarkOpacity);
        previewPanel.setScale(1.0); // 文本水印不支持缩放，使用默认值1.0
        previewPanel.setRotation(rotation);
        previewPanel.setPresetPosition(position);
        
        // 根据参数决定是否强制更新位置
        if (forceUpdatePosition) {
            previewPanel.setUsePresetPosition(true);
        } else {
            // 只有在使用预设位置时才设置为true，否则保持用户自定义的位置
            if (previewPanel.isUsePresetPosition()) {
                previewPanel.setUsePresetPosition(true);
            }
        }
    }
    
    // 保存为模板
    private void saveTemplate() {
        // 更新预览以确保所有设置都是最新的
        updatePreview();
        
        String templateName = JOptionPane.showInputDialog(this, "请输入模板名称:", "保存模板", JOptionPane.QUESTION_MESSAGE);
        if (templateName == null || templateName.trim().isEmpty()) {
            return;
        }
        
        com.watermark.model.WatermarkTemplate template = new com.watermark.model.WatermarkTemplate();
        template.setName(templateName.trim());
        template.setType(com.watermark.model.WatermarkTemplate.TemplateType.TEXT);
        
        // 设置文本水印参数
        template.setTextWatermark(textWatermarkField.getText());
        template.setFontName((String) fontComboBox.getSelectedItem());
        template.setFontSize((Integer) fontSizeSpinner.getValue());
        template.setBold(boldCheckBox.isSelected());
        template.setItalic(italicCheckBox.isSelected());
        template.setTextColor(TemplateManager.cloneColor(textColor));
        template.setTextOpacity(opacitySlider.getValue());
        template.setHasShadow(shadowCheckBox.isSelected());
        template.setHasOutline(outlineCheckBox.isSelected());
        
        // 设置通用参数
        template.setRotation(rotationSlider.getValue());
        template.setPosition(com.watermark.model.WatermarkTemplate.WatermarkPosition.values()[positionComboBox.getSelectedIndex()]);
        
        // 设置自定义位置（如果有的话）
        if (!previewPanel.isUsePresetPosition()) {
            template.setCustomPosition(TemplateManager.clonePoint(previewPanel.getWatermarkPosition()));
        }
        
        // 保存模板
        if (TemplateManager.getInstance().saveTemplate(template)) {
            JOptionPane.showMessageDialog(this, "模板保存成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "模板保存失败！", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // 加载模板
    private void loadTemplate() {
        Frame parentFrame = JOptionPane.getFrameForComponent(this);
        TemplateManagerDialog dialog = new TemplateManagerDialog(parentFrame);
        dialog.setVisible(true);
        
        com.watermark.model.WatermarkTemplate template = dialog.getSelectedTemplate();
        if (template != null && template.getType() == com.watermark.model.WatermarkTemplate.TemplateType.TEXT) {
            loadTemplateData(template);
        }
    }
    
    // 加载模板数据到界面
    private void loadTemplateData(com.watermark.model.WatermarkTemplate template) {
        // 加载文本水印参数
        if (template.getTextWatermark() != null) {
            textWatermarkField.setText(template.getTextWatermark());
        }
        
        if (template.getFontName() != null) {
            fontComboBox.setSelectedItem(template.getFontName());
        }
        
        fontSizeSpinner.setValue(template.getFontSize());
        boldCheckBox.setSelected(template.isBold());
        italicCheckBox.setSelected(template.isItalic());
        
        if (template.getTextColor() != null) {
            textColor = template.getTextColor();
            colorButton.setBackground(textColor);
            colorButton.setForeground(textColor);
        }
        
        opacitySlider.setValue(template.getTextOpacity());
        shadowCheckBox.setSelected(template.isHasShadow());
        outlineCheckBox.setSelected(template.isHasOutline());
        
        // 加载通用参数
        rotationSlider.setValue((int) template.getRotation());
        
        if (template.getPosition() != null) {
            positionComboBox.setSelectedIndex(template.getPosition().ordinal());
        }
        
        // 加载自定义位置（如果有的话）
        if (template.getCustomPosition() != null) {
            previewPanel.setUsePresetPosition(false);
            previewPanel.setWatermarkPosition(template.getCustomPosition());
        } else {
            previewPanel.setUsePresetPosition(true);
        }
        
        // 更新预览
        updatePreview();
    }
    
    // 加载上次会话
    private void loadLastSession() {
        com.watermark.model.WatermarkTemplate template = TemplateManager.getInstance().loadLastSession();
        if (template != null && template.getType() == com.watermark.model.WatermarkTemplate.TemplateType.TEXT) {
            loadTemplateData(template);
        }
    }
    
    // 保存当前会话
    private void saveCurrentSession() {
        com.watermark.model.WatermarkTemplate template = new com.watermark.model.WatermarkTemplate();
        template.setType(com.watermark.model.WatermarkTemplate.TemplateType.TEXT);
        
        // 设置文本水印参数
        template.setTextWatermark(textWatermarkField.getText());
        template.setFontName((String) fontComboBox.getSelectedItem());
        template.setFontSize((Integer) fontSizeSpinner.getValue());
        template.setBold(boldCheckBox.isSelected());
        template.setItalic(italicCheckBox.isSelected());
        template.setTextColor(TemplateManager.cloneColor(textColor));
        template.setTextOpacity(opacitySlider.getValue());
        template.setHasShadow(shadowCheckBox.isSelected());
        template.setHasOutline(outlineCheckBox.isSelected());
        
        // 设置通用参数
        template.setRotation(rotationSlider.getValue());
        template.setPosition(com.watermark.model.WatermarkTemplate.WatermarkPosition.values()[positionComboBox.getSelectedIndex()]);
        
        // 设置自定义位置（如果有的话）
        if (!previewPanel.isUsePresetPosition()) {
            template.setCustomPosition(TemplateManager.clonePoint(previewPanel.getWatermarkPosition()));
        }
        
        TemplateManager.getInstance().saveCurrentSession(template);
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public String getWatermarkText() {
        return watermarkText;
    }

    public String getFontName() {
        return fontName;
    }

    public int getFontSize() {
        return fontSize;
    }

    public boolean isBold() {
        return isBold;
    }

    public boolean isItalic() {
        return isItalic;
    }

    public Color getTextColor() {
        return textColor;
    }

    public int getWatermarkOpacity() {
        return watermarkOpacity;
    }

    public boolean hasShadow() {
        return hasShadow;
    }

    public boolean hasOutline() {
        return hasOutline;
    }
    
    // 高级设置相关getter方法
    public double getRotation() {
        return rotation;
    }

    public WatermarkPosition getPosition() {
        return position;
    }
    
    // 获取水印位置
    public Point getWatermarkPosition() {
        // 如果使用预设位置，返回null，否则返回自定义位置
        if (previewPanel.isUsePresetPosition()) {
            return null;
        } else {
            return previewPanel.getWatermarkPosition();
        }
    }
}