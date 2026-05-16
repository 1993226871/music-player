const { serveNcmApi } = require('NeteaseCloudMusicApi/server')

const PORT = 3000

// 启动服务
async function start() {
    const app = await serveNcmApi({ port: PORT })
    console.log(`NeteaseCloudMusicApi running on http://localhost:${PORT}`)
}

start().catch(console.error)