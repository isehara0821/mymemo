
let isJapanese = false;
function toggleContent(targetLang) {
  // ① 全ての切り替えグループを取得
  const groups = document.querySelectorAll('.color-switch-group');
  const buttonElement = document.getElementById('toggleButton');

  if (isJapanese) {
    buttonElement.textContent = "例文を日本語表示にする";
    isJapanese = false;
  }else{
    buttonElement.textContent = "例文を"+targetLang+'表示にする';
    isJapanese = true;
  }

  // ② 各グループに対して処理を繰り返す
  groups.forEach(group => {
    // グループ内のexmp_sentとtransitionを取得
    const text1 = group.querySelector('.exmp_sent');
    const text2 = group.querySelector('.transition');

    // ③ クラスを交換（前回のトグル処理と同じ）
    if (text1 && text2) {
      // text1: fblack と fblue をトグル
      text1.classList.toggle('font_color_black');
      text1.classList.toggle('font_color_same_background');

      // text2: fblue と fblack をトグル
      text2.classList.toggle('font_color_same_background');
      text2.classList.toggle('font_color_black');
    }
  });
}