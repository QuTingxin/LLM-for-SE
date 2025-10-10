import sys
import os
from pathlib import Path
from PyQt5.QtWidgets import QApplication
from main_window import MainWindow

def main():
    app = QApplication(sys.argv)
    app.setApplicationName("水印工具")
    app.setApplicationVersion("1.0.0")
    
    # 创建应用数据目录
    app_data_dir = os.path.join(os.path.expanduser('~'), '.watermarkapp')
    if not os.path.exists(app_data_dir):
        os.makedirs(app_data_dir)
    
    window = MainWindow(app_data_dir)
    window.show()
    
    sys.exit(app.exec_())

if __name__ == '__main__':
    main()