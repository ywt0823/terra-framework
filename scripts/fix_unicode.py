#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import os
import re
import sys

def fix_unicode_in_file(file_path):
    """修复文件中的Unicode转义序列"""
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # 使用正则表达式查找所有Unicode转义序列并解码它们
    def replace_unicode(match):
        try:
            # 提取前面的4位十六进制数
            code = match.group(1)
            # 将十六进制数转换为对应的Unicode字符
            char = chr(int(code, 16))
            return char
        except Exception as e:
            print(f"错误处理Unicode: {match.group(0)}, 错误: {e}")
            return match.group(0)
    
    # 匹配类似于 u4e2d u6587 这样的Unicode转义序列
    pattern = r'u([0-9a-fA-F]{4})'
    
    # 替换所有匹配的Unicode转义序列
    modified_content = re.sub(pattern, replace_unicode, content)
    
    # 如果内容有变化，则写回文件
    if modified_content != content:
        with open(file_path, 'w', encoding='utf-8') as f:
            f.write(modified_content)
        print(f"已修复: {file_path}")
    else:
        print(f"无需要修复: {file_path}")

def main():
    """主函数"""
    if len(sys.argv) < 2:
        print("使用方法: python fix_unicode.py <文件路径>")
        sys.exit(1)
    
    file_path = sys.argv[1]
    if not os.path.exists(file_path):
        print(f"错误: 文件 {file_path} 不存在")
        sys.exit(1)
    
    fix_unicode_in_file(file_path)

if __name__ == "__main__":
    main() 