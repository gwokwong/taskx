#!/bin/bash

echo "🚀 启动 DooTask 项目..."

# 检查是否安装了必要的依赖
command -v java >/dev/null 2>&1 || { echo "❌ 需要安装 Java 17 或更高版本"; exit 1; }
command -v node >/dev/null 2>&1 || { echo "❌ 需要安装 Node.js 18 或更高版本"; exit 1; }
command -v npm >/dev/null 2>&1 || { echo "❌ 需要安装 npm"; exit 1; }

# 启动后端服务
echo "📦 启动 Spring Boot 后端服务..."
cd java-backend

if [ ! -f "target/dootask-backend-1.0.0.jar" ]; then
    echo "🔨 编译后端项目..."
    ./mvnw clean package -DskipTests
fi

echo "🎯 启动后端服务 (端口 8080)..."
java -jar target/dootask-backend-1.0.0.jar &
BACKEND_PID=$!

# 等待后端启动
echo "⏳ 等待后端服务启动..."
sleep 10

# 启动前端服务
echo "🎨 启动 Vue3 前端服务..."
cd ../vue-frontend

if [ ! -d "node_modules" ]; then
    echo "📥 安装前端依赖..."
    npm install
fi

echo "🌐 启动前端服务 (端口 3000)..."
npm run dev &
FRONTEND_PID=$!

echo "✅ 服务启动完成!"
echo "🌐 前端地址: http://localhost:3000"
echo "🔧 后端API: http://localhost:8080/api"
echo "📚 API文档: http://localhost:8080/doc.html"
echo ""
echo "按 Ctrl+C 停止所有服务"

# 创建清理函数
cleanup() {
    echo ""
    echo "🛑 正在停止服务..."
    kill $BACKEND_PID 2>/dev/null
    kill $FRONTEND_PID 2>/dev/null
    echo "✅ 所有服务已停止"
    exit 0
}

# 捕获退出信号
trap cleanup SIGINT SIGTERM

# 等待进程
wait