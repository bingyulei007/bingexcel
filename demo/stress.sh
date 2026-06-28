#!/usr/bin/env bash
# ============================================================
# BingExcel demo - podman HTTP 并发压测脚本
#
# 用法 (在 Windows Git Bash 中执行):
#   bash demo/stress.sh                         # 默认 50 并发 1000 请求
#   CONCURRENCY=100 TOTAL=5000 bash demo/stress.sh
#
# 流程:
#   1. 构建镜像 (multi-stage Dockerfile, 根目录运行)
#   2. 启动 demo 容器
#   3. 解析 WSL VM IP (podman 在 Windows 上不对 localhost 暴露端口)
#   4. 用 python:3-alpine 容器跑 stress.py 打并发流量
#   5. 收集结果并清理容器
# ============================================================
set -euo pipefail

YELLOW='\033[1;33m'; GREEN='\033[1;32m'; RED='\033[1;31m'; CYAN='\033[1;36m'; NC='\033[0m'

# --- 参数 ---
CONCURRENCY="${CONCURRENCY:-50}"
TOTAL="${TOTAL:-1000}"
IMAGE_TAG="bing-excel-demo:latest"
CONTAINER_NAME="bing-excel-demo-stress"
PYTHON_IMAGE="python:3.12-alpine"

echo -e "${YELLOW}=== BingExcel demo stress test ===${NC}"
echo -e "  Concurrency : ${CYAN}${CONCURRENCY}${NC}"
echo -e "  Total reqs  : ${CYAN}${TOTAL}${NC}"

# --- 1. 构建镜像 ---
echo -e "${YELLOW}=== Building image ${IMAGE_TAG}...${NC}"
if podman image exists "${IMAGE_TAG}" 2>/dev/null; then
    echo -e "  Image already exists, skipping build"
else
    podman build -t "${IMAGE_TAG}" -f Dockerfile . || {
        echo -e "${RED}ERROR: image build failed${NC}"; exit 1;
    }
fi

# --- 2. 启动容器 ---
echo -e "${YELLOW}=== Starting container ${CONTAINER_NAME}...${NC}"
podman rm -f "${CONTAINER_NAME}" >/dev/null 2>&1 || true
podman run -d --name "${CONTAINER_NAME}" -p 8080:8080 "${IMAGE_TAG}" >/dev/null
echo -e "  Container started: ${CYAN}${CONTAINER_NAME}${NC}"

cleanup() {
    echo -e "${YELLOW}=== Cleaning up...${NC}"
    podman rm -f "${CONTAINER_NAME}" >/dev/null 2>&1 || true
}
trap cleanup EXIT

# --- 3. 解析 WSL VM IP ---
echo -e "${YELLOW}=== Resolving Podman WSL VM IP...${NC}"
VM_IP=$(wsl -d podman-machine-default sh -c 'ip -4 addr show eth0 2>/dev/null | grep -oP "inet \K[0-9.]+" | head -1' 2>/dev/null || true)
if [ -z "${VM_IP}" ]; then
    echo -e "${RED}ERROR: could not resolve Podman WSL VM IP${NC}"
    echo -e "  Tip: ensure 'podman machine start' has been run"
    exit 1
fi
echo -e "  VM IP: ${CYAN}${VM_IP}${NC}"

TARGET="http://${VM_IP}:8080"

# --- 4. 等待应用启动 ---
# 从 Windows curl 检查 (能访问 VM IP)
echo -e "${YELLOW}=== Waiting for app to start...${NC}"
for i in $(seq 1 30); do
    code=$(curl -s -o /dev/null -w "%{http_code}" --max-time 3 "${TARGET}/api/excel/download-sample" 2>/dev/null || echo "000")
    if [ "${code}" = "200" ] || [ "${code}" = "302" ]; then
        echo -e "  ${GREEN}App is up (attempt ${i}, HTTP ${code})${NC}"
        break
    fi
    if [ "$i" -eq 30 ]; then
        echo -e "${RED}ERROR: app did not start within 60s${NC}"
        podman logs "${CONTAINER_NAME}" | tail -30
        exit 1
    fi
    sleep 2
done

# --- 5. 跑压测 (python 容器) ---
echo -e "${YELLOW}=== Running stress test (c=${CONCURRENCY}, n=${TOTAL})...${NC}"
# --network=host 让 python 容器共享 WSL VM 网络栈, 可直接访问 127.0.0.1:8080
# (demo 容器用 -p 8080:8080 把端口映射到 VM 的 127.0.0.1)
podman run --rm --network=host \
    -v "$(pwd)/demo/stress.py:/stress.py:ro" \
    "${PYTHON_IMAGE}" \
    python3 /stress.py --host "http://127.0.0.1:8080" --concurrency "${CONCURRENCY}" --total "${TOTAL}"

echo ""
echo -e "${GREEN}=== Stress test complete ===${NC}"
