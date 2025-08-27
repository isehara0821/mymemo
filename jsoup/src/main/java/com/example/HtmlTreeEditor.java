package com.example;

import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.List;

public class HtmlTreeEditor extends JFrame {
    private JTree tree;
    private DefaultTreeModel treeModel;
    private File htmlFile;
    private JTextField titleTextField;
    private JTextField h2TextField;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new HtmlTreeEditor("C:\\project\\history\\mymemo\\jsoup\\sample.html").setVisible(true);
        });
    }

    public HtmlTreeEditor(String htmlPath) {
        this.htmlFile = new File(htmlPath);
        setTitle("HTML Summary Editor");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 600);
        setLocationRelativeTo(null);

        DefaultMutableTreeNode rootNode = loadHtml(htmlPath);
        treeModel = new DefaultTreeModel(rootNode);
        tree = new JTree(treeModel);
        JScrollPane scrollPane = new JScrollPane(tree);
        add(scrollPane, BorderLayout.CENTER);

        setupPopupMenu();
        setupTopPanel();
    }

    // ==== データ読み込みとツリー構築 ====
    private DefaultMutableTreeNode loadHtml(String path) {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("HTML Structure");
        try {
            Document doc = Jsoup.parse(new File(path), "UTF-8");
            // titleTagの内容取得
            titleTextField = new JTextField(42);    
            Elements titleTag = doc.select("title");
            if (titleTag != null) {
                String titleText = titleTag.text();
                titleTextField.setText(titleText);
            }
            // h2Tagの内容取得
            h2TextField = new JTextField(42);
            Elements h2Tag = doc.select("h2");
            if (h2Tag != null){
                String h2Text = h2Tag.text();
                h2TextField.setText(h2Text);
            }
            // 繰り返し部分のTag内容取得
            Elements summaries = doc.select("summary.common_h3");
            for (Element summary : summaries) {
                DefaultMutableTreeNode summaryNode = new DefaultMutableTreeNode("summary: " + summary.text());
                Element details = summary.parent();
                Elements h4s = details.select("h4.common_section");
                for (Element h4 : h4s) {
                    DefaultMutableTreeNode h4Node = new DefaultMutableTreeNode("h4: " + h4.text());
                    Element ul = h4.nextElementSibling();
                    // if (ul != null && ul.tagName().equals("ul")) {
                    if (ul != null && ul.select("ul") != null )  {
                        for (Element li : ul.select("li")) {
                            Element a = li.selectFirst("a");
                            Element remarks = li.selectFirst("div.remarks");
                            
                            if (a != null) {
                                String text = a.text();
                                String href = a.attr("href");
                                String remarksText = (remarks != null) ? remarks.text() : "";  // nullチェックと代替
                            
                                String nodeLabel = String.format("Link: %s [%s] (%s)", text, href, remarksText);
                                DefaultMutableTreeNode aNode = new DefaultMutableTreeNode(nodeLabel);
                                h4Node.add(aNode);
                            }                            
                        }
                    }
                    summaryNode.add(h4Node);
                }
                root.add(summaryNode);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return root;
    }

    //Saveボタンと固定入力欄のGUI設定
    private void setupTopPanel(){
        // ツールバーとSaveボタン
        JToolBar toolbar = new JToolBar();
        JButton saveBtn = new JButton("Save");
        saveBtn.addActionListener(e -> saveToHtml());
        toolbar.add(saveBtn);

        JPanel fixedTagPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        JLabel titltLabel = new JLabel("title");
        JLabel h2Label = new JLabel("h2");

        // 左上のtilte label
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(5, 5, 5, 5); // 外側の余白
        gbc.anchor = GridBagConstraints.WEST;
        fixedTagPanel.add(titltLabel, gbc);       
        // 右上のtilte Text
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.insets = new Insets(5, 5, 5, 5); // 外側の余白
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        fixedTagPanel.add(titleTextField, gbc);       
        // 左下のh2 label
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.insets = new Insets(5, 5, 5, 5); // 外側の余白
        gbc.anchor = GridBagConstraints.WEST;
        fixedTagPanel.add(h2Label, gbc);       
        // 右下のh2 Text
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.insets = new Insets(5, 5, 5, 5); // 外側の余白
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        fixedTagPanel.add(h2TextField, gbc);       

        // ③ ツールバーとタグパネルを縦に並べるパネルを用意し、それをフレームに追加
        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BorderLayout());
        topContainer.add(toolbar, BorderLayout.NORTH);
        topContainer.add(fixedTagPanel, BorderLayout.CENTER);
        add(topContainer, BorderLayout.NORTH);
    }

    // ==== 右クリックメニュー ====
    private void setupPopupMenu() {
        JPopupMenu popup = new JPopupMenu();
        JMenuItem addItem = new JMenuItem("Add");
        JMenuItem editItem = new JMenuItem("Edit");
        JMenuItem deleteItem = new JMenuItem("Delete");
        JMenuItem moveUpItem = new JMenuItem("Move Up");
        JMenuItem moveDownItem = new JMenuItem("Move Down");

        popup.add(addItem);
        popup.add(editItem);
        popup.add(deleteItem);
        popup.addSeparator();
        popup.add(moveUpItem);
        popup.add(moveDownItem);

        tree.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    int row = tree.getClosestRowForLocation(e.getX(), e.getY());
                    tree.setSelectionRow(row);
                    popup.show(tree, e.getX(), e.getY());
                }
            }
        });

        addItem.addActionListener(e -> addNode());
        editItem.addActionListener(e -> editNode());
        deleteItem.addActionListener(e -> deleteNode());
        moveUpItem.addActionListener(e -> moveNode(-1));
        moveDownItem.addActionListener(e -> moveNode(1));
    }

    // ==== ノード操作 ====
    private void addNode() {
        TreePath path = tree.getSelectionPath();
        if (path == null) return;

        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) path.getLastPathComponent();
        if (selectedNode == null) return;
        String type = getNodeType(selectedNode);

        DefaultMutableTreeNode newNode = null;
        if (type.equals("root")) { 
            String input = JOptionPane.showInputDialog(this, "Enter text:");
            if (input == null || input.isEmpty()) return;
            newNode = new DefaultMutableTreeNode("summary: " + input); 
        } else if (type.equals("summary")) {
            String input = JOptionPane.showInputDialog(this, "Enter text:");
            if (input == null || input.isEmpty()) return;
            newNode = new DefaultMutableTreeNode("h4: " + input);
        } else if (type.equals("h4")) {
            JPanel panel = new JPanel(new GridLayout(3, 2));
            JTextField textField = new JTextField();
            JTextField linkField = new JTextField();
            JTextField remarksField = new JTextField();
            panel.add(new JLabel("text:")); panel.add(textField);
            panel.add(new JLabel("link:")); panel.add(linkField);
            panel.add(new JLabel("remarks:")); panel.add(remarksField);
        
            int result = JOptionPane.showConfirmDialog(this, panel, "Add Link", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                String text = textField.getText().trim();
                String link = linkField.getText().trim();
                String remarks = remarksField.getText().trim();
                String nodeLabel = String.format("%s [%s] (%s)", text, link, remarks);
                newNode = new DefaultMutableTreeNode(nodeLabel);
            }
        }
        if (newNode != null) {
            selectedNode.add(newNode);
            treeModel.reload(selectedNode);
        }
    }

    private void editNode() {
        TreePath path = tree.getSelectionPath();
        if (path == null) return;
    
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
        String current = node.getUserObject().toString();
    
        if (current.startsWith("Link: ")) {
            // Linkノードの編集処理
            String content = current.substring("Link: ".length());
            String text = "", link = "", remarks = "";
            try {
                // "text (link) [remarks]" のパターン解析
                int linkStart = content.indexOf(" ()");
                int linkEnd = content.indexOf(")", linkStart);
                int remarksStart = content.indexOf("[", linkEnd);
                int remarksEnd = content.indexOf("]", remarksStart);
    
                if (linkStart != -1 && linkEnd != -1) {
                    text = content.substring(0, linkStart).trim();
                    link = content.substring(linkStart + 2, linkEnd).trim();
                }
                if (remarksStart != -1 && remarksEnd != -1) {
                    remarks = content.substring(remarksStart + 1, remarksEnd).trim();
                }
            } catch (Exception e) {
                // フォーマットが崩れている場合は無視して空欄扱い
            }
    
            JPanel panel = new JPanel(new GridLayout(3, 2));
            JTextField textField = new JTextField(text);
            JTextField linkField = new JTextField(link);
            JTextField remarksField = new JTextField(remarks);
            panel.add(new JLabel("text:")); panel.add(textField);
            panel.add(new JLabel("link:")); panel.add(linkField);
            panel.add(new JLabel("remarks:")); panel.add(remarksField);
    
            int result = JOptionPane.showConfirmDialog(this, panel, "Edit Link", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                String newText = textField.getText().trim();
                String newLink = linkField.getText().trim();
                String newRemarks = remarksField.getText().trim();
                String nodeLabel = String.format("Link: %s [%s] (%s)", newText, newLink, newRemarks);
                node.setUserObject(nodeLabel);
                treeModel.nodeChanged(node);
            }
    
        } else {
            // Summary, H4 はテキストだけを編集
            String input = JOptionPane.showInputDialog(this, "Edit text:", current);
            if (input != null && !input.isEmpty()) {
                node.setUserObject(input);
                treeModel.nodeChanged(node);
            }
        }
    }
    

    private void deleteNode() {
        TreePath path = tree.getSelectionPath();
        if (path == null) return;

        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
        if (node.getParent() != null) {
            treeModel.removeNodeFromParent(node);
        }
    }

    private void moveNode(int direction) {
        TreePath path = tree.getSelectionPath();
        if (path == null) return;

        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
        MutableTreeNode parent = (MutableTreeNode) node.getParent();
        if (parent == null) return;

        int index = parent.getIndex(node);
        int newIndex = index + direction;
        if (newIndex < 0 || newIndex >= parent.getChildCount()) return;

        treeModel.removeNodeFromParent(node);
        treeModel.insertNodeInto(node, parent, newIndex);
        tree.scrollPathToVisible(new TreePath(node.getPath()));
    }

    private String getNodeType(DefaultMutableTreeNode node) {
        if (node.isRoot()) return "root";
        String val = node.getUserObject().toString();
        if (val.startsWith("summary:")) return "summary";
        if (val.startsWith("h4:")) return "h4";
        if (val.startsWith("link:")) return "a";
        return "";
    }

    // ==== HTMLへの保存 ====
    private void saveToHtml() {
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) treeModel.getRoot();
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>\n<html lang=\"en\">\n");
        // headタグ
        sb.append(" <head>\n");
        sb.append("  <meta charset=\"utf-8\">\n");
        sb.append("  <meta name=\"viewport\" content=\"width=device-width,initial-scale=1.0,user-scalable=yes\">\n");
        sb.append("  <link rel=\"stylesheet\" href=\"../../css/common.css\">\n");
        sb.append("  <link rel=\"stylesheet\" href=\"../../css/table.css\">\n");
        sb.append("  <link rel=\"icon\" type=\"image/png\" href=\"../../img/pochama.png\">\n");
        sb.append("  <title>");
        sb.append(titleTextField.getText());
        sb.append("</title>\n");

        sb.append(" </head>\n");

        // bodyタグ
        sb.append(" <body>\n");
        sb.append("  <div class=\"common_top\">\n");
        sb.append("   <h2>");
        sb.append(h2TextField.getText());
        sb.append("</h2>\n");
        sb.append("  </div>\n");
        Enumeration<TreeNode> summaries = root.children();
        while (summaries.hasMoreElements()) {
            DefaultMutableTreeNode summaryNode = (DefaultMutableTreeNode) summaries.nextElement();
            String summaryText = summaryNode.getUserObject().toString().replace("summary: ", "");

            sb.append("  <div class=\"common_contents\">\n");
            sb.append("   <details open>\n");
            sb.append("    <summary class=\"common_h3\">").append(summaryText).append("</summary>\n");

            Enumeration<TreeNode> h4s = summaryNode.children();
            while (h4s.hasMoreElements()) {
                DefaultMutableTreeNode h4Node = (DefaultMutableTreeNode) h4s.nextElement();
                String h4Text = h4Node.getUserObject().toString().replace("h4: ", "");

                sb.append("    <h4 class=\"common_section\">").append(h4Text).append("</h4>\n");
                sb.append("    <div class=\"contents_h4\">\n");
                sb.append("     <ul class=\"pad_h5\">\n");

                Enumeration<TreeNode> links = h4Node.children();
                while (links.hasMoreElements()) {
                    DefaultMutableTreeNode aNode = (DefaultMutableTreeNode) links.nextElement();
                    String aText = aNode.getUserObject().toString().replace("Link: ", "");
                    String[] aParts = aText.split(" \\[");
                    String text = aParts[0];
                    String linkRemarks = aParts[1];
                    String[] linkParts = linkRemarks.split(" \\(");
                    String href = linkParts.length > 1 ? linkParts[0].replace("]", "") : "#";
                    String remarks = linkParts.length > 1 ? linkParts[1].replace(")", "") : "#";
                    sb.append("      <li>\n");
                    sb.append("       <a href=\"").append(href).append("\">").append(text).append("</a>\n");
                    sb.append("       <iframe class=\"iframe-preview\" src=\"").append(href).append("#prev\">\n");
                    sb.append("       <div class=\"remarks\">").append(remarks).append("</div>)\n");
                }
                sb.append("      </li>\n     </ul>\n");
            }

            sb.append("   </details>\n  </div>\n");
        }

        sb.append("  <div class=\"common_footer\">\n");
        sb.append("   <p class=\"link\"><a href=\"../../index.html\">TOPへ戻る</a></p>\n");
        sb.append("  </div>\n");
        sb.append(" </body>\n");
        sb.append("</html>\n");

        try {
            Files.writeString(htmlFile.toPath(), sb.toString());
            JOptionPane.showMessageDialog(this, "Saved to " + htmlFile.getName());
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Save failed.");
        }
    }
}
