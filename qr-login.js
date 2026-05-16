const QRCode = require('qrcode')
const fs = require('fs')

async function main() {
  // Get QR code key from netease-api
  const keyRes = await fetch('http://localhost:3000/login/qr/key')
  const keyData = await keyRes.json()
  const key = keyData.data.unikey
  console.log('QR Code Key:', key)

  // Generate QR image
  const qrUrl = `https://music.163.com/login?codekey=${key}`
  const qrDataUrl = await QRCode.toDataURL(qrUrl, { width: 300, margin: 2 })

  // Save QR code image
  const base64Data = qrDataUrl.replace(/^data:image\/png;base64,/, '')
  fs.writeFileSync('E:/music player/qrcode.png', Buffer.from(base64Data, 'base64'))
  console.log('QR code saved to E:/music player/qrcode.png')

  // Open with default browser
  const { exec } = require('child_process')
  exec('start "" "E:/music player/qrcode.png"', (err) => {
    if (err) console.error('Failed to open QR code:', err)
  })

  console.log('\n===========================================')
  console.log('请打开网易云音乐APP扫描二维码登录')
  console.log('二维码图片路径: E:/music player/qrcode.png')
  console.log('===========================================\n')

  // Check QR status every 3 seconds for 2 minutes
  for (let i = 0; i < 40; i++) {
    await new Promise(r => setTimeout(r, 3000))

    const checkRes = await fetch(`http://localhost:3000/login/qr/check?key=${key}`)
    const checkData = await checkRes.json()
    console.log(`\n检查 ${i+1}:`, JSON.stringify(checkData, null, 2))

    // Check based on actual response structure
    const code = checkData.body?.code || checkData.code

    if (code === 803) {
      console.log('\n===========================================')
      console.log('登录成功!')
      console.log('===========================================\n')

      // Get cookie
      const cookie = checkData.cookie.join(';')
      console.log('获取到 Cookie，长度:', cookie.length)

      // Save to .env
      const envPath = 'E:/music player/netease-api/.env'
      let envContent = fs.readFileSync(envPath, 'utf-8')

      const lines = envContent.split('\n')
      const newLines = lines.filter(line =>
        line.startsWith('NODE_ENV=') || line.startsWith('PORT=')
      )
      newLines.push(`MUSIC_U=${cookie}`)

      const nmtidLine = lines.find(line => line.startsWith('NMTID='))
      if (nmtidLine) {
        newLines.push(nmtidLine)
      }

      fs.writeFileSync(envPath, newLines.join('\n'))
      console.log('已保存到 .env 文件')

      process.exit(0)
    } else if (code === 800) {
      console.log('二维码已过期，请重新运行脚本')
      process.exit(1)
    } else {
      console.log(`等待扫码中... (${i*3}s)`)
    }
  }

  console.log('\n超时未扫码，请重新运行脚本')
  process.exit(1)
}

main().catch(console.error)