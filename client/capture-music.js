import { chromium } from '@playwright/test';
import fs from 'fs';

(async () => {
  const browser = await chromium.launch({ headless: false });
  const context = await browser.newContext();
  const page = await context.newPage();

  console.log('========================================');
  console.log('网易云音乐抓包工具 v5 - 完整Cookie提取');
  console.log('========================================\n');

  console.log('1. 打开浏览器并访问网易云...');
  await page.goto('https://music.163.com', { waitUntil: 'networkidle', timeout: 30000 });
  await page.waitForTimeout(2000);

  console.log('2. 请在浏览器中扫码登录...\n');

  // 等待登录
  let loggedIn = false;
  for (let i = 0; i < 60; i++) {
    await new Promise(r => setTimeout(r, 1000));
    try {
      const userInfo = await page.evaluate(() => {
        const nick = document.querySelector('.nickname, .user-name');
        return nick ? nick.textContent : null;
      });
      if (userInfo) {
        console.log(`   已登录: ${userInfo}`);
        loggedIn = true;
        break;
      }
    } catch {}
    if (i % 10 === 0) console.log(`   等待登录... ${i}s`);
  }

  // 等待额外3秒确保cookies完全
  await new Promise(r => setTimeout(r, 3000));

  // 获取所有cookies
  console.log('\n3. 提取所有Cookies...');
  const allCookies = await context.cookies();
  fs.writeFileSync('E:/music player/all-cookies.json', JSON.stringify(allCookies, null, 2));

  // 找到关键认证cookies
  const authCookies = allCookies.filter(c =>
    c.name === 'MUSIC_U' ||
    c.name === 'NMTID' ||
    c.name === 'OSessionId' ||
    c.name === 'OSupid' ||
    c.name.includes('_csrf') ||
    c.name.includes('token')
  );

  const cookieStr = authCookies.map(c => `${c.name}=${c.value}`).join('; ');
  fs.writeFileSync('E:/music player/auth-cookie.txt', cookieStr);

  console.log(`   Total cookies: ${allCookies.length}`);
  console.log(`   Auth cookies: ${authCookies.length}`);
  authCookies.forEach(c => console.log(`   - ${c.name}: ${c.value.substring(0, 40)}...`));

  console.log('\n4. 保存Cookie到文件...');
  console.log('   auth-cookie.txt 已保存');

  console.log('\n5. 搜索并播放"蔡依林 大艺术家"...');
  await page.goto('https://music.163.com/#/search?m=search&s=%E8%94%A1%E4%BE%9D%E6%9E%97%20%E5%A4%A7%E8%89%BA%E6%9C%AF%E5%AE%B6', { waitUntil: 'networkidle', timeout: 30000 });
  await page.waitForTimeout(3000);

  // 点击播放
  try {
    await page.locator('.srchsonglist .item').first().click();
    console.log('   已点击播放');
  } catch(e) {
    console.log('   点击失败:', e.message);
  }

  console.log('\n6. 等待 30 秒抓包...');
  await new Promise(r => setTimeout(r, 30000));

  // 最终audio状态
  const audioInfo = await page.evaluate(() => {
    const audio = document.querySelector('audio');
    return audio ? {
      src: audio.src?.substring(0, 100),
      currentTime: Math.round(audio.currentTime),
      duration: Math.round(audio.duration),
      paused: audio.paused
    } : { error: 'no audio' };
  });

  console.log('\n7. Audio状态:');
  console.log(JSON.stringify(audioInfo, null, 2));

  console.log('\n========================================');
  console.log('抓包完成');
  console.log('========================================');
  console.log('\n下一步: 使用提取的cookie调用API');

  await browser.close();
  process.exit(0);
})();