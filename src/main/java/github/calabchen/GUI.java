package github.calabchen;

import javax.swing.*;

/**
 * @author calabchen
 * @since 2025/6/9
 */
public class GUI extends JFrame {

    private JPanel MainWindow;
    private JTextField textField1;
    private JComboBox comboBox1;
    private JCheckBox checkBox1;

    public static void main(String[] args) {
        JFrame jFrame = new JFrame("L25编译器");
        GUI gui = new GUI();
        jFrame.setVisible(true);
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jFrame.setSize(800,800);
    }
}
