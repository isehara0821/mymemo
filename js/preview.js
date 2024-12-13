async function searchInHtmlContent(url, keyword) {
    try {
        // 外部HTMLを取得
        const response = await fetch(url);
        if (!response.ok) {
            throw new Error(`HTTPエラー: ${response.status}`);
        }
        const htmlContent = await response.text();

        // HTMLを行ごとに分割
        const lines = htmlContent.split("\n");

        // キーワードに一致する行を抽出
        const matchedLines = lines.filter(line => line.includes(keyword));

        // 結果を表示
        const resultsDiv = document.getElementById('results');
        resultsDiv.innerHTML = ''; // クリア
        if (matchedLines.length > 0) {
            matchedLines.forEach(line => {
                // 抽出した行をそのまま挿入
                const div = document.createElement('div');
                div.innerHTML = line;
                // div.textContent = line; // HTMLをそのまま表示したくない場合はtextContentを使用
                resultsDiv.appendChild(div);
            });
        } else {
            resultsDiv.innerHTML = '<p>該当する結果がありません。</p>';
        }
        // 検索結果の高さを取得
        const resultsHeight = resultsDiv.offsetHeight;

        // 既存のコンテンツを下にずらす
        const contentDiv = document.getElementById('index_contents');
        contentDiv.style.marginTop = `${resultsHeight + 20}px`; // 検索結果の高さ + 余白
    } catch (error) {
        console.error('エラーが発生しました:', error);
    }
}

// 検索ボタンのイベントリスナー
document.getElementById('searchButton').addEventListener('click', () => {
    const url = './other/memo.html'; // 検索対象のHTMLファイルのURL
    const keyword = document.getElementById('searchBox').value; // 入力されたキーワード
    searchInHtmlContent(url, keyword);
});
