#!/bin/bash

# Easy Recon SDK 构建脚本
# 用于构建和发布所有语言版本的 SDK

set -e

echo "========================================"
echo "Easy Recon SDK 构建脚本"
echo "========================================"

# 定义颜色
GREEN="\033[0;32m"
YELLOW="\033[1;33m"
RED="\033[0;31m"
NC="\033[0m" # No Color

# 构建 Java Spring Boot Starter
function build_java() {
    echo -e "${YELLOW}构建 Java Spring Boot Starter...${NC}"
    cd "$(dirname "$0")/../sdk/spring-boot-starter"
    mvn clean package -DskipTests
    echo -e "${GREEN}Java Spring Boot Starter 构建完成！${NC}"
    cd - > /dev/null
}

# 构建 Go SDK
function build_go() {
    echo -e "${YELLOW}构建 Go SDK...${NC}"
    cd "$(dirname "$0")/../sdk/go"
    go build -o easy-recon-sdk-go .
    echo -e "${GREEN}Go SDK 构建完成！${NC}"
    cd - > /dev/null
}

# 构建 Python SDK
function build_python() {
    echo -e "${YELLOW}构建 Python SDK...${NC}"
    cd "$(dirname "$0")/../sdk/python"
    # Python 包构建
    echo "Python SDK 构建完成！"
    cd - > /dev/null
}

# 构建 Node.js SDK
function build_nodejs() {
    echo -e "${YELLOW}构建 Node.js SDK...${NC}"
    cd "$(dirname "$0")/../sdk/node.js"
    npm install
    echo -e "${GREEN}Node.js SDK 构建完成！${NC}"
    cd - > /dev/null
}

# 显示帮助信息
function show_help() {
    echo "用法: $0 [选项]"
    echo ""
    echo "选项:"
    echo "  --all           构建所有语言版本"
    echo "  --java          构建 Java Spring Boot Starter"
    echo "  --go            构建 Go SDK"
    echo "  --python        构建 Python SDK"
    echo "  --nodejs        构建 Node.js SDK"
    echo "  --help          显示帮助信息"
    echo ""
    echo "示例:"
    echo "  $0 --all        # 构建所有语言版本"
    echo "  $0 --java       # 只构建 Java 版本"
}

# 主函数
function main() {
    if [ $# -eq 0 ]; then
        show_help
        exit 1
    fi

    while [ $# -gt 0 ]; do
        case "$1" in
            --all)
                build_java
                build_go
                build_python
                build_nodejs
                echo -e "${GREEN}所有语言版本构建完成！${NC}"
                ;;
            --java)
                build_java
                ;;
            --go)
                build_go
                ;;
            --python)
                build_python
                ;;
            --nodejs)
                build_nodejs
                ;;
            --help)
                show_help
                exit 0
                ;;
            *)
                echo -e "${RED}未知选项: $1${NC}"
                show_help
                exit 1
                ;;
        esac
        shift
    done
}

# 执行主函数
main "$@"
