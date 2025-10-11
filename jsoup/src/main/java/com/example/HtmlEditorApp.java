package com.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HtmlEditorApp extends JFrame {

    private final String originalHtmlContent;
    private final Map<String, JTextField> h2Fields = new LinkedHashMap<>();
    // h3Fieldsという名前ですが、実際には <summary class="common_h3"> の内容を格納しています。
    private final Map<String, JTextField> h3Fields = new LinkedHashMap<>();
    private final JPanel h4ContentPanel = new JPanel();

    // ★★★ インプットとなるHTMLファイル（添付ファイルの内容） ★★★
    private static final String ATTACHED_HTML = "<!DOCTYPE html>\n" +
            "<html lang=\"en\" dir=\"ltr\">\n" +
            "  <head>\n" +
            "    <meta charset=\"utf-8\">\n" +
            "    <meta name=\"viewport\" content=\"width=device-width,initial-scale=1.0,user-scalable=yes\" />\n" +
            "    <link rel=\"stylesheet\" href=\"../../css/common.css\">\n" +
            "    <link rel=\"stylesheet\" href=\"../../css/table.css\">\n" +
            "    <link rel=\"icon\" type=\"image/png\" href=\"../../img/pochama.png\">\n" +
            "    <title>日本</title>\n" +
            "  </head>\n" +
            "  <body>\n" +
            "    <div class=\"common_top\">\n" +
            "      <h2>日本史について忘れたくないことのメモ</h2>\n" +
            "    </div>\n" +
            "    <div class=\"common_contents\">\n" +
            "      <details open>\n" +
            "        <summary class=\"common_h3\">流れ</summary>\n" +
            "        <h4 class=\"common_section\">戦国</h4>\n" +
            "        <div class=\"contents_h4\">\n" +
            "          <h5 class=\"common_h5\">畿内戦国史</h5>\n" +
            "          <div class=\"contents_h5\">\n" +
            "          <ul class=\"pad_h5\">\n" +
            "            <li><a href=\"./jinbutsu/ashikaga_yoshihisa.html\">足利義尚と鈎の陣（足利義尚の頁参照）</a></li>\n" +
            "          </ul>\n" +
            "        </div>\n" +
            "      </details>\n" +
            "    </div>\n" +
            "    <div class=\"common_contents\">\n" +
            "      <details open>\n" +
            "        <summary class=\"common_h3\">出来事</summary>\n" +
            "        <h4 class=\"common_section\">奈良</h4>\n" +
            "        <div class=\"contents_h4\">\n" +
            "          <li><a href=\"./dekigoto/nagayaou_no_hen.html\">長屋王の変</a></li>\n" +
            "          <li><a href=\"./dekigoto/fujiwara_hirotsuguno_ran.html\">藤原広嗣の変</a></li>\n" +
            "          <li><a href=\"./dekigoto/tachibananonaramaro_no_hen.html\">橘奈良麻呂の変</a></li>\n" +
            "          <li><a href=\"./dekigoto/eminooshikatshuno_ran.html\">恵美押勝の乱</a></li>\n" +
            "        </div>\n" +
            "      </details>\n" +
            "    </div>\n" +
            "    <div class=\"common_contents\">\n" +
            "      <details open>\n" +
            "        <summary class=\"common_h3\">その他</summary>\n" +
            "        <h4 class=\"common_section\">用語</h4>\n" +
            "        <div class=\"contents_h4\">\n" +
            "          <ul class=\"pad_h5\">\n" +
            "            <li><a href=\"./other/hanhajime.html\">判始</a>（はんはじめ）</li>\n" +
            "            <li><a href=\"./other/hanhajime.html\">寺社本所領返還政策</a>（じしゃほんじょりょうへんかんせいさく）</li>\n" +
            "          </ul>\n" +
            "        </div>\n" +
            "      </details>\n" +
            "    </div>\n" +
            "    <div class=\"common_footer\">\n" +
            "      <p class=\"link\"><a href=\"../../index.html\">TOPへ戻る</a></p>\n" +
            "    </div>\n" +
            "  </body>\n" +
            "</html>";
    
    // --- コンストラクタ ---
    public HtmlEditorApp(String htmlContent) {
        this.originalHtmlContent = htmlContent;
        setTitle("HTMLコンテンツエディタ");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setPreferredSize(new Dimension(800, 600));

        parseAndBuildGUI(mainPanel);

        JButton saveButton = new JButton("変更をHTMLとして保存");
        saveButton.addActionListener(this::saveHtml);
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(saveButton);

        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
    }
    
    // --- HTML解析とGUI構築のロジック ---
    private void parseAndBuildGUI(JPanel mainPanel) {
        
        // 1. h2タグ (タイトル) - 編集可能
        mainPanel.add(new JLabel("--- h2タグ（タイトル）の編集 ---"));
        extractAndAddField(mainPanel, "h2", h2Fields, "h2", "タイトル", 60);
        
        // 2. summaryタグ (大項目) - 編集可能
        // ★ h3Fieldsを利用し、summaryタグの内容を抽出・表示します ★
        mainPanel.add(new JLabel("--- SUMMARYタグ（大項目）の編集・追加 ---"));
        extractAndAddField(mainPanel, "<summary class=\"common_h3\">", h3Fields, "summary", "大項目", 40);

        // 3. summaryタグの追加ボタン
        JButton addH3Button = new JButton("大項目（SUMMARY）の追加");
        addH3Button.addActionListener(e -> addNewH3Field(mainPanel));
        mainPanel.add(addH3Button);
        
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        // 4. h4と<li>タグ
        mainPanel.add(new JLabel("--- h4/liタグ（小項目/リンク）の編集・追加 ---"));
        
        h4ContentPanel.setLayout(new BoxLayout(h4ContentPanel, BoxLayout.Y_AXIS));
        mainPanel.add(h4ContentPanel);

        // h4/liの抽出・GUI構築
        extractH4AndLi();

        // 5. h4タグの追加ボタン
        JButton addH4Button = new JButton("h4タグの追加 (新規 common_contents ブロックも追加)");
        addH4Button.addActionListener(e -> addNewH4Block());
        mainPanel.add(addH4Button);
    }
    
    /**
     * 特定のタグの内容を抽出し、TextFieldをGUIに追加します。
     * @param tag 検索する開始タグの文字列 (例: "h2", "<summary class=\"common_h3\">")
     * @param tagName 検索する終了タグのタグ名 (例: "h2", "summary")
     */
    private void extractAndAddField(JPanel panel, String tag, Map<String, JTextField> fields, String tagName, String labelPrefix, int columns) {
        // 正規表現でタグの内容を抽出
        Pattern pattern = Pattern.compile(Pattern.quote(tag) + "(.*?)</" + tagName + ">", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(originalHtmlContent);
        int index = 1;

        while (matcher.find()) {
            String originalText = matcher.group(1).trim();
            JTextField textField = new JTextField(originalText, columns);
            fields.put(originalText, textField);
            
            JPanel p = new JPanel(new BorderLayout());
            p.setMaximumSize(new Dimension(Short.MAX_VALUE, p.getPreferredSize().height));
            p.add(new JLabel(labelPrefix + index + ": "), BorderLayout.WEST);
            p.add(textField, BorderLayout.CENTER);
            panel.add(p);
            index++;
        }
    }

    // summary（h3相当）追加フィールドの処理
    private void addNewH3Field(JPanel mainPanel) {
        String placeholder = "新しい大項目";
        JTextField newField = new JTextField(placeholder, 40);
        h3Fields.put("NEW_H3_KEY_" + System.currentTimeMillis(), newField);

        JPanel p = new JPanel(new BorderLayout());
        p.setMaximumSize(new Dimension(Short.MAX_VALUE, p.getPreferredSize().height));
        p.add(new JLabel("新規H3: "), BorderLayout.WEST);
        p.add(newField, BorderLayout.CENTER);

        // 既存のH3フィールド群の下に追加するためのインデックスを検索
        Component lastH3 = null;
        for (Component c : mainPanel.getComponents()) {
            if (c instanceof JPanel && ((JPanel)c).getComponent(0) instanceof JLabel && ((JLabel)((JPanel)c).getComponent(0)).getText().startsWith("大項目")) {
                lastH3 = c;
            }
        }
        
        int index = -1;
        if (lastH3 != null) {
            for(int i = 0; i < mainPanel.getComponentCount(); i++) {
                if(mainPanel.getComponent(i) == lastH3) {
                    index = i + 1;
                    break;
                }
            }
        }
        
        if (index != -1) {
            mainPanel.add(p, index);
        } else {
             mainPanel.add(p);
        }
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    // h4とliの内容を抽出・GUI構築 (変更なし)
    private void extractH4AndLi() {
        // <div class="common_contents">...</div> ブロック全体を取得
        Pattern contentsPattern = Pattern.compile("(<div class=\"common_contents\">.*?</div>)", Pattern.DOTALL);
        Matcher contentsMatcher = contentsPattern.matcher(originalHtmlContent);

        while (contentsMatcher.find()) {
            String contentBlock = contentsMatcher.group(1);
            
            // h4タグの内容を抽出（セクション名）
            Pattern h4Pattern = Pattern.compile("<h4 class=\"common_section\">(.*?)</h4>", Pattern.DOTALL);
            Matcher h4Matcher = h4Pattern.matcher(contentBlock);
            
            if (h4Matcher.find() && contentBlock.contains("<div class=\"contents_h4\">")) {
                 String h4Text = h4Matcher.group(1).trim();
                 
                 H4BlockEditor block = new H4BlockEditor(h4Text, contentBlock);
                 h4ContentPanel.add(block);
            }
        }
    }

    // 新しいh4ブロックの追加 (変更なし)
    private void addNewH4Block() {
        H4BlockEditor newBlock = new H4BlockEditor("新規h4セクション", null);
        h4ContentPanel.add(newBlock);
        h4ContentPanel.revalidate();
        h4ContentPanel.repaint();
    }
    
    // --- HTML保存のロジック ---
    private void saveHtml(ActionEvent event) {
        String newHtml = originalHtmlContent;
        
        // 1. h2タグの置換
        for (Map.Entry<String, JTextField> entry : h2Fields.entrySet()) {
            String originalText = entry.getKey();
            String newText = entry.getValue().getText();
            newHtml = newHtml.replaceFirst(
                Pattern.quote("<h2>" + originalText + "</h2>"),
                "<h2>" + newText + "</h2>"
            );
        }

        // 2. summaryタグ（h3相当）の置換
        for (Map.Entry<String, JTextField> entry : h3Fields.entrySet()) {
            String originalText = entry.getKey();
            String newText = entry.getValue().getText();
            if (!originalText.startsWith("NEW_H3_KEY_")) {
                 // ★ summaryタグの置換 ★
                 newHtml = newHtml.replaceFirst(
                    Pattern.quote("<summary class=\"common_h3\">" + originalText + "</summary>"),
                    "<summary class=\"common_h3\">" + newText + "</summary>"
                );
            }
        }
        
        // 新規summaryタグ（h3相当）の追加
        StringBuilder newH3Blocks = new StringBuilder();
        for (Map.Entry<String, JTextField> entry : h3Fields.entrySet()) {
            if (entry.getKey().startsWith("NEW_H3_KEY_")) {
                newH3Blocks.append("\n    <div class=\"common_contents\">\n")
                           .append("      <details open>\n")
                           // ★ 新規追加するsummaryタグにも必ず common_h3 クラスを付与します ★
                           .append("        <summary class=\"common_h3\">").append(entry.getValue().getText()).append("</summary>\n")
                           .append("        <h4 class=\"common_section\">新規セクション名</h4>\n") 
                           .append("        <div class=\"contents_h4\">\n")
                           .append("        </div>\n")
                           .append("      </details>\n")
                           .append("    </div>");
            }
        }

        // 既存の最後の common_contents の直前に挿入 (footerの前)
        int bodyEnd = newHtml.indexOf("    <div class=\"common_footer\">");
        if (bodyEnd != -1 && newH3Blocks.length() > 0) {
            newHtml = newHtml.substring(0, bodyEnd) + newH3Blocks.toString() + "\n" + newHtml.substring(bodyEnd);
        }


        // 3. h4/liタグの置換・追加 (変更なし)
        String temporaryHtml = newHtml;
        
        for (Component comp : h4ContentPanel.getComponents()) {
            if (comp instanceof H4BlockEditor) {
                H4BlockEditor editor = (H4BlockEditor) comp;
                String oldBlock = editor.getOriginalBlock();
                
                if (oldBlock != null) {
                    String newBlock = editor.generateNewBlockHtml();
                    temporaryHtml = temporaryHtml.replace(oldBlock.trim(), newBlock.trim());
                }
            }
        }

        // 新規 h4 ブロックの追加
        StringBuilder newH4Blocks = new StringBuilder();
        for (Component comp : h4ContentPanel.getComponents()) {
            if (comp instanceof H4BlockEditor) {
                H4BlockEditor editor = (H4BlockEditor) comp;
                String oldBlock = editor.getOriginalBlock();

                if (oldBlock == null) {
                    String h4LiContent = editor.generateH4LiHtml(); 
                    
                    String newFullBlock = "\n    <div class=\"common_contents\">\n" +
                                        "      <details open>\n" +
                                        // ★ 新規追加するcommon_contentsブロックのsummaryタグにもクラスを付与 ★
                                        "        <summary class=\"common_h3\">新規追加セクション</summary>\n" +
                                        h4LiContent + 
                                        "      </details>\n" +
                                        "    </div>";
                    newH4Blocks.append(newFullBlock);
                }
            }
        }
        
        if (newH4Blocks.length() > 0) {
            int lastCommonContentsEnd = temporaryHtml.lastIndexOf("</div>", temporaryHtml.lastIndexOf("</div>", temporaryHtml.lastIndexOf("</div>") - 1) + 1);
            if (lastCommonContentsEnd != -1 && lastCommonContentsEnd < temporaryHtml.indexOf("    <div class=\"common_footer\">")) {
                 temporaryHtml = temporaryHtml.substring(0, lastCommonContentsEnd + 6) + newH4Blocks.toString() + temporaryHtml.substring(lastCommonContentsEnd + 6);
            }
        }

        newHtml = temporaryHtml;
        
        // ファイルへの書き出し (変更なし)
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("変更を保存するファイルを選択");
        fileChooser.setSelectedFile(new java.io.File("test_japan_index_edited.html"));
        
        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            Path fileToSave = fileChooser.getSelectedFile().toPath();
            try {
                Files.writeString(fileToSave, newHtml);
                JOptionPane.showMessageDialog(this, "HTMLファイルが保存されました:\n" + fileToSave.toString(), "成功", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "ファイルの保存中にエラーが発生しました: " + e.getMessage(), "エラー", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // --- H4とLIの編集ブロッククラス (変更なし) ---
    private static class H4BlockEditor extends JPanel {
        private final JTextField h4Field;
        private final JPanel liListPanel = new JPanel();
        private final String originalBlock;

        public H4BlockEditor(String h4Text, String blockHtml) {
            this.originalBlock = blockHtml;
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createTitledBorder("h4セクション名: " + h4Text + (blockHtml == null ? " (新規)" : "")));
            
            h4Field = new JTextField(h4Text, 30);
            
            JPanel h4Panel = new JPanel(new BorderLayout());
            h4Panel.add(new JLabel("h4テキスト変更: "), BorderLayout.WEST);
            h4Panel.add(h4Field, BorderLayout.CENTER);
            add(h4Panel, BorderLayout.NORTH);

            liListPanel.setLayout(new BoxLayout(liListPanel, BoxLayout.Y_AXIS));
            
            JScrollPane liScrollPane = new JScrollPane(liListPanel);
            liScrollPane.setPreferredSize(new Dimension(600, 150));
            liScrollPane.setBorder(BorderFactory.createEmptyBorder());

            add(liScrollPane, BorderLayout.CENTER); 

            JButton addLiButton = new JButton("リンク追加");
            addLiButton.addActionListener(e -> addLiField(new LiLinkData("", "", false)));
            
            JPanel buttonPanel = new JPanel();
            buttonPanel.add(addLiButton);
            add(buttonPanel, BorderLayout.SOUTH);

            if (blockHtml != null) {
                parseLi(blockHtml);
            }
        }
        
        private void parseLi(String blockHtml) {
            Pattern liPattern = Pattern.compile("<li><a href=\"(.*?)\">(.*?)</a></li>", Pattern.DOTALL);
            Matcher liMatcher = liPattern.matcher(blockHtml);

            while (liMatcher.find()) {
                String link = liMatcher.group(1);
                String text = liMatcher.group(2).trim();
                addLiField(new LiLinkData(link, text, true));
            }
        }

        private void addLiField(LiLinkData data) {
            LiLinkEditor editor = new LiLinkEditor(this, data, 30, 40);
            liListPanel.add(editor);
            liListPanel.revalidate();
            liListPanel.repaint();
        }
        
        public String getOriginalBlock() {
            return originalBlock;
        }
        
        public String generateH4LiHtml() {
            String newH4Text = h4Field.getText();
            StringBuilder liContent = new StringBuilder();
            
            for (Component comp : liListPanel.getComponents()) {
                if (comp instanceof LiLinkEditor) {
                    LiLinkEditor liEditor = (LiLinkEditor) comp;
                    if (liEditor.getLinkText().length() > 0 && liEditor.getLinkHref().length() > 0) {
                        liContent.append("          <li><a href=\"")
                                 .append(liEditor.getLinkHref())
                                 .append("\">")
                                 .append(liEditor.getLinkText())
                                 .append("</a></li>\n");
                    }
                }
            }
            
            boolean usesUl = originalBlock != null && originalBlock.contains("<ul class=\"pad_h5\">");
            String liWrapperStart = usesUl ? "          <ul class=\"pad_h5\">\n" : "";
            String liWrapperEnd = usesUl ? "          </ul>\n" : "";
            
            String h5Wrapper = "";
            if (originalBlock != null && originalBlock.contains("<h5 class=\"common_h5\">")) {
                int start = originalBlock.indexOf("<h5 class=\"common_h5\">");
                int end = originalBlock.indexOf("</div>", originalBlock.indexOf("<div class=\"contents_h5\">")) + 6;
                h5Wrapper = originalBlock.substring(start, end).trim() + "\n";
            }

            return String.format(
                "        <h4 class=\"common_section\">%s</h4>\n" +
                "        <div class=\"contents_h4\">\n" +
                "%s" + 
                "%s%s%s" + 
                "        </div>",
                newH4Text,
                h5Wrapper.trim(),
                liWrapperStart.trim(),
                liContent.toString(),
                liWrapperEnd.trim()
            );
        }

        public String generateNewBlockHtml() {
            if (originalBlock == null) return generateH4LiHtml(); 

            String h4LiHtml = generateH4LiHtml();
            
            int startOfH4 = originalBlock.indexOf("<h4 class=\"common_section\">");
            int endOfContentsH4 = originalBlock.indexOf("</div>", originalBlock.indexOf("<div class=\"contents_h4\">")) + 6;
            
            return originalBlock.substring(0, startOfH4) + 
                   h4LiHtml.trim() + 
                   "\n" + originalBlock.substring(endOfContentsH4).trim();
        }
    }

    // --- LIタグ（リンク）の編集ブロッククラス (変更なし) ---
    private static class LiLinkEditor extends JPanel {
        private final JTextField textField;
        private final JTextField linkField;

        public LiLinkEditor(H4BlockEditor parent, LiLinkData data, int textColumns, int linkColumns) {
            setLayout(new FlowLayout(FlowLayout.LEFT));
            setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
            setMaximumSize(new Dimension(Short.MAX_VALUE, 40));

            add(new JLabel("テキスト:"));
            textField = new JTextField(data.text, textColumns); 
            add(textField);

            add(new JLabel("リンク(href):"));
            linkField = new JTextField(data.href, linkColumns);
            add(linkField);

            JButton removeButton = new JButton("削除");
            removeButton.addActionListener(e -> {
                parent.liListPanel.remove(this);
                parent.liListPanel.revalidate();
                parent.liListPanel.repaint();
            });
            add(removeButton);
        }

        public String getLinkText() {
            return textField.getText().trim();
        }

        public String getLinkHref() {
            return linkField.getText().trim();
        }
    }

    // リンクデータ保持用クラス (変更なし)
    private static class LiLinkData {
        String href;
        String text;
        boolean existing;

        public LiLinkData(String href, String text, boolean existing) {
            this.href = href;
            this.text = text;
            this.existing = existing;
        }
    }

    // --- メインメソッド ---
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new HtmlEditorApp(ATTACHED_HTML).setVisible(true);
        });
    }
}