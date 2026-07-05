#!/usr/bin/env bash
set -euo pipefail

PUBLIC_IP="${1:-}"
if [[ -z "$PUBLIC_IP" ]]; then
  echo "Usage: bash scripts/deploy-ec2.sh <ec2-public-ip>" >&2
  exit 1
fi

export REACT_APP_API_URL="${REACT_APP_API_URL:-/api}"
export JWT_SECRET="${JWT_SECRET:-gocomet-super-secret-key-for-jwt-signing-2024}"
export DB_USER="${DB_USER:-postgres}"
export DB_PASS="${DB_PASS:-postgres}"

sudo apt-get update
sudo apt-get install -y ca-certificates curl gnupg lsb-release
sudo install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu $(. /etc/os-release && echo "$VERSION_CODENAME") stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
sudo apt-get update
sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin
sudo systemctl enable docker
sudo systemctl start docker
sudo usermod -aG docker "$USER"

sudo mkdir -p /opt/shipment-tracking-system
sudo cp -r . /opt/shipment-tracking-system/
cd /opt/shipment-tracking-system
sudo docker compose pull
sudo docker compose up -d --build

cat <<EOF
Deployment complete.
Open http://$PUBLIC_IP
Login with: demo@gocomet.com / demo123
EOF
