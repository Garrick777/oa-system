#!/bin/bash

# OA办公系统启动脚本

echo "=========================================="
echo "    OA办公系统启动脚本"
echo "=========================================="

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$PROJECT_DIR"

# 检查并启动后端
start_backend() {
    echo -e "${YELLOW}[1/2] 启动后端服务...${NC}"
    
    # 检查端口
    if lsof -i:9090 > /dev/null 2>&1; then
        echo -e "${GREEN}后端已在运行中 (端口9090)${NC}"
    else
        cd "$PROJECT_DIR/backend"
        nohup ./mvnw spring-boot:run -q > ../logs/backend.log 2>&1 &
        echo $! > ../.backend.pid
        echo -e "${GREEN}后端启动中...${NC}"
        sleep 5
    fi
}

# 检查并启动前端
start_frontend() {
    echo -e "${YELLOW}[2/2] 启动前端服务...${NC}"
    
    # 检查端口
    if lsof -i:3000 > /dev/null 2>&1; then
        echo -e "${GREEN}前端已在运行中 (端口3000)${NC}"
    else
        cd "$PROJECT_DIR/frontend"
        nohup npm run dev > ../logs/frontend.log 2>&1 &
        echo $! > ../.frontend.pid
        echo -e "${GREEN}前端启动中...${NC}"
        sleep 3
    fi
}

# 创建日志目录
mkdir -p "$PROJECT_DIR/logs"

# 启动服务
start_backend
start_frontend

echo ""
echo "=========================================="
echo -e "${GREEN}启动完成!${NC}"
echo "=========================================="
echo ""
echo "访问地址:"
echo "  - 前端: http://localhost:3000"
echo "  - 后端: http://localhost:9090/api"
echo "  - API文档: http://localhost:9090/api/doc.html"
echo ""
echo "默认账号: admin / admin123"
echo ""
echo "日志文件:"
echo "  - 后端: logs/backend.log"
echo "  - 前端: logs/frontend.log"
echo ""
