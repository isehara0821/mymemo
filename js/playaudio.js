// ① audio要素を取得
const triggers = document.querySelectorAll('.sound-trigger');

// 各画像にクリックイベントを設定する
triggers.forEach(trigger => {
    trigger.addEventListener('click', function() {
        // クリックされた画像の data-target 属性の値（例: 'audio1'）を取得
        const targetId = this.dataset.target; 
        
        // 取得したIDを使って対応する audio 要素を取得
        const audio = document.getElementById(targetId);

        if (audio) {
            // 連続クリック時に音を重ねない処理
            audio.pause();
            audio.currentTime = 0; 
            
            // 再生開始
            audio.play()
                .catch(error => {
                    console.error("音声再生エラー:", error);
                });
        }
    });
});