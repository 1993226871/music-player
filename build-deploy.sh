#!/bin/bash
# build-deploy.sh - Build Tauri app and deploy to cloud server

set -e

# Configuration
SERVER_IP="119.91.55.249"
SERVER_USER="root"
REMOTE_DIR="/root/music-player"

echo "=== Building Tauri App ==="
cd client
npm run build
npx tauri build --bundles nsis

echo "=== Copying files to server ==="
# Create tar archive of built files
tar -czf /tmp/music-app.tar.gz \
  -C dist . \
  -C ../server/target music-player-1.0.0.jar \
  -C ../Dockerfile.server . \
  -C ../Dockerfile.netease . \
  -C ../docker-compose.yml . \
  -C ../server/src/main/resources application-prod.yml \
  -C ../server/docker-entrypoint.sh .

# Upload and extract on server
scp /tmp/music-app.tar.gz ${SERVER_USER}@${SERVER_IP}:/tmp/
ssh ${SERVER_USER}@${SERVER_IP} "
  mkdir -p ${REMOTE_DIR}
  tar -xzf /tmp/music-app.tar.gz -C ${REMOTE_DIR}
  cd ${REMOTE_DIR}
  docker build -t music-server -f Dockerfile.server .
  docker build -t music-netease -f Dockerfile.netease .
  docker-compose up -d
"

echo "=== Deploy complete ==="
echo "Server running at: http://${SERVER_IP}:8080"