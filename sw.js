/* 血圧記録ノート Service Worker（v5：統計グラフ・血圧入力の矢印に対応）
   アプリを更新したら CACHE の番号を上げてください。 */
const CACHE = 'bp-note-v5';
const FILES = [
  './',
  './index.html',
  './manifest.webmanifest',
  './icons/icon-192.png',
  './icons/icon-512.png',
  './icons/maskable-192.png',
  './icons/maskable-512.png',
  './icons/apple-touch-icon.png'
];

self.addEventListener('install', e => {
  e.waitUntil(caches.open(CACHE).then(c => c.addAll(FILES)).then(() => self.skipWaiting()));
});

self.addEventListener('activate', e => {
  e.waitUntil(
    caches.keys()
      .then(keys => Promise.all(keys.filter(k => k !== CACHE).map(k => caches.delete(k))))
      .then(() => self.clients.claim())
  );
});

// 画面(HTML)：ネット優先。ただし 404 などエラー応答のときは保存済みの画面を使い、上書きもしない
self.addEventListener('fetch', e => {
  const req = e.request;
  if (req.method !== 'GET' || new URL(req.url).origin !== location.origin) return;
  if (req.mode === 'navigate') {
    e.respondWith(
      fetch(req).then(res => {
        if (!res.ok) return caches.match('./index.html').then(hit => hit || res);
        const copy = res.clone();
        caches.open(CACHE).then(c => c.put('./index.html', copy));
        return res;
      }).catch(() => caches.match('./index.html'))
    );
    return;
  }
  // 画像など：保存済み優先
  e.respondWith(caches.match(req).then(hit => hit || fetch(req)));
});
