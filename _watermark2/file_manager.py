import os
import shutil
from pathlib import Path
from typing import List, Dict, Any
from PIL import Image

class FileManager:
    def __init__(self):
        self.supported_formats = ['.jpg', '.jpeg', '.png', '.bmp', '.tiff', '.tif']
        self.current_files = []
    
    def validate_image_file(self, file_path: str) -> bool:
        """验证图像文件"""
        try:
            with Image.open(file_path) as img:
                return True
        except:
            return False
    
    def import_single_file(self, file_path: str) -> Dict[str, Any]:
        """导入单个文件"""
        if not os.path.exists(file_path):
            raise FileNotFoundError(f"文件不存在: {file_path}")
        
        if Path(file_path).suffix.lower() not in self.supported_formats:
            raise ValueError(f"不支持的格式: {Path(file_path).suffix}")
        
        if not self.validate_image_file(file_path):
            raise ValueError("无效的图像文件")
        
        file_info = {
            'path': file_path,
            'name': Path(file_path).name,
            'size': os.path.getsize(file_path)
        }
        
        if file_info not in self.current_files:
            self.current_files.append(file_info)
        
        return file_info
    
    def import_multiple_files(self, file_paths: List[str]) -> List[Dict[str, Any]]:
        """导入多个文件"""
        imported_files = []
        for file_path in file_paths:
            try:
                file_info = self.import_single_file(file_path)
                imported_files.append(file_info)
            except Exception as e:
                print(f"导入文件失败 {file_path}: {e}")
                continue
        
        return imported_files
    
    def import_folder(self, folder_path: str) -> List[Dict[str, Any]]:
        """导入整个文件夹"""
        if not os.path.exists(folder_path):
            raise FileNotFoundError(f"文件夹不存在: {folder_path}")
        
        image_files = []
        for root, dirs, files in os.walk(folder_path):
            for file in files:
                if Path(file).suffix.lower() in self.supported_formats:
                    image_files.append(os.path.join(root, file))
        
        return self.import_multiple_files(image_files)
    
    def generate_thumbnail(self, file_path: str, size: tuple = (100, 100)) -> Image.Image:
        """生成缩略图"""
        try:
            with Image.open(file_path) as img:
                img.thumbnail(size, Image.LANCZOS)
                return img.copy()
        except:
            # 返回默认缩略图
            return Image.new('RGB', size, color='gray')
    
    def clear_files(self):
        """清空文件列表"""
        self.current_files.clear()
    
    def remove_file(self, file_path: str):
        """移除单个文件"""
        self.current_files = [f for f in self.current_files if f['path'] != file_path]
    
    def get_file_count(self) -> int:
        """获取文件数量"""
        return len(self.current_files)