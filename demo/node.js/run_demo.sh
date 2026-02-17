#!/bin/bash

# 确保在脚本所在目录
cd "$(dirname "$0")"

echo "=== Easy Recon Node.js Demo Runner ==="

# 检查 node 是否安装
if ! command -v node &> /dev/null; then
    echo "Error: Node.js is not installed."
    exit 1
fi

# 安装依赖
echo ">>> Installing dependencies..."
# 注意：如果是私有包，请确保配置了 .npmrc
npm install

# 运行 Demo
echo ">>> Running Demo..."
node index.js

echo "=== Done ==="
