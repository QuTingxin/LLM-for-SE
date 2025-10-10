import os
from pathlib import Path
from PyQt5.QtWidgets import (QWidget, QVBoxLayout, QHBoxLayout, QGroupBox, 
                             QLabel, QLineEdit, QComboBox, QSlider, QSpinBox,
                             QPushButton, QColorDialog, QFileDialog, QCheckBox,
                             QGridLayout, QTabWidget, QButtonGroup, QRadioButton,
                             QListWidget, QMessageBox, QInputDialog)
from PyQt5.QtCore import Qt, pyqtSignal
from PyQt5.QtGui import QColor, QFont

class SettingsPanel(QWidget):
    settings_changed = pyqtSignal(dict)
    export_requested = pyqtSignal()
    
    def __init__(self, config_manager):
        super().__init__()
        self.config_manager = config_manager
        self.current_color = QColor(255, 255, 255)  # 默认白色
        self.init_ui()
    
    def init_ui(self):
        layout = QVBoxLayout(self)
        
        # 创建标签页
        tab_widget = QTabWidget()
        
        # 水印设置标签页
        watermark_tab = self.create_watermark_tab()
        tab_widget.addTab(watermark_tab, "水印设置")
        
        # 导出设置标签页
        export_tab = self.create_export_tab()
        tab_widget.addTab(export_tab, "导出设置")
        
        # 模板管理标签页
        template_tab = self.create_template_tab()
        tab_widget.addTab(template_tab, "模板管理")
        
        layout.addWidget(tab_widget)
        
        # 导出按钮
        self.export_btn = QPushButton("导出所有图片")
        self.export_btn.clicked.connect(self.export_requested.emit)
        self.export_btn.setStyleSheet("QPushButton { background-color: #4CAF50; color: white; font-size: 14px; padding: 10px; }")
        layout.addWidget(self.export_btn)
    
    def create_watermark_tab(self):
        tab = QWidget()
        layout = QVBoxLayout(tab)
        
        # 水印类型选择
        type_group = QGroupBox("水印类型")
        type_layout = QHBoxLayout(type_group)
        
        self.watermark_type = QButtonGroup()
        self.text_radio = QRadioButton("文本水印")
        self.image_radio = QRadioButton("图片水印")
        
        self.text_radio.setChecked(True)
        self.watermark_type.addButton(self.text_radio, 0)
        self.watermark_type.addButton(self.image_radio, 1)
        self.watermark_type.buttonClicked.connect(self.on_watermark_type_changed)
        
        type_layout.addWidget(self.text_radio)
        type_layout.addWidget(self.image_radio)
        
        # 文本水印设置
        self.text_group = QGroupBox("文本设置")
        text_layout = QGridLayout(self.text_group)
        
        text_layout.addWidget(QLabel("水印文字:"), 0, 0)
        self.text_content = QLineEdit("水印文字")
        self.text_content.textChanged.connect(self.on_settings_changed)
        text_layout.addWidget(self.text_content, 0, 1)
        
        text_layout.addWidget(QLabel("字体大小:"), 1, 0)
        self.font_size = QSpinBox()
        self.font_size.setRange(10, 200)
        self.font_size.setValue(36)
        self.font_size.valueChanged.connect(self.on_settings_changed)
        text_layout.addWidget(self.font_size, 1, 1)
        
        text_layout.addWidget(QLabel("颜色:"), 2, 0)
        self.color_btn = QPushButton()
        self.color_btn.setStyleSheet("background-color: white;")
        self.color_btn.clicked.connect(self.choose_color)
        text_layout.addWidget(self.color_btn, 2, 1)
        
        text_layout.addWidget(QLabel("透明度:"), 3, 0)
        self.text_opacity = QSlider(Qt.Horizontal)
        self.text_opacity.setRange(0, 100)
        self.text_opacity.setValue(80)
        self.text_opacity.valueChanged.connect(self.on_opacity_changed)
        text_layout.addWidget(self.text_opacity, 3, 1)
        self.text_opacity_label = QLabel("80%")
        text_layout.addWidget(self.text_opacity_label, 3, 2)
        
        text_layout.addWidget(QLabel("旋转角度:"), 4, 0)
        self.text_rotation = QSpinBox()
        self.text_rotation.setRange(-180, 180)
        self.text_rotation.setValue(0)
        self.text_rotation.valueChanged.connect(self.on_settings_changed)
        text_layout.addWidget(self.text_rotation, 4, 1)
        
        # 图片水印设置
        self.image_group = QGroupBox("图片设置")
        self.image_group.setVisible(False)
        image_layout = QGridLayout(self.image_group)
        
        image_layout.addWidget(QLabel("水印图片:"), 0, 0)
        self.image_path = QLineEdit()
        self.image_path.setReadOnly(True)
        image_layout.addWidget(self.image_path, 0, 1)
        
        self.image_choose_btn = QPushButton("选择图片")
        self.image_choose_btn.clicked.connect(self.choose_image)
        image_layout.addWidget(self.image_choose_btn, 0, 2)
        
        image_layout.addWidget(QLabel("缩放比例:"), 1, 0)
        self.image_scale = QSpinBox()
        self.image_scale.setRange(10, 500)
        self.image_scale.setValue(100)
        self.image_scale.setSuffix