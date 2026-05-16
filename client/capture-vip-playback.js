import { chromium } from '@playwright/test';

(async () => {
  const browser = await chromium.launch({ headless: false });
  const context = await browser.newContext();
  const page = await context.newPage();

  const requests = [];
  const responses = [];

  page.on('request', request => {
    const url = request.url();
    requests.push({ method: request.method(), url, headers: request.headers() });
  });

  page.on('response', response => {
    responses.push({ status: response.status(), url: response.url() });
  });

  try {
    // 1. 访问网易云网页版
    console.log('1. 访问网易云网页版...');
    await page.goto('https://music.163.com', { waitUntil: 'networkidle', timeout: 30000 });
    await page.waitForTimeout(2000);

    // 检查是否已登录
    const isLoggedIn = await page.evaluate(() => {
      return document.querySelector('.user-info, .nickname') !== null;
    });
    console.log('Is logged in:', isLoggedIn);

    if (!isLoggedIn) {
      console.log('\n请手动登录网易云账号...');
      console.log('登录后按 Enter 继续...');
      await new Promise(resolve => {
        process.stdin.once('data', () => resolve());
      });
    }

    // 3. 搜索VIP歌曲
    console.log('\n3. 导航到搜索页面...');
    await page.goto('https://music.163.com/#/search?m=search&s=%E8%94%A1%E4%BE%9D%E6%9E%97%20%E5%A4%A7%E8%89%BA%E6%9C%AF%E5%AE%B6', { waitUntil: 'networkidle', timeout: 30000 });
    await page.waitForTimeout(3000);

    console.log('请手动点击播放一首VIP歌曲...');
    console.log('播放 10 秒后按 Enter 继续...');
    await new Promise(resolve => {
      process.stdin.once('data', () => resolve());
    });

    // 查看audio状态
    const audioInfo = await page.evaluate(() => {
      const audio = document.querySelector('audio');
      if (!audio) return { error: 'no audio element' };
      return {
        src: audio.src,
        currentTime: audio.currentTime,
        duration: audio.duration,
        paused: audio.paused
      };
    });
    console.log('\n=== Audio 状态 ===');
    console.log(JSON.stringify(audioInfo, null, 2));

  } catch (error) {
    console.error('Error:', error.message);
  } finally {
    // 输出所有包含 song 或 url 的请求
    console.log('\n\n=== 关键网络请求 ===');
    requests.filter(r => r.url.includes('song') || r.url.includes('url') || r.url.includes('.mp3') || r.url.includes('enhance'))
      .slice(-50)
      .forEach(r => {
        console.log(`[${r.method}] ${r.url.substring(0, 250)}`);
      });
    await browser.close();
  }
})();