#!/usr/bin/env python3
"""
生成自动打卡应用的图标
"""

from PIL import Image, ImageDraw
import os

def create_clock_icon(size):
    """创建一个时钟图标"""
    # 创建图像
    img = Image.new('RGBA', (size, size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    
    # 颜色定义
    primary_color = (98, 0, 238, 255)  # 紫色
    white_color = (255, 255, 255, 255)
    
    # 计算中心点和半径
    center = size // 2
    outer_radius = int(size * 0.4)
    inner_radius = int(size * 0.35)
    
    # 绘制外圆（背景）
    draw.ellipse([center - outer_radius, center - outer_radius, 
                  center + outer_radius, center + outer_radius], 
                 fill=primary_color)
    
    # 绘制内圆（时钟面）
    draw.ellipse([center - inner_radius, center - inner_radius, 
                  center + inner_radius, center + inner_radius], 
                 fill=white_color)
    
    # 绘制时钟中心点
    center_radius = max(2, size // 50)
    draw.ellipse([center - center_radius, center - center_radius, 
                  center + center_radius, center + center_radius], 
                 fill=primary_color)
    
    # 绘制时针（指向9点）
    hour_hand_length = int(size * 0.2)
    hour_hand_width = max(2, size // 50)
    draw.rectangle([center - hour_hand_width//2, center - hour_hand_length, 
                   center + hour_hand_width//2, center], 
                  fill=primary_color)
    
    # 绘制分针（指向12点）
    minute_hand_length = int(size * 0.25)
    minute_hand_width = max(1, size // 80)
    draw.rectangle([center - minute_hand_width//2, center - minute_hand_length, 
                   center + minute_hand_width//2, center], 
                  fill=primary_color)
    
    # 绘制时钟刻度
    tick_length = max(3, size // 30)
    tick_width = max(1, size // 100)
    
    # 12点刻度
    draw.rectangle([center - tick_width//2, center - inner_radius + 5, 
                   center + tick_width//2, center - inner_radius + 5 + tick_length], 
                  fill=primary_color)
    
    # 6点刻度
    draw.rectangle([center - tick_width//2, center + inner_radius - 5 - tick_length, 
                   center + tick_width//2, center + inner_radius - 5], 
                  fill=primary_color)
    
    # 3点刻度
    draw.rectangle([center + inner_radius - 5 - tick_length, center - tick_width//2, 
                   center + inner_radius - 5, center + tick_width//2], 
                  fill=primary_color)
    
    # 9点刻度
    draw.rectangle([center - inner_radius + 5, center - tick_width//2, 
                   center - inner_radius + 5 + tick_length, center + tick_width//2], 
                  fill=primary_color)
    
    return img

def main():
    """主函数"""
    # 定义不同尺寸
    sizes = {
        'mdpi': 48,
        'hdpi': 72,
        'xhdpi': 96,
        'xxhdpi': 144,
        'xxxhdpi': 192
    }
    
    # 创建图标
    for density, size in sizes.items():
        print(f"生成 {density} 图标 ({size}x{size})...")
        
        # 创建图标
        icon = create_clock_icon(size)
        
        # 确保目录存在
        mipmap_dir = f"app/src/main/res/mipmap-{density}"
        os.makedirs(mipmap_dir, exist_ok=True)
        
        # 保存图标
        icon.save(f"{mipmap_dir}/ic_launcher.png", "PNG")
        icon.save(f"{mipmap_dir}/ic_launcher_round.png", "PNG")
        
        print(f"已保存到 {mipmap_dir}/")
    
    print("所有图标生成完成！")

if __name__ == "__main__":
    main() 