package github.calabchen;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.*;
import java.nio.file.Paths;

/**
 * @author calabchen
 * @since 2025/6/9
 */
public class GUI extends JFrame {

    private JPanel mainPanel;
    private JTextArea tmpsWindow;
    private JButton pcodeButton;
    private JButton symbolButton;
    private JButton analysisButton;
    private JTextArea outputWindow;
    private JButton loadFileButton;
    private JButton saveFileButton;
    private JTextField inputFileNameField;
    private JTextArea codeWindow;
    private JButton compileButton;
    private JCheckBox listObjectCodeCheckBox;
    private JCheckBox listSymbolTableCheckBox;
    private JButton runButton;
    private JPanel rightPanel;
    private JPanel rightTopPanel;
    private JPanel leftPanel;
    private JPanel leftTopPanel;
    private JPanel codeRunnerPanel;
    private JScrollPane codeScrollPanel;
    private JScrollPane tmpsScrollPanel;

    public GUI() {
        L25.projectRoot = System.getProperty("user.dir");
        L25.testDir = Paths.get(L25.projectRoot, "l25testcode").toString();

        $$$setupUI$$$(); // 自动从 .form 文件加载 UI 布局
        this.setContentPane(mainPanel);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(800, 600);
        this.setVisible(true);
        // 添加事件监听器
        addEventListeners();
    }

    private void $$$setupUI$$$() {
    }

    private void addEventListeners() {
        // 加载文件按钮事件监听器
        loadFileButton.addActionListener(e -> {
            String fileName = inputFileNameField.getText();
            if (fileName.isEmpty()) {
                outputWindow.append("Please enter a valid file name.\n");
                return;
            }
            try {
                BufferedReader reader = new BufferedReader(new FileReader(Paths.get(L25.testDir, fileName).toFile()));
                codeWindow.read(reader, null);
                reader.close();
                outputWindow.append("Loaded file: " + fileName + "\n");
            } catch (Exception ex) {
                outputWindow.append("Error loading file: " + ex.getMessage() + "\n");
            }
        });

        // 保存文件按钮事件监听器 (New Implementation)
        saveFileButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser(L25.testDir); // Start in the test directory
            fileChooser.setDialogTitle("Save L25 Source File");
            // Optionally set file filters
            fileChooser.setFileFilter(new FileNameExtensionFilter("L25 Source Files (*.l25)", "l25"));
            fileChooser.setFileFilter(new FileNameExtensionFilter("Text Files (*.txt)", "txt"));


            // Pre-fill the file name if something is in inputFileNameField
            String currentFileName = inputFileNameField.getText();
            if (!currentFileName.isEmpty()) {
                fileChooser.setSelectedFile(new File(L25.testDir, currentFileName));
            }

            int userSelection = fileChooser.showSaveDialog(this); // Show save dialog

            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();

                // Add .l25 extension if not already present and L25 filter is active
                String filePath = fileToSave.getAbsolutePath();
                if (fileChooser.getFileFilter() instanceof FileNameExtensionFilter) {
                    FileNameExtensionFilter filter = (FileNameExtensionFilter) fileChooser.getFileFilter();
                    String[] extensions = filter.getExtensions();
                    boolean hasExtension = false;
                    for (String ext : extensions) {
                        if (filePath.toLowerCase().endsWith("." + ext.toLowerCase())) {
                            hasExtension = true;
                            break;
                        }
                    }
                    if (!hasExtension && extensions.length > 0) {
                        // Just append the first extension from the filter
                        fileToSave = new File(filePath + "." + extensions[0]);
                    }
                }


                inputFileNameField.setText(fileToSave.getName()); // Update the text field with the chosen file name
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileToSave))) {
                    codeWindow.write(writer); // Write content of JTextArea to file
                    outputWindow.append("Saved file: " + fileToSave.getName() + "\n");
                } catch (Exception ex) {
                    outputWindow.append("Error saving file: " + ex.getMessage() + "\n");
                    ex.printStackTrace(); // For debugging
                }
            }
        });


        // 编译按钮事件监听器
        compileButton.addActionListener(e -> {
            String fileName = inputFileNameField.getText();
            if (fileName.isEmpty()) {
                outputWindow.append("Please enter a file name first.\n");
                return;
            }

            try {
                BufferedReader fin = new BufferedReader(new FileReader(Paths.get(L25.testDir, fileName).toFile()));
                L25.fa1 = new PrintStream(Paths.get(L25.testDir, "fa1.tmp").toString());
                L25 l25 = new L25(fin);

                // 设置是否输出 object code 和 symbol table
                L25.listswitch = listObjectCodeCheckBox.isSelected();
                L25.tableswitch = listSymbolTableCheckBox.isSelected();

                boolean success = l25.compile();
                if (success) {
                    outputWindow.append("Compilation successful.\n");
                } else {
                    outputWindow.append("Compilation failed.\n");
                }
            } catch (Exception ex) {
                outputWindow.append("Error during compilation: " + ex.getMessage() + "\n");
            }
        });

        // 运行按钮事件监听器
        runButton.addActionListener(e -> {
            try {
                L25.fa2 = new PrintStream(Paths.get(L25.testDir, "fa2.tmp").toString());
                if (L25.interp != null) {
                    L25.interp.interpret();
                    L25.fa2.close();
                    outputWindow.append("Program executed.\n");
                } else {
                    outputWindow.append("Error: Interpreter not initialized. Please compile first.\n");
                }
            } catch (Exception ex) {
                outputWindow.append("Error: " + ex.getMessage() + "\n");
            }
        });

        // 加载对象代码按钮事件监听器
        pcodeButton.addActionListener(e -> {
            tmpsWindow.setText(""); // 清空之前的显示
            if (listObjectCodeCheckBox.isSelected()) {
                try {
                    BufferedReader reader = new BufferedReader(new FileReader(Paths.get(L25.testDir, "fa.tmp").toFile()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        tmpsWindow.append(line + "\n");
                    }
                    reader.close();
                } catch (Exception ex) {
                    outputWindow.append("Error reading fa.tmp: " + ex.getMessage() + "\n");
                }
            } else {
                outputWindow.append("Object code listing was not enabled during compilation.\n");
            }
        });

        // 加载符号表按钮事件监听器
        symbolButton.addActionListener(e -> {
            tmpsWindow.setText("");
            if (listSymbolTableCheckBox.isSelected()) {
                try {
                    BufferedReader reader = new BufferedReader(new FileReader(Paths.get(L25.testDir, "fas.tmp").toFile()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        tmpsWindow.append(line + "\n");
                    }
                    reader.close();
                } catch (Exception ex) {
                    outputWindow.append("Error reading fas.tmp: " + ex.getMessage() + "\n");
                }
            } else {
                outputWindow.append("Symbol table listing was not enabled during compilation.\n");
            }
        });

        // 加载编译分析按钮事件监听器
        analysisButton.addActionListener(e -> {
            tmpsWindow.setText("");
            try {
                BufferedReader reader = new BufferedReader(new FileReader(Paths.get(L25.testDir, "fa1.tmp").toFile()));
                String line;
                while ((line = reader.readLine()) != null) {
                    tmpsWindow.append(line + "\n");
                }
                reader.close();
            } catch (Exception ex) {
                outputWindow.append("Error reading fa1.tmp: " + ex.getMessage() + "\n");
            }
        });
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(GUI::new);
    }
}
