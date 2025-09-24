

import os
import sys
from datetime import datetime
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont, ExifTags
import argparse

class WatermarkConfig:
    def __init__(self):
        self.font_size = 236
        self.font_color = (255, 255, 255)  # 白色
        self.position = "bottom-right"  # 默认位置
        self.margin = 20  # 边距
        self.opacity = 200  # 透明度 (0-255)

class ImageWatermarkTool:
    def __init__(self):
        self.config = WatermarkConfig()
        
    def get_exif_date(self, image_path):
        """从图片EXIF信息中提取拍摄日期"""
        try:
            with Image.open(image_path) as img:
                # print("  - 打开图片...", img)
                exif = img._getexif()
                # print("  - 读取EXIF信22222息...")
                # print(exif)
                if exif is not None:
                    for tag, value in exif.items():
                        tag_name = ExifTags.TAGS.get(tag, tag)
                        # print(f"    - {tag_name}: {value}")
                        # 查找拍摄时间相关的标签
                        if tag_name in ['DateTime', 'DateTimeOriginal', 'DateTimeDigitized']:
                            if value:
                                # 解析日期时间字符串
                                try:
                                    dt = datetime.strptime(value, '%Y:%m:%d %H:%M:%S')
                                    return dt.strftime('%Y.%m.%d')
                                except:
                                    continue
                return None
        except Exception as e:
            print(f"读取EXIF信息失败 {image_path}: {e}")
            return None
    
    def create_watermark_image(self, text, image_size):
        """创建水印文字图像"""
        # 创建透明背景的文字图像
        txt_img = Image.new('RGBA', image_size, (255, 255, 255, 0))
        draw = ImageDraw.Draw(txt_img)
        
        # 尝试使用系统字体，如果没有则使用默认字体
        try:
            # 尝试使用常见的系统字体
            font_paths = [
                "/System/Library/Fonts/Arial.ttf",  # macOS
                "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf",  # Linux
                "C:/Windows/Fonts/arial.ttf",  # Windows
            ]
            
            font = None
            for font_path in font_paths:
                if os.path.exists(font_path):
                    print(f"  - 使用字体: {font_path}")
                    font = ImageFont.truetype(font_path, self.config.font_size)
                    break
            
            if font is None:
                font = ImageFont.load_default()
                
        except Exception:
            font = ImageFont.load_default()
        
        # 获取文字尺寸
        bbox = draw.textbbox((0, 0), text, font=font)
        text_width = bbox[2] - bbox[0]
        text_height = bbox[3] - bbox[1]
        
        # 计算文字位置
        img_width, img_height = image_size
        margin = self.config.margin
        
        if self.config.position == "top-left":
            x, y = margin, margin
        elif self.config.position == "top-right":
            x, y = img_width - text_width - margin, margin
        elif self.config.position == "bottom-left":
            x, y = margin, img_height - text_height - margin
        elif self.config.position == "bottom-right":
            x, y = img_width - text_width - margin, img_height - text_height - margin
        elif self.config.position == "center":
            x, y = (img_width - text_width) // 2, (img_height - text_height) // 2
        else:
            x, y = img_width - text_width - margin, img_height - text_height - margin
        
        # 绘制文字（带阴影效果）
        shadow_offset = 2
        shadow_color = (0, 0, 0, self.config.opacity)
        
        # 绘制阴影
        draw.text((x + shadow_offset, y + shadow_offset), text, 
                 font=font, fill=shadow_color)
        
        # 绘制主文字
        main_color = self.config.font_color + (self.config.opacity,)
        draw.text((x, y), text, font=font, fill=main_color)
        
        return txt_img
    
    def add_watermark_to_image(self, image_path, output_path, date_text):
        """给单张图片添加水印"""
        try:
            with Image.open(image_path) as img:
                # 转换为RGBA模式以支持透明度
                if img.mode != 'RGBA':
                    print("  - 转换为RGBA模式...")
                    img = img.convert('RGBA')
                
                # 创建水印
                watermark = self.create_watermark_image(date_text, img.size)
                # watermark.save("debug_watermark.png")

                
                # 合并图片和水印
                result = Image.alpha_composite(img, watermark)
                
                # 转换回RGB模式并保存
                if result.mode != 'RGB':
                    result = result.convert('RGB')
                
                result.save(output_path, 'JPEG', quality=95)
                return True
                
        except Exception as e:
            print(f"处理图片失败 {image_path}: {e}")
            return False
    
    def process_directory(self, directory_path):
        """处理目录中的所有图片"""
        directory = Path(directory_path)
        
        if not directory.exists():
            print(f"目录不存在: {directory_path}")
            return
        
        # 创建输出目录
        output_dir = directory / f"_watermarkdImg"
        output_dir.mkdir(exist_ok=True)
        
        # 支持的图片格式
        supported_formats = ('.jpg', '.jpeg', '.png', '.bmp', '.tiff', '.tif')
        
        # 获取所有图片文件
        image_files = [f for f in directory.iterdir() 
                      if f.suffix.lower() in supported_formats]
        
        if not image_files:
            print("未找到图片文件")
            return
        
        print(f"找到 {len(image_files)} 张图片")
        
        processed = 0
        failed = 0
        
        for image_file in image_files:
            print(f"处理: {image_file.name}")
            
            # 获取EXIF日期
            date_text = self.get_exif_date(image_file)
            if not date_text:
                print(f"  - 未找到拍摄日期，跳过")
                failed += 1
                continue
            
            # 生成输出文件名
            output_file = output_dir / f"{image_file.stem}_watermarked.jpg"
            print(f"  - 拍摄日期: {date_text}")
            
            # 添加水印
            if self.add_watermark_to_image(image_file, output_file, date_text):
                print(f"  - 完成: {output_file.name}")
                processed += 1
            else:
                print(f"  - 失败")
                failed += 1
        
        print(f"\n处理完成! 成功: {processed}, 失败: {failed}")
        print(f"输出目录: {output_dir}")

def interactive_setup():
    """交互式设置水印参数"""
    config = WatermarkConfig()
    
    print("=== 图片水印工具设置 ===")
    print("请设置水印参数 (直接回车使用默认值):\n")
    
    # 字体大小
    try:
        size = input(f"字体大小 (默认 {config.font_size}): ").strip()
        if size:
            config.font_size = int(size)
    except:
        print("使用默认字体大小")
    
    # 字体颜色
    color_input = input("字体颜色 (格式: R,G,B 默认 255,255,255 白色): ").strip()
    if color_input:
        try:
            r, g, b = map(int, color_input.split(','))
            config.font_color = (r, g, b)
        except:
            print("颜色格式错误，使用默认白色")
    
    # 位置
    print("\n可选位置: top-left, top-right, bottom-left, bottom-right, center")
    position_input = input(f"水印位置 (默认 {config.position}): ").strip()
    if position_input:
        if position_input in ["top-left", "top-right", "bottom-left", 
                             "bottom-right", "center"]:
            config.position = position_input
        else:
            print("位置无效，使用默认右下角")
    
    # 边距
    try:
        margin = input(f"边距 (默认 {config.margin}): ").strip()
        if margin:
            config.margin = int(margin)
    except:
        print("使用默认边距")
    
    # 透明度
    try:
        opacity = input(f"透明度 (0-255, 默认 {config.opacity}): ").strip()
        if opacity:
            config.opacity = int(opacity)
    except:
        print("使用默认透明度")
    
    return config

def main():
    # print("欢迎使用图片水印添加工具")

    parser = argparse.ArgumentParser(description='图片水印添加工具')
    parser.add_argument('path', nargs='?', help='图片目录路径')
    parser.add_argument('--interactive', '-i', action='store_true', 
                       help='交互式设置参数')

    # print("欢迎使用图片水印添加工具")
    
    args = parser.parse_args()
    
    tool = ImageWatermarkTool()
    
    # 交互式设置
    if args.interactive:
        tool.config = interactive_setup()
        
    
    # 获取目录路径
    if args.path:
        directory_path = args.path
    else:
        directory_path = r"E:\code\LLM-for-SE\_watermark"
    
    if not directory_path:
        print("未提供目录路径")
        return
    
    # 处理目录
    tool.process_directory(directory_path)

if __name__ == "__main__":
    main()