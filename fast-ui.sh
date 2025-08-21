#!/bin/bash

# Fast UI deployment - Just get the frontend working quickly
set -e

INSTANCE_IP="54.221.140.224"
KEY_FILE="story-publisher-key.pem"

echo "🚀 Fast UI Deployment..."

# Just test current backend first
echo "🔍 Testing current backend..."
curl -s http://$INSTANCE_IP:8080/api/stories/public && echo " ✅ Backend is working!" || echo " ❌ Backend not ready"

# Quick frontend deployment with simple static files
echo "📱 Setting up simple frontend..."

ssh -i "$KEY_FILE" -o StrictHostKeyChecking=no ubuntu@$INSTANCE_IP << 'EOF'
# Create simple HTML frontend
mkdir -p ~/simple-ui
cd ~/simple-ui

# Create simple index.html
cat > index.html << 'HTML_EOF'
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Story Publisher</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 40px; background: #f5f5f5; }
        .container { max-width: 800px; margin: 0 auto; background: white; padding: 20px; border-radius: 8px; }
        h1 { color: #333; text-align: center; }
        .api-test { background: #f0f0f0; padding: 15px; border-radius: 5px; margin: 20px 0; }
        button { background: #007bff; color: white; border: none; padding: 10px 20px; border-radius: 5px; cursor: pointer; }
        button:hover { background: #0056b3; }
        #result { background: #e9ecef; padding: 10px; border-radius: 5px; margin-top: 10px; max-height: 300px; overflow-y: auto; }
        .nav { text-align: center; margin: 20px 0; }
        .nav a { margin: 0 10px; color: #007bff; text-decoration: none; }
        .nav a:hover { text-decoration: underline; }
    </style>
</head>
<body>
    <div class="container">
        <h1>🚀 Story Publisher</h1>
        <p style="text-align: center; color: #666;">Your story publishing platform is live!</p>
        
        <div class="nav">
            <a href="/">Home</a>
            <a href="/test.html">API Test</a>
            <a href="/stories.html">Stories</a>
            <a href="/auth.html">Authentication</a>
        </div>

        <div class="api-test">
            <h3>🔗 Quick API Test</h3>
            <button onclick="testAPI()">Test Backend API</button>
            <div id="result"></div>
        </div>

        <div style="text-align: center; margin-top: 30px;">
            <h3>📚 Available Endpoints</h3>
            <p><strong>Backend API:</strong> <a href="http://54.221.140.224:8080/api/stories/public" target="_blank">http://54.221.140.224:8080/api/stories/public</a></p>
            <p><strong>Auth API:</strong> <a href="http://54.221.140.224:8080/api/auth/login" target="_blank">Login Endpoint</a></p>
        </div>
    </div>

    <script>
        async function testAPI() {
            const result = document.getElementById('result');
            result.innerHTML = 'Testing API...';
            
            try {
                const response = await fetch('http://54.221.140.224:8080/api/stories/public');
                const data = await response.json();
                result.innerHTML = '<strong>✅ API Working!</strong><br><pre>' + JSON.stringify(data, null, 2) + '</pre>';
            } catch (error) {
                result.innerHTML = '<strong>❌ API Error:</strong><br>' + error.message;
            }
        }
    </script>
</body>
</html>
HTML_EOF

# Start simple HTTP server
echo "Starting simple web server..."
nohup python3 -m http.server 3000 > server.log 2>&1 &
echo $! > server.pid

echo "✅ Simple UI server started on port 3000"
EOF

echo ""
echo "🎉 Fast deployment completed!"
echo ""
echo "🌐 Access your application:"
echo "   📱 Frontend UI: http://$INSTANCE_IP:3000"
echo "   🔧 Backend API: http://$INSTANCE_IP:8080/api"
echo "   📚 Stories: http://$INSTANCE_IP:8080/api/stories/public"
echo ""
echo "🔍 Test the UI now!"
