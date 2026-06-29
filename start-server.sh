#!/bin/bash
# Todo App 服务端一键启动脚本
# 用法: ./start-server.sh [port]

set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
JAR_FILE="$SCRIPT_DIR/server/build/libs/server-all.jar"
DATA_DIR="${TODO_DATA_DIR:-$SCRIPT_DIR/.data}"
PORT="${1:-8080}"

# 检查 Java 21
JAVA_BIN="java"
if [ -n "$JAVA_HOME" ]; then
    JAVA_BIN="$JAVA_HOME/bin/java"
fi

JAVA_VERSION=$("$JAVA_BIN" -version 2>&1 | head -1 | grep -oP '\d+' | head -1)
if [ "$JAVA_VERSION" -lt 21 ] 2>/dev/null; then
    # 尝试查找系统上的 JDK 21
    if [ -d "~/Library/Java/JavaVirtualMachines/corretto-21.0.11/Contents/Home" ]; then
        JAVA_BIN="~/Library/Java/JavaVirtualMachines/corretto-21.0.11/Contents/Home/bin/java"
    elif [ -d "$JAVA_HOME_21" ]; then
        JAVA_BIN="$JAVA_HOME_21/bin/java"
    fi
fi

echo "╔══════════════════════════════════════╗"
echo "║     Todo App 服务端 v1.0.0          ║"
echo "╠══════════════════════════════════════╣"
echo "║  端口: $PORT                       "
echo "║  数据: $DATA_DIR"
echo "║  健康: http://localhost:$PORT/health"
echo "╚══════════════════════════════════════╝"

mkdir -p "$DATA_DIR"
exec "$JAVA_BIN" \
    -Dtodo.db.path="$DATA_DIR" \
    -jar "$JAR_FILE"
