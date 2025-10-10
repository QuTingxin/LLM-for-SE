package com.watermark.ui;

import com.watermark.model.ImageItem;
import com.watermark.ui.WatermarkPreviewPanel.WatermarkPosition;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

public class AdvancedWatermarkDialog extends JDialog {
    private boolean confirmed = false;
    
    // 预览面板
    private WatermarkPreviewPanel previewPanel;
    
    // 水印设置控件
    private JSpinner scaleSpinner;
    private JSlider opacitySlider;
    private JSlider rotationSlider;
    private JComboBox<String> positionComboBox;
    private JLabel opacityLabel;
    private JLabel rotationLabel;
    
    // 水印参数
    private double scale = 100.0; // 百分比
    private int watermarkOpacity = 100; // 0-100%
    private double rotation = 0.0; // 旋转角度
    private WatermarkPosition position = WatermarkPosition.BOTTOM_RIGHT;
    
    // 水印类型标识
    private boolean isImageWatermark = false;
    private BufferedImage watermarkImage = null;
    private String textWatermark = null;
    private Font textFont = null;
    private Color textColor = Color.BLACK;

    public AdvancedWatermarkDialog(Frame parent, ImageItem imageItem) {
        super(parent, "高级水印设置", true);
        initializeComponents();
        layoutComponents();
        setupEventHandlers();
        
        // 设置预览图片
        if (imageItem != null) {
            previewPanel.setImageItem(imageItem);
        }
        
        // 如果是图片水印，设置图片水印
        if (isImageWatermark && watermarkImage != null) {
            previewPanel.setWatermarkImage(watermarkImage);
        } 
        // 如果是文本水印，设置文本水印
        else if (!isImageWatermark && textWatermark != null && textFont != null) {
            previewPanel.setTextWatermark(textWatermark, textFont, textColor);
        }
        
        // 更新预览参数
        updatePreview();
        
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        pack();
        setResizable(true);
        setLocationRelativeTo(parent);
    }

    private void initializeComponents() {
        // 创建预览面板
        previewPanel = new WatermarkPreviewPanel();
        previewPanel.setPreferredSize(new Dimension(500, 400));
        previewPanel.setBorder(BorderFactory.createTitledBorder("预览"));
        
        // 缩放比例选择
        scaleSpinner = new JSpinner(new SpinnerNumberModel(scale, 10, 500, 5));
        
        // 透明度滑块
        opacitySlider = new JSlider(0, 100, watermarkOpacity);
        opacitySlider.setMajorTickSpacing(25); // 减少标签数量，避免重叠
        opacitySlider.setMinorTickSpacing(5);
        opacitySlider.setPaintTicks(true);
        opacitySlider.setPaintLabels(true);
        opacitySlider.setPreferredSize(new Dimension(250, 50)); // 增加宽度和高度
        
        opacityLabel = new JLabel("透明度: " + watermarkOpacity + "%");
        opacityLabel.setPreferredSize(new Dimension(120, 25)); // 固定标签大小
        
        // 旋转滑块
        rotationSlider = new JSlider(-180, 180, (int)rotation);
        rotationSlider.setMajorTickSpacing(90); // 减少标签数量，避免重叠
        rotationSlider.setMinorTickSpacing(15);
        rotationSlider.setPaintTicks(true);
        rotationSlider.setPaintLabels(true);
        rotationSlider.setPreferredSize(new Dimension(250, 50)); // 增加宽度和高度
        
        rotationLabel = new JLabel("旋转: " + (int)rotation + "°");
        rotationLabel.setPreferredSize(new Dimension(120, 25)); // 固定标签大小
        
        // 位置选择下拉框
        String[] positions = {
            "左上角", "顶部居中", "右上角",
            "左侧居中", "正中央", "右侧居中",
            "左下角", "底部居中", "右下角"
        };
        positionComboBox = new JComboBox<>(positions);
        positionComboBox.setSelectedIndex(8); // 默认右下角
    }

    private void layoutComponents() {
        setLayout(new BorderLayout());
        
        // 左侧控制面板
        JPanel controlPanel = new JPanel(new GridBagLayout());
        controlPanel.setBorder(BorderFactory.createTitledBorder("水印设置"));
        controlPanel.setPreferredSize(new Dimension(350, 0)); // 增加控制面板宽度
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // 缩放设置
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 1;
        controlPanel.add(new JLabel("缩放比例:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 1;
        controlPanel.add(scaleSpinner, gbc);
        gbc.gridx = 2; gbc.gridwidth = 1;
        controlPanel.add(new JLabel("%"), gbc);
        
        // 透明度设置
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        controlPanel.add(opacityLabel, gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        controlPanel.add(opacitySlider, gbc);
        
        // 旋转设置
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1;
        controlPanel.add(rotationLabel, gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        controlPanel.add(rotationSlider, gbc);
        
        // 位置设置
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 1;
        controlPanel.add(new JLabel("位置:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        controlPanel.add(positionComboBox, gbc);
        
        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton okButton = new JButton("确定");
        JButton cancelButton = new JButton("取消");
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 3;
        controlPanel.add(buttonPanel, gbc);
        
        // 添加到主面板
        add(controlPanel, BorderLayout.WEST);
        add(previewPanel, BorderLayout.CENTER);
        
        // 按钮事件处理
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                confirmed = true;
                scale = (Double) scaleSpinner.getValue();
                watermarkOpacity = opacitySlider.getValue();
                rotation = rotationSlider.getValue();
                position = WatermarkPosition.values()[positionComboBox.getSelectedIndex()];
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
        // 缩放比例变化事件
        scaleSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                scale = (Double) scaleSpinner.getValue();
                updatePreview();
            }
        });
        
        // 透明度滑块事件
        opacitySlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                watermarkOpacity = opacitySlider.getValue();
                opacityLabel.setText(String.format("透明度: %3d%%", watermarkOpacity)); // 格式化文本
                updatePreview();
            }
        });
        
        // 旋转滑块事件
        rotationSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                rotation = rotationSlider.getValue();
                rotationLabel.setText(String.format("旋转: %3d°", (int)rotation)); // 格式化文本，转换为整数
                updatePreview();
            }
        });
        
        // 位置选择事件
        positionComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                position = WatermarkPosition.values()[positionComboBox.getSelectedIndex()];
                updatePreview();
            }
        });
    }
    
    // 更新预览
    private void updatePreview() {
        previewPanel.setScale(scale / 100.0);
        previewPanel.setOpacity(watermarkOpacity);
        previewPanel.setRotation(rotation);
        previewPanel.setPresetPosition(position);
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public double getScale() {
        return scale;
    }

    public int getWatermarkOpacity() {
        return watermarkOpacity;
    }

    public double getRotation() {
        return rotation;
    }

    public WatermarkPosition getPosition() {
        return position;
    }
    
    // 设置为图片水印模式
    public void setImageWatermark(BufferedImage watermarkImage) {
        this.isImageWatermark = true;
        this.watermarkImage = watermarkImage;
        if (previewPanel != null) {
            previewPanel.setWatermarkImage(watermarkImage);
        }
    }
    
    // 设置为文本水印模式
    public void setTextWatermark(String text, Font font, Color color) {
        this.isImageWatermark = false;
        this.textWatermark = text;
        this.textFont = font;
        this.textColor = color;
        if (previewPanel != null) {
            previewPanel.setTextWatermark(text, font, color);
        }
    }
}