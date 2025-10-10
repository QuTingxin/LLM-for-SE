package com.watermark.ui;

import com.watermark.model.ImageItem;
import com.watermark.ui.WatermarkPreviewPanel.WatermarkPosition;
import com.watermark.utils.TemplateManager;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;

public class ImageWatermarkDialog extends JDialog {
    private boolean confirmed = false;
    private File watermarkFile = null;
    private BufferedImage watermarkImage = null;
    private ImageItem previewImageItem;
    
    // 图片水印设置
    private JLabel imagePreviewLabel;
    private JButton selectImageButton;
    private JSlider opacitySlider;
    private JLabel opacityLabel;
    
    // 高级设置控件
    private JSlider scaleSlider;
    private JSlider rotationSlider;
    private JComboBox<String> positionComboBox;
    private WatermarkPreviewPanel previewPanel;
    
    // 模板管理控件
    private JButton saveTemplateButton;
    private JButton loadTemplateButton;
    
    // 默认值
    private int watermarkOpacity = 100; // 0-100%
    
    // 高级设置默认值
    private double rotation = 0.0; // 旋转角度
    private WatermarkPosition position = WatermarkPosition.BOTTOM_RIGHT;

    public ImageWatermarkDialog(Frame parent, ImageItem previewImage) {
        super(parent, "图片水印设置", true);
        this.previewImageItem = previewImage;
        initializeComponents();
        layoutComponents();
        setupEventHandlers();
        
        // 设置预览图片
        if (previewImageItem != null) {
            previewPanel.setImageItem(previewImageItem);
        }
        
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        pack();
        setResizable(true);
        setLocationRelativeTo(parent);
    }

    private void initializeComponents() {
        // 图片预览标签
        imagePreviewLabel = new JLabel("无图片", SwingConstants.CENTER);
        imagePreviewLabel.setPreferredSize(new Dimension(200, 150));
        imagePreviewLabel.setBorder(BorderFactory.createEtchedBorder());
        
        // 选择图片按钮
        selectImageButton = new JButton("选择图片");
        selectImageButton.setPreferredSize(new Dimension(120, 35));
        
        // 透明度滑块
        opacitySlider = new JSlider(0, 100, watermarkOpacity);
        opacitySlider.setMajorTickSpacing(20);
        opacitySlider.setMinorTickSpacing(5);
        opacitySlider.setPaintTicks(true);
        opacitySlider.setPaintLabels(true);
        opacitySlider.setPreferredSize(new Dimension(250, 50));
        
        opacityLabel = new JLabel("透明度: " + watermarkOpacity + "%");
        opacityLabel.setPreferredSize(new Dimension(120, 30));
        
        // 高级设置组件
        previewPanel = new WatermarkPreviewPanel();
        previewPanel.setPreferredSize(new Dimension(450, 350));
        previewPanel.setBorder(BorderFactory.createTitledBorder("预览"));
        
        // 缩放滑块（用于预览中的缩放）
        scaleSlider = new JSlider(10, 500, 100); // 默认值改为100
        scaleSlider.setMajorTickSpacing(50);
        scaleSlider.setMinorTickSpacing(10);
        scaleSlider.setPaintTicks(true);
        scaleSlider.setPaintLabels(true);
        scaleSlider.setPreferredSize(new Dimension(250, 50));
        
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
            BorderFactory.createTitledBorder("图片水印设置"),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        
        // 图片选择和预览
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 1;
        mainPanel.add(new JLabel("水印图片:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        mainPanel.add(imagePreviewLabel, gbc);
        
        // 选择图片按钮
        gbc.gridx = 3; gbc.gridwidth = 1;
        mainPanel.add(selectImageButton, gbc);
        
        // 透明度设置
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1; // 从2改为1
        mainPanel.add(opacityLabel, gbc);
        gbc.gridx = 1; gbc.gridwidth = 3;
        mainPanel.add(opacitySlider, gbc);
        
        // 高级设置 - 缩放（用于预览中的缩放）
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1; // 从3改为2
        mainPanel.add(new JLabel("预览缩放:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3;
        mainPanel.add(scaleSlider, gbc);
        
        // 高级设置 - 旋转
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 1; // 从4改为3
        mainPanel.add(new JLabel("旋转:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3;
        mainPanel.add(rotationSlider, gbc);
        
        // 高级设置 - 位置
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 1; // 从5改为4
        mainPanel.add(new JLabel("位置:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3;
        mainPanel.add(positionComboBox, gbc);
        
        // 模板管理按钮
        JPanel templateButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        templateButtonPanel.add(saveTemplateButton);
        templateButtonPanel.add(loadTemplateButton);
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 4; // 从6改为5
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
        selectImageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectWatermarkImage();
            }
        });
        
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (watermarkImage != null) {
                    confirmed = true;
                    watermarkOpacity = opacitySlider.getValue();
                    
                    // 高级设置值
                    rotation = rotationSlider.getValue();
                    position = WatermarkPosition.values()[positionComboBox.getSelectedIndex()];
                    
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(ImageWatermarkDialog.this, 
                        "请先选择一张水印图片", "提示", JOptionPane.WARNING_MESSAGE);
                }
            }
        });
        
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                confirmed = false;
                dispose();
            }
        });
        
        // 透明度滑块事件处理
        opacitySlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                watermarkOpacity = opacitySlider.getValue();
                opacityLabel.setText(String.format("透明度: %3d%%", watermarkOpacity));
                updatePreview();
            }
        });
    }

    private void setupEventHandlers() {
        // 预览缩放滑块事件处理
        scaleSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                // 确保即使在调整过程中也更新预览
                updatePreview(false); // 不强制更新位置
            }
        });
        
        // 旋转滑块事件处理
        rotationSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                // 确保即使在调整过程中也更新预览
                rotation = rotationSlider.getValue();
                updatePreview(false); // 不强制更新位置
            }
        });
        
        // 位置选择事件处理
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
    
    private void selectWatermarkImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "图片文件 (PNG, JPEG, BMP, TIFF)", "png", "jpg", "jpeg", "bmp", "tiff", "tif"));

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                watermarkFile = fileChooser.getSelectedFile();
                watermarkImage = ImageIO.read(watermarkFile);
                
                // 显示预览
                displayImagePreview();
                updatePreview();
                
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, 
                    "加载图片时发生错误：" + e.getMessage(), 
                    "错误", 
                    JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }
    
    private void displayImagePreview() {
        if (watermarkImage != null) {
            // 计算缩略图尺寸，保持宽高比
            int originalWidth = watermarkImage.getWidth();
            int originalHeight = watermarkImage.getHeight();
            int maxWidth = 180;
            int maxHeight = 130;
            
            double scaleWidth = (double) maxWidth / originalWidth;
            double scaleHeight = (double) maxHeight / originalHeight;
            double scale = Math.min(scaleWidth, scaleHeight);
            
            int scaledWidth = (int) (originalWidth * scale);
            int scaledHeight = (int) (originalHeight * scale);
            
            // 创建缩略图
            Image scaledImage = watermarkImage.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH);
            imagePreviewLabel.setIcon(new ImageIcon(scaledImage));
            imagePreviewLabel.setText("");
        }
    }
    
    // 更新预览
    private void updatePreview() {
        updatePreview(false);
    }
    
    // 更新预览，forceUpdatePosition为true时强制更新位置
    private void updatePreview(boolean forceUpdatePosition) {
        if (watermarkImage != null) {
            double previewScale = scaleSlider.getValue();
            previewPanel.setWatermarkImage(watermarkImage);
            previewPanel.setOpacity(watermarkOpacity);
            previewPanel.setScale(previewScale / 100.0);
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
    }
    
    // 保存为模板
    private void saveTemplate() {
        if (watermarkImage == null) {
            JOptionPane.showMessageDialog(this, "请先选择一张水印图片", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // 更新预览以确保所有设置都是最新的
        updatePreview();
        
        String templateName = JOptionPane.showInputDialog(this, "请输入模板名称:", "保存模板", JOptionPane.QUESTION_MESSAGE);
        if (templateName == null || templateName.trim().isEmpty()) {
            return;
        }
        
        com.watermark.model.WatermarkTemplate template = new com.watermark.model.WatermarkTemplate();
        template.setName(templateName.trim());
        template.setType(com.watermark.model.WatermarkTemplate.TemplateType.IMAGE);
        
        // 设置图片水印参数
        if (watermarkFile != null) {
            template.setImagePath(watermarkFile.getAbsolutePath());
        }
        template.setImageOpacity(opacitySlider.getValue());
        
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
        if (template != null && template.getType() == com.watermark.model.WatermarkTemplate.TemplateType.IMAGE) {
            loadTemplateData(template);
        }
    }
    
    // 加载模板数据到界面
    private void loadTemplateData(com.watermark.model.WatermarkTemplate template) {
        // 加载图片水印参数
        if (template.getImagePath() != null) {
            try {
                File file = new File(template.getImagePath());
                if (file.exists()) {
                    watermarkFile = file;
                    watermarkImage = ImageIO.read(watermarkFile);
                    displayImagePreview();
                } else {
                    JOptionPane.showMessageDialog(this, "找不到图片文件: " + template.getImagePath(), "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "加载图片时发生错误: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
                return;
            }
        }
        
        opacitySlider.setValue(template.getImageOpacity());
        
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

    public boolean isConfirmed() {
        return confirmed;
    }

    public BufferedImage getWatermarkImage() {
        return watermarkImage;
    }

    public double getScale() {
        return scaleSlider.getValue(); // 返回预览缩放滑块的值
    }

    public int getWatermarkOpacity() {
        return watermarkOpacity;
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