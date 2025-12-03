const scriptElement = document.querySelector('script[src*="title_changer.js"]');

if (scriptElement) {
  // 現在のURLのプロトコルを取得 (例: 'file:', 'http:', 'https:')
  const currentProtocol = window.location.protocol;
  // title要素を取得
  const titleElement = document.querySelector('title');
  const pageTitle = scriptElement.getAttribute('page-title');

  if (currentProtocol === 'file:') {
      // ローカルファイルとして開いている場合
      titleElement.textContent = pageTitle + '(ローカル)';
      
  } else {
      // サーバー経由で開いている場合 (http: や https: など)
      titleElement.textContent = pageTitle;
  }
}