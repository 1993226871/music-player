#!/bin/sh
set -e

# Wait for backend to be ready
echo "Waiting for backend..."
max_attempts=30
attempt=0
while [ $attempt -lt $max_attempts ]; do
  if wget --spider --timeout=2 -q "http://localhost:8080/api/auth/me" 2>/dev/null || wget --spider --timeout=2 -q "http://localhost:8080/api/admin/cookie" 2>/dev/null; then
    echo "Backend is ready"
    break
  fi
  attempt=$((attempt + 1))
  echo "Attempt $attempt/$max_attempts - waiting..."
  sleep 2
done

# Fetch cookie from backend using wget
echo "Fetching cookie from backend..."
COOKIE_RESPONSE=$(wget -O- -q "http://localhost:8080/api/admin/cookie")
COOKIE=$(echo "$COOKIE_RESPONSE" | grep -o '"MUSIC_U":"[^"]*"' | cut -d'"' -f4)

if [ -n "$COOKIE" ] && [ "$COOKIE" != "null" ] && [ ${#COOKIE} -gt 100 ]; then
  echo "Got cookie from database, length: ${#COOKIE}"
  # Write to .env file
  cat > /app/.env << 'ENVEOF'
NODE_ENV=production
PORT=3000
MUSIC_U={{COOKIE}}
NMTID=00O6y2ayCYZyQXy-E1WkPZ2PdKwZM4AAAGeLNgYEw
ENVEOF
  # Replace placeholder with actual cookie
  sed -i "s/{{COOKIE}}/$COOKIE/" /app/.env
  echo "Cookie written to /app/.env"
else
  echo "No valid cookie in database, using anonymous token"
  cat > /app/.env << 'ENVEOF'
NODE_ENV=production
PORT=3000
MUSIC_U=
NMTID=00O6y2ayCYZyQXy-E1WkPZ2PdKwZM4AAAGeLNgYEw
ENVEOF
fi

echo "Starting netease-api..."
exec node app.js