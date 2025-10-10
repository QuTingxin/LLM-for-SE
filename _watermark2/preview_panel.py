from PyQt5.QtWidgets import (QWidget, QVBoxLayout, QHBoxLayout, QLabel, 
                             QScrollArea, QSizePolicy)
from PyQt5.QtCore import Qt
from PyQt5.QtGui import QPixmap, QImage
from PIL import Image
from PIL.ImageQt import ImageQt

class PreviewPanel(QWidget):
    def __init__(self):
        super().__init__()
        self.current_file = None
        self.init_ui()
    
    def init_ui(self):
        layout = QVBoxLayout(self)
        
        # 标题
        self.title_label = QLabel("预览")
        self.title_label.setAlignment(Qt.AlignCenter)
        self.title_label.setStyleSheet("font-size: 16px; font-weight: bold; margin: 10px;")
        
        # 图片显示区域
        self.image_label = QLabel()
        self.image_label.setAlignment(Qt.AlignCenter)
        self.image_label.setMinimumSize(400, 300)
        self.image_label.setStyleSheet("border: 1px solid #ccc; background-color: #f0f0f0;")
        self.image_label.setText("请选择图片进行预览")
        
        # 滚动区域
        scroll_area = QScrollArea()
        scroll_area.setWidget(self.image_label)
        scroll_area.setWidgetResizable(True)
        scroll_area.setAlignment(Qt.AlignCenter)
        
        layout.addWidget(self.title_label)
        layout.addWidget(scroll_area)
    
    def load_image(self, file_path: str):
        """加载图片"""
        self.current_file = file_path
        self.title_label.setText(f"预览 - {file_path.split('/')[-1]}")
        
        # 显示原图
        try:
            with Image.open(file_path) as img:
                # 调整尺寸以适应预览
                preview_size = (800, 600)
                img.thumbnail(preview_size, Image.LANCZOS)
                
                # 转换为QPixmap显示
                qimage = ImageQt(img.convert('RGB'))
                pixmap = QPixmap.fromImage(qimage)
                self.image_label.setPixmap(pixmap)
                
        except Exception as e:
            self.image_label.setText(f"加载图片失败: {str(e)}")
    
    def update_preview(self, pil_image):
        """更新预览图片"""
        try:
            # 调整尺寸以适应预览
            preview_size = (800, 600)
            pil_image.thumbnail(preview_size, Image.LANCZOS)
            
            # 转换为QPixmap显示
            qimage = ImageQt(pil_image)
            pixmap = QPixmap.fromImage(qimage)
            self.image_label.setPixmap(pixmap)
            
        except Exception as e:
            self.image_label.setText(f"更新预览失败: {str(e)}")
    
    def clear_preview(self):
        """清空预览"""
        self.current_file = None
        self.title_label.setText("预览")
        self.image_label.setText("请选择图片进行预览")
        self.image_label.setPixmap(QPixmap())
    
    def get_current_file(self):
        """获取当前文件路径"""
        return self.current_file