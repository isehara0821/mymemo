import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class SummaryEditorApp extends JFrame {
    private Document htmlDoc;
    private File currentFile;
    private DefaultListModel<String> summaryListModel;
    private JList<String> summaryList;
    private Elements summaryElements;

    public SummaryEditorApp() {
        setTitle("Summary Editor");
        setSize(600, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // UI Components
        summaryListModel = new DefaultListModel<>();
        summaryList = new JList<>(summaryListModel);
        JScrollPane scrollPane = new JScrollPane(summaryList);

        JButton loadButton = new JButton("HTML読み込み");
        JButton saveButton = new JButton("保存");
        JButton addButton = new JButton("追加");
        
        JButton removeButton = new JButton("削除");

        // Layout
        JPanel topPanel = new JPanel();
        topPanel.add(loadButton);
        topPanel.add(saveButton);
        topPanel.add(addButton);
        topPanel.add(removeButton);

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // ボタンの動作
        loadButton.addActionListener(e -> loadHtmlFile());
        saveButton.addActionListener(e -> saveHtmlFile());
        addButton.addActionListener(e -> addSummary());
        removeButton.addActionListener(e -> removeSelectedSummary());
    }

    private void loadHtmlFile() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            currentFile = fileChooser.getSelectedFile();
            try {
                htmlDoc = Jsoup.parse(currentFile, "UTF-8");
                updateSummaryList();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "ファイル読み込みエラー: " + ex.getMessage());
            }
        }
    }

    private void updateSummaryList() {
        summaryListModel.clear();
        summaryElements = htmlDoc.select("summary.common_h3");
        for (Element el : summaryElements) {
            summaryListModel.addElement(el.text());
        }
    }

    private void addSummary() {
        String newText = JOptionPane.showInputDialog(this, "新しい summary のテキストを入力:");
        if (newText != null && !newText.trim().isEmpty()) {
            Element newSummary = htmlDoc.createElement("summary");
            newSummary.addClass("common_h3");
            newSummary.text(newText);

            // 追加場所：最後の details 要素内に追加（例として）
            Element lastDetails = htmlDoc.select("details").last();
            if (lastDetails != null) {
                lastDetails.appendChild(newSummary);
            } else {
                htmlDoc.body().appendChild(newSummary); // fallback
            }

            updateSummaryList();
        }
    }

    private void removeSelectedSummary() {
        int selectedIndex = summaryList.getSelectedIndex();
        if (selectedIndex >= 0 && selectedIndex < summaryElements.size()) {
            summaryElements.get(selectedIndex).remove();
            updateSummaryList();
        } else {
            JOptionPane.showMessageDialog(this, "削除する summary を選択してください。");
        }
    }

    private void saveHtmlFile() {
        if (currentFile != null && htmlDoc != null) {
            try (Writer writer = new OutputStreamWriter(new FileOutputStream(currentFile), StandardCharsets.UTF_8)) {
                writer.write(htmlDoc.outerHtml());
                JOptionPane.showMessageDialog(this, "保存しました。");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "保存エラー: " + ex.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SummaryEditorApp().setVisible(true));
    }
}
