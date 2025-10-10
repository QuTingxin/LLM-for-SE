import os
from PIL import Image, ImageDraw, ImageFont, ImageOps
import numpy as np
from typing import Tuple, Optional, Dict, Any

class WatermarkProcessor:
    def __init__(self):
        self.supported_formats = ['.jpg', '.jpeg', '.png', '.bmp', '.tiff', '.tif']
        
    def add_text_watermark(self, image_path: str, text: str, position: Tuple[int, int], 
                          font_size: int = 36, color: Tuple[int, int, int] = (255, 255, 255),
                          opacity: int = 100, rotation: int = 0, 
                          font_path: Optional[str] = None) -> Image.Image:
        """添加文本水印"""
        try:
            # 打开原图
            with Image.open(image_path) as img:
                img = img.convert('RGBA')
                
                # 创建透明层用于水印
                watermark = Image.new('RGBA', img.size, (0, 0, 0, 0))
                draw = ImageDraw.Draw(watermark)
                
                # 加载字体
                try:
                    if font_path and os.path.exists(font_path):
                        font = ImageFont.truetype(font_path, font_size)
                    else:
                        font = ImageFont.load_default()
                except:
                    font = ImageFont.load_default()
                
                # 计算文本尺寸
                bbox = draw.textbbox((0, 0), text, font=font)
                text_width = bbox[2] - bbox[0]
                text_height = bbox[3] - bbox[1]
                
                # 调整位置（确保在图像范围内）
                x, y = position
                x = max(0, min(x, img.width - text_width))
                y = max(0, min(y, img.height - text_height))
                
                # 绘制文本
                rgba_color = color + (int(255 * opacity / 100),)
                draw.text((x, y), text, font=font, fill=rgba_color)
                
                # 旋转水印
                if rotation != 0:
                    watermark = watermark.rotate(rotation, expand=False, resample=Image.BICUBIC)
                
                # 合并图像
                result = Image.alpha_composite(img, watermark)
                return result.convert('RGB')
                
        except Exception as e:
            raise Exception(f"添加文本水印失败: {str(e)}")
    
    def add_image_watermark(self, image_path: str, watermark_path: str, 
                           position: Tuple[int, int], scale: float = 1.0, 
                           opacity: int = 100, rotation: int = 0) -> Image.Image:
        """添加图片水印"""
        try:
            # 打开原图
            with Image.open(image_path) as img:
                img = img.convert('RGBA')
                
                # 打开水印图片
                with Image.open(watermark_path) as watermark_img:
                    watermark_img = watermark_img.convert('RGBA')
                    
                    # 缩放水印
                    if scale != 1.0:
                        new_width = int(watermark_img.width * scale)
                        new_height = int(watermark_img.height * scale)
                        watermark_img = watermark_img.resize((new_width, new_height), Image.LANCZOS)
                    
                    # 调整透明度
                    if opacity < 100:
                        alpha = watermark_img.split()[3]
                        alpha = alpha.point(lambda p: p * opacity / 100)
                        watermark_img.putalpha(alpha)
                    
                    # 旋转水印
                    if rotation != 0:
                        watermark_img = watermark_img.rotate(rotation, expand=True, resample=Image.BICUBIC)
                    
                    # 创建透明层用于放置水印
                    watermark_layer = Image.new('RGBA', img.size, (0, 0, 0, 0))
                    
                    # 计算位置（确保水印在图像范围内）
                    x, y = position
                    x = max(0, min(x, img.width - watermark_img.width))
                    y = max(0, min(y, img.height - watermark_img.height))
                    
                    # 粘贴水印
                    watermark_layer.paste(watermark_img, (x, y), watermark_img)
                    
                    # 合并图像
                    result = Image.alpha_composite(img, watermark_layer)
                    return result.convert('RGB')
                    
        except Exception as e:
            raise Exception(f"添加图片水印失败: {str(e)}")
    
    def resize_image(self, image: Image.Image, width: Optional[int] = None, 
                    height: Optional[int] = None, percent: Optional[float] = None) -> Image.Image:
        """调整图像尺寸"""
        if percent is not None:
            new_width = int(image.width * percent / 100)
            new_height = int(image.height * percent / 100)
            return image.resize((new_width, new_height), Image.LANCZOS)
        elif width is not None and height is not None:
            return image.resize((width, height), Image.LANCZOS)
        elif width is not None:
            ratio = width / image.width
            new_height = int(image.height * ratio)
            return image.resize((width, new_height), Image.LANCZOS)
        elif height is not None:
            ratio = height / image.height
            new_width = int(image.width * ratio)
            return image.resize((new_width, height), Image.LANCZOS)
        else:
            return image
    
    def save_image(self, image: Image.Image, output_path: str, format: str = 'JPEG', 
                  quality: int = 95) -> None:
        """保存图像"""
        if format.upper() == 'JPEG':
            image = image.convert('RGB')
            image.save(output_path, format='JPEG', quality=quality, optimize=True)
        else:
            image.save(output_path, format='PNG', optimize=True)