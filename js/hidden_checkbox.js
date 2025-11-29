document.addEventListener('DOMContentLoaded', () => {
    // 💡 既存の change イベントリスナー設定はそのまま維持してください
    const checkboxes = document.querySelectorAll('#language-toggles input[type="checkbox"]');

    checkboxes.forEach(checkbox => {
        checkbox.addEventListener('change', toggleVisibility); // changeイベントで実行される関数
    });
    
    // 💡 初期表示時、全てチェックされていない状態から始めるため、全ての例文を非表示にします。
    // この処理は、checkAll(false)とほぼ同じ動作になりますが、ここでは手動で隠します。
    document.querySelectorAll('.example-sentence').forEach(el => el.classList.add('hidden'));
});

// changeイベントで実行される関数を分離 (再利用性を高めるため)
function toggleVisibility() {
    const language = this.value;
    const sentences = document.querySelectorAll('.' + language);
    
    if (this.checked) {
        sentences.forEach(sentence => sentence.classList.remove('hidden'));
    } else {
        sentences.forEach(sentence => sentence.classList.add('hidden'));
    }
}


/**
 * 全てのチェックボックスを操作し、表示を更新する関数
 * @param {boolean} shouldCheck - true: 全てチェック, false: 全てチェックを外す
 */
function checkAll(shouldCheck) {
    // 全てのチェックボックスを取得
    const checkboxes = document.querySelectorAll('#language-toggles input[type="checkbox"]');
    
    checkboxes.forEach(checkbox => {
        // ① チェックボックスの状態を更新
        checkbox.checked = shouldCheck; 
        
        // ② 関連する例文の表示も更新
        const language = checkbox.value;
        const sentences = document.querySelectorAll('.' + language);
        
        if (shouldCheck) {
            // true (全チェック) の場合、全て表示
            sentences.forEach(sentence => sentence.classList.remove('hidden'));
        } else {
            // false (全外し) の場合、全て非表示
            sentences.forEach(sentence => sentence.classList.add('hidden'));
        }
    });
}