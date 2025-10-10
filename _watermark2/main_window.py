import os
import sys
from pathlib import Path
from PyQt5.QtWidgets import (QMainWindow, QWidget, QVBoxLayout, QHBoxLayout, 
                             QSplitter, QListWidget, QListWidgetItem, QLabel, 
                             QPushButton, QFileDialog, QMessageBox, QTabWidget,
                             QGroupBox, QScrollArea, QProgressDialog)
from PyQt5.QtCore import Qt, QSize
from PyQt5.QtGui import QPixmap, QFont

from watermark_processor import WatermarkProcessor
from file_manager import FileManager
from config_manager import ConfigManager
from preview_panel import PreviewPanel
from settings_panel import SettingsPanel

class MainWindow(QMainWindow):
    def __init__(self, app_data_dir: str):
        super().__init__()
        self.app_data_dir = app_data_dir
        self.file_manager = FileManager()
        self.watermark_processor = WatermarkProcessor()
        self.config_manager = ConfigManager(app_data_dir)
        
        self.current_watermark_settings = {
            'type': 'text',  # 'text' 或 'image'
            'text': {
                'content': '水印文字',
                'font_size': 36,
                'color': (255, 255, 255),
                'opacity': 80,
                'rotation': 0,
                'font_family': 'Arial'
            },
            'image': {
                'path': '',
                'scale': 1.0,
                'opacity': 80,
                'rotation': 0
            },
            'position': (50, 50),  # 默认位置
            'layout': 'custom'  # 或 'top-left', 'top-right' 等
        }
        
        self.init_ui()
        self.load_last_settings()
    
    def init_ui(self):
        self.setWindowTitle("水印工具 v1.0")
        self.setGeometry(100, 100, 1200, 800)
        
        # 创建中央部件
        central_widget = QWidget()
        self.setCentralWidget(central_widget)
        
        # 主布局
        main_layout = QHBoxLayout(central_widget)
        
        # 左侧文件列表区域
        left_panel = self.create_left_panel()
        
        # 右侧预览和设置区域
        right_panel = self.create_right_panel()
        
        # 使用分割器
        splitter = QSplitter(Qt.Horizontal)
        splitter.addWidget(left_panel)
        splitter.addWidget(right_panel)
        splitter.setSizes([300, 900])
        
        main_layout.addWidget(splitter)
        
        # 创建菜单栏
        self.create_menu_bar()
        
        # 创建状态栏
        self.statusBar().showMessage("就绪")
    
    def create_left_panel(self):
        """创建左侧文件列表面板"""
        panel = QWidget()
        layout = QVBoxLayout(panel)
        
        # 文件操作按钮
        btn_layout = QHBoxLayout()
        
        self.btn_add_files = QPushButton("添加文件")
        self.btn_add_files.clicked.connect(self.add_files)
        
        self.btn_add_folder = QPushButton("添加文件夹")
        self.btn_add_folder.clicked.connect(self.add_folder)
        
        self.btn_clear = QPushButton("清空列表")
        self.btn_clear.clicked.connect(self.clear_files)
        
        btn_layout.addWidget(self.btn_add_files)
        btn_layout.addWidget(self.btn_add_folder)
        btn_layout.addWidget(self.btn_clear)
        
        # 文件列表
        self.file_list = QListWidget()
        self.file_list.itemClicked.connect(self.on_file_selected)
        
        # 文件计数
        self.file_count_label = QLabel("已选择 0 个文件")
        
        layout.addLayout(btn_layout)
        layout.addWidget(QLabel("文件列表:"))
        layout.addWidget(self.file_list)
        layout.addWidget(self.file_count_label)
        
        return panel
    
    def create_right_panel(self):
        """创建右侧预览和设置面板"""
        panel = QWidget()
        layout = QVBoxLayout(panel)
        
        # 创建标签页
        tab_widget = QTabWidget()
        
        # 预览标签页
        self.preview_panel = PreviewPanel()
        tab_widget.addTab(self.preview_panel, "预览")
        
        # 设置标签页
        self.settings_panel = SettingsPanel(self.config_manager)
        self.settings_panel.settings_changed.connect(self.on_settings_changed)
        self.settings_panel.export_requested.connect(self.export_images)
        tab_widget.addTab(self.settings_panel, "水印设置")
        
        layout.addWidget(tab_widget)
        
        return panel
    
    def create_menu_bar(self):
        """创建菜单栏"""
        menubar = self.menuBar()
        
        # 文件菜单
        file_menu = menubar.addMenu('文件')
        
        import_files_action = file_menu.addAction('导入文件')
        import_files_action.triggered.connect(self.add_files)
        
        import_folder_action = file_menu.addAction('导入文件夹')
        import_folder_action.triggered.connect(self.add_folder)
        
        file_menu.addSeparator()
        
        export_action = file_menu.addAction('导出图片')
        export_action.triggered.connect(self.export_images)
        
        file_menu.addSeparator()
        
        exit_action = file_menu.addAction('退出')
        exit_action.triggered.connect(self.close)
        
        # 模板菜单
        template_menu = menubar.addMenu('模板')
        
        save_template_action = template_menu.addAction('保存当前模板')
        save_template_action.triggered.connect(self.save_template)
        
        load_template_action = template_menu.addAction('加载模板')
        load_template_action.triggered.connect(self.load_template)
    
    def add_files(self):
        """添加文件"""
        files, _ = QFileDialog.getOpenFileNames(
            self, "选择图片文件", "",
            "图片文件 (*.jpg *.jpeg *.png *.bmp *.tiff *.tif)"
        )
        
        if files:
            try:
                imported_files = self.file_manager.import_multiple_files(files)
                self.update_file_list()
                self.statusBar().showMessage(f"成功导入 {len(imported_files)} 个文件")
            except Exception as e:
                QMessageBox.warning(self, "导入失败", str(e))
    
    def add_folder(self):
        """添加文件夹"""
        folder = QFileDialog.getExistingDirectory(self, "选择文件夹")
        
        if folder:
            try:
                imported_files = self.file_manager.import_folder(folder)
                self.update_file_list()
                self.statusBar().showMessage(f"成功导入 {len(imported_files)} 个文件")
            except Exception as e:
                QMessageBox.warning(self, "导入失败", str(e))
    
    def clear_files(self):
        """清空文件列表"""
        self.file_manager.clear_files()
        self.update_file_list()
        self.preview_panel.clear_preview()
    
    def update_file_list(self):
        """更新文件列表显示"""
        self.file_list.clear()
        
        for file_info in self.file_manager.current_files:
            item = QListWidgetItem(file_info['name'])
            item.setData(Qt.UserRole, file_info['path'])
            self.file_list.addItem(item)
        
        self.file_count_label.setText(f"已选择 {self.file_manager.get_file_count()} 个文件")
    
    def on_file_selected(self, item):
        """文件选择事件"""
        file_path = item.data(Qt.UserRole)
        self.preview_panel.load_image(file_path)
        self.update_preview()
    
    def on_settings_changed(self, settings):
        """设置改变事件"""
        self.current_watermark_settings.update(settings)
        self.update_preview()
    
    def update_preview(self):
        """更新预览"""
        current_file = self.preview_panel.get_current_file()
        if current_file:
            try:
                if self.current_watermark_settings['type'] == 'text':
                    text_settings = self.current_watermark_settings['text']
                    result_image = self.watermark_processor.add_text_watermark(
                        current_file,
                        text_settings['content'],
                        self.current_watermark_settings['position'],
                        text_settings['font_size'],
                        text_settings['color'],
                        text_settings['opacity'],
                        text_settings['rotation'],
                        text_settings.get('font_family')
                    )
                else:
                    image_settings = self.current_watermark_settings['image']
                    if image_settings['path'] and os.path.exists(image_settings['path']):
                        result_image = self.watermark_processor.add_image_watermark(
                            current_file,
                            image_settings['path'],
                            self.current_watermark_settings['position'],
                            image_settings['scale'],
                            image_settings['opacity'],
                            image_settings['rotation']
                        )
                    else:
                        # 如果没有图片水印，显示原图
                        from PIL import Image
                        with Image.open(current_file) as img:
                            result_image = img.copy()
                
                self.preview_panel.update_preview(result_image)
                
            except Exception as e:
                QMessageBox.warning(self, "预览更新失败", str(e))
    
    def export_images(self):
        """导出图片"""
        if not self.file_manager.current_files:
            QMessageBox.warning(self, "导出失败", "没有可导出的文件")
            return
        
        # 选择输出文件夹
        output_dir = QFileDialog.getExistingDirectory(self, "选择输出文件夹")
        if not output_dir:
            return
        
        # 检查是否与原文件夹相同
        if any(os.path.dirname(f['path']) == output_dir for f in self.file_manager.current_files):
            reply = QMessageBox.warning(self, "警告", 
                                      "输出文件夹与源文件夹相同，可能覆盖原文件。是否继续？",
                                      QMessageBox.Yes | QMessageBox.No)
            if reply == QMessageBox.No:
                return
        
        # 获取导出设置
        export_settings = self.settings_panel.get_export_settings()
        
        # 创建进度对话框
        progress = QProgressDialog("正在导出图片...", "取消", 0, len(self.file_manager.current_files), self)
        progress.setWindowTitle("导出进度")
        progress.setWindowModality(Qt.WindowModal)
        
        success_count = 0
        for i, file_info in enumerate(self.file_manager.current_files):
            progress.setValue(i)
            
            if progress.wasCanceled():
                break
            
            try:
                # 处理图片
                if self.current_watermark_settings['type'] == 'text':
                    text_settings = self.current_watermark_settings['text']
                    result_image = self.watermark_processor.add_text_watermark(
                        file_info['path'],
                        text_settings['content'],
                        self.current_watermark_settings['position'],
                        text_settings['font_size'],
                        text_settings['color'],
                        text_settings['opacity'],
                        text_settings['rotation'],
                        text_settings.get('font_family')
                    )
                else:
                    image_settings = self.current_watermark_settings['image']
                    if image_settings['path'] and os.path.exists(image_settings['path']):
                        result_image = self.watermark_processor.add_image_watermark(
                            file_info['path'],
                            image_settings['path'],
                            self.current_watermark_settings['position'],
                            image_settings['scale'],
                            image_settings['opacity'],
                            image_settings['rotation']
                        )
                    else:
                        from PIL import Image
                        with Image.open(file_info['path']) as img:
                            result_image = img.copy()
                
                # 调整尺寸
                if export_settings['resize_enabled']:
                    result_image = self.watermark_processor.resize_image(
                        result_image,
                        width=export_settings.get('width'),
                        height=export_settings.get('height'),
                        percent=export_settings.get('percent')
                    )
                
                # 生成输出文件名
                original_name = Path(file_info['path']).stem
                extension = export_settings['format'].lower()
                
                if export_settings['naming'] == 'prefix':
                    output_name = f"{export_settings['prefix']}{original_name}.{extension}"
                elif export_settings['naming'] == 'suffix':
                    output_name = f"{original_name}{export_settings['suffix']}.{extension}"
                else:
                    output_name = f"{original_name}.{extension}"
                
                output_path = os.path.join(output_dir, output_name)
                
                # 保存图片
                self.watermark_processor.save_image(
                    result_image,
                    output_path,
                    export_settings['format'],
                    export_settings['quality']
                )
                
                success_count += 1
                
            except Exception as e:
                print(f"导出失败 {file_info['path']}: {e}")
                continue
        
        progress.setValue(len(self.file_manager.current_files))
        
        QMessageBox.information(self, "导出完成", 
                              f"成功导出 {success_count}/{len(self.file_manager.current_files)} 个文件")
    
    def save_template(self):
        """保存模板"""
        from PyQt5.QtWidgets import QInputDialog
        
        name, ok = QInputDialog.getText(self, "保存模板", "请输入模板名称:")
        if ok and name:
            success = self.config_manager.save_template(name, self.current_watermark_settings)
            if success:
                QMessageBox.information(self, "成功", "模板保存成功")
                self.settings_panel.update_template_list()
            else:
                QMessageBox.warning(self, "失败", "模板保存失败")
    
    def load_template(self):
        """加载模板"""
        templates = self.config_manager.list_templates()
        if not templates:
            QMessageBox.information(self, "提示", "没有可用的模板")
            return
        
        from PyQt5.QtWidgets import QInputDialog
        template, ok = QInputDialog.getItem(self, "加载模板", "选择模板:", templates, 0, False)
        
        if ok and template:
            template_data = self.config_manager.load_template(template)
            if template_data:
                self.current_watermark_settings.update(template_data['settings'])
                self.settings_panel.load_settings(template_data['settings'])
                self.update_preview()
                QMessageBox.information(self, "成功", "模板加载成功")
            else:
                QMessageBox.warning(self, "失败", "模板加载失败")
    
    def load_last_settings(self):
        """加载上次的设置"""
        if self.config_manager.config.get('auto_load_last', True):
            last_template = self.config_manager.config.get('last_used_template')
            if last_template:
                template_data = self.config_manager.load_template(last_template)
                if template_data:
                    self.current_watermark_settings.update(template_data['settings'])
                    self.settings_panel.load_settings(template_data['settings'])
    
    def closeEvent(self, event):
        """关闭事件"""
        # 保存当前设置
        self.config_manager.update_config('last_used_template', 'last_session')
        self.config_manager.save_template('last_session', self.current_watermark_settings)
        event.accept()