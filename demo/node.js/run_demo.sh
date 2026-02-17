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
if [ ! -d "node_modules" ]; then
    npm install
else
    # 简单的检查，如果 package.json 变动可能需要更新
    npm install
fi

# 检查 SDK 依赖是否存在 (这里 demo 引用了相对路径的 SDK，假定 SDK 目录下的 node_modules 也需要安装)
# 实际项目中通常是 npm link 或 npm install ../../sdk/node.js
# 这里为了简便，手动确保 SDK 依赖
if [ -d "../../sdk/node.js" ]; then
    echo ">>> Checking SDK dependencies..."
    pushd ../../sdk/node.js > /dev/null
    if [ ! -d "node_modules" ]; then
        npm install
    fi
    popd > /dev/null
fi

# 运行 Demo
echo ">>> Running Demo..."
node index.js

echo "=== Done ==="
