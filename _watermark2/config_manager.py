import json
import os
from typing import Dict, Any, List
from datetime import datetime

class ConfigManager:
    def __init__(self, app_data_dir: str):
        self.app_data_dir = app_data_dir
        self.templates_dir = os.path.join(app_data_dir, 'templates')
        self.config_file = os.path.join(app_data_dir, 'config.json')
        
        # 创建必要的目录
        os.makedirs(self.templates_dir, exist_ok=True)
        
        # 加载配置
        self.config = self.load_config()
    
    def load_config(self) -> Dict[str, Any]:
        """加载配置文件"""
        default_config = {
            'last_used_template': None,
            'output_folder': None,
            'default_quality': 95,
            'auto_load_last': True
        }
        
        if os.path.exists(self.config_file):
            try:
                with open(self.config_file, 'r', encoding='utf-8') as f:
                    user_config = json.load(f)
                    default_config.update(user_config)
            except:
                pass
        
        return default_config
    
    def save_config(self):
        """保存配置文件"""
        try:
            with open(self.config_file, 'w', encoding='utf-8') as f:
                json.dump(self.config, f, ensure_ascii=False, indent=2)
        except Exception as e:
            print(f"保存配置失败: {e}")
    
    def save_template(self, template_name: str, settings: Dict[str, Any]) -> bool:
        """保存水印模板"""
        try:
            template_data = {
                'name': template_name,
                'settings': settings,
                'created_at': datetime.now().isoformat(),
                'updated_at': datetime.now().isoformat()
            }
            
            template_file = os.path.join(self.templates_dir, f"{template_name}.json")
            with open(template_file, 'w', encoding='utf-8') as f:
                json.dump(template_data, f, ensure_ascii=False, indent=2)
            
            return True
        except Exception as e:
            print(f"保存模板失败: {e}")
            return False
    
    def load_template(self, template_name: str) -> Dict[str, Any]:
        """加载水印模板"""
        try:
            template_file = os.path.join(self.templates_dir, f"{template_name}.json")
            if os.path.exists(template_file):
                with open(template_file, 'r', encoding='utf-8') as f:
                    return json.load(f)
            return None
        except:
            return None
    
    def delete_template(self, template_name: str) -> bool:
        """删除水印模板"""
        try:
            template_file = os.path.join(self.templates_dir, f"{template_name}.json")
            if os.path.exists(template_file):
                os.remove(template_file)
                return True
            return False
        except:
            return False
    
    def list_templates(self) -> List[str]:
        """列出所有模板"""
        templates = []
        try:
            for file in os.listdir(self.templates_dir):
                if file.endswith('.json'):
                    templates.append(file[:-5])  # 移除.json扩展名
        except:
            pass
        return templates
    
    def update_config(self, key: str, value: Any):
        """更新配置"""
        self.config[key] = value
        self.save_config()