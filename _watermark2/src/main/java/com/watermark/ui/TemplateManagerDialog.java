package com.watermark.ui;

import com.watermark.model.WatermarkTemplate;
import com.watermark.utils.TemplateManager;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;

public class TemplateManagerDialog extends JDialog {
    private JTable templateTable;
    private TemplateTableModel tableModel;
    private JButton loadButton;
    private JButton deleteButton;
    private JButton closeButton;
    private List<WatermarkTemplate> templates;
    private WatermarkTemplate selectedTemplate;
    
    public TemplateManagerDialog(Frame parent) {
        super(parent, "水印模板管理", true);
        initializeComponents();
        layoutComponents();
        setupEventHandlers();
        loadTemplates();
        
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        pack();
        setResizable(true);
        setLocationRelativeTo(parent);
    }
    
    private void initializeComponents() {
        tableModel = new TemplateTableModel();
        templateTable = new JTable(tableModel);
        templateTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        templateTable.getSelectionModel().addListSelectionListener(e -> {
            int selectedRow = templateTable.getSelectedRow();
            deleteButton.setEnabled(selectedRow >= 0);
            loadButton.setEnabled(selectedRow >= 0);
        });
        
        // 设置表格外观
        templateTable.setRowHeight(25);
        templateTable.getTableHeader().setPreferredSize(new Dimension(0, 30));
        templateTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 12));
        
        loadButton = new JButton("加载选中模板");
        loadButton.setEnabled(false);
        loadButton.setPreferredSize(new Dimension(120, 30));
        deleteButton = new JButton("删除选中模板");
        deleteButton.setEnabled(false);
        deleteButton.setPreferredSize(new Dimension(120, 30));
        closeButton = new JButton("关闭");
        closeButton.setPreferredSize(new Dimension(80, 30));
    }
    
    private void layoutComponents() {
        setLayout(new BorderLayout());
        
        // 表格面板
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("已保存的模板"));
        tablePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("已保存的模板"),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        JScrollPane scrollPane = new JScrollPane(templateTable);
        scrollPane.setPreferredSize(new Dimension(600, 250));
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        add(tablePanel, BorderLayout.CENTER);
        
        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.add(loadButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(closeButton);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 10));
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void setupEventHandlers() {
        loadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = templateTable.getSelectedRow();
                if (selectedRow >= 0) {
                    selectedTemplate = templates.get(selectedRow);
                    templateTable.clearSelection(); // 清除选中状态，避免dispose时出现KeyboardManager异常
                    
                    // 使用invokeLater延迟dispose操作，确保当前事件处理完成
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            dispose();
                        }
                    });
                }
            }
        });
        
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = templateTable.getSelectedRow();
                if (selectedRow >= 0) {
                    WatermarkTemplate template = templates.get(selectedRow);
                    int result = JOptionPane.showConfirmDialog(
                        TemplateManagerDialog.this,
                        "确定要删除模板 \"" + template.getName() + "\" 吗？",
                        "确认删除",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                    );
                    
                    if (result == JOptionPane.YES_OPTION) {
                        TemplateManager.getInstance().deleteTemplate(template.getName());
                        loadTemplates(); // 重新加载模板列表
                    }
                }
            }
        });
        
        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectedTemplate = null;
                templateTable.clearSelection(); // 清除选中状态，避免dispose时出现KeyboardManager异常
                
                // 使用invokeLater延迟dispose操作，确保当前事件处理完成
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        dispose();
                    }
                });
            }
        });
    }
    
    private void loadTemplates() {
        templates = TemplateManager.getInstance().loadAllTemplates();
        tableModel.setTemplates(templates);
        deleteButton.setEnabled(false);
        loadButton.setEnabled(false);
    }
    
    public WatermarkTemplate getSelectedTemplate() {
        return selectedTemplate;
    }
    
    // 表格模型
    private static class TemplateTableModel extends AbstractTableModel {
        private List<WatermarkTemplate> templates;
        private final String[] columnNames = {"模板名称", "类型", "水印内容", "位置"};
        
        public void setTemplates(List<WatermarkTemplate> templates) {
            this.templates = templates;
            fireTableDataChanged();
        }
        
        @Override
        public int getRowCount() {
            return templates != null ? templates.size() : 0;
        }
        
        @Override
        public int getColumnCount() {
            return columnNames.length;
        }
        
        @Override
        public String getColumnName(int column) {
            return columnNames[column];
        }
        
        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (templates == null || rowIndex >= templates.size()) {
                return null;
            }
            
            WatermarkTemplate template = templates.get(rowIndex);
            switch (columnIndex) {
                case 0: // 模板名称
                    return template.getName();
                case 1: // 类型
                    return template.getType() == WatermarkTemplate.TemplateType.TEXT ? "文本" : "图片";
                case 2: // 水印内容
                    if (template.getType() == WatermarkTemplate.TemplateType.TEXT) {
                        return template.getTextWatermark();
                    } else {
                        return template.getImagePath() != null ? 
                            new File(template.getImagePath()).getName() : "无图片";
                    }
                case 3: // 位置
                    if (template.getCustomPosition() != null) {
                        return "自定义位置";
                    } else if (template.getPosition() != null) {
                        switch (template.getPosition()) {
                            case TOP_LEFT: return "左上角";
                            case TOP_CENTER: return "顶部居中";
                            case TOP_RIGHT: return "右上角";
                            case CENTER_LEFT: return "左侧居中";
                            case CENTER: return "正中央";
                            case CENTER_RIGHT: return "右侧居中";
                            case BOTTOM_LEFT: return "左下角";
                            case BOTTOM_CENTER: return "底部居中";
                            case BOTTOM_RIGHT: return "右下角";
                            default: return "未知";
                        }
                    } else {
                        return "未设置";
                    }
                default:
                    return null;
            }
        }
    }
}