package main;

import javax.swing.*;
import java.awt.*;

/**
 * GameModeSelector - диалоговое окно для выбора режима игры
 * Позволяет выбрать между игрой против человека или компьютера
 */
public class GameModeSelector extends JDialog {
    private int selectedMode = -1;  // -1: не выбран, 0: PvP, 1: PvAI
    private int selectedDifficulty = 3;  // 1-5, по умолчанию средний
    
    public GameModeSelector(JFrame parent) {
        super(parent, "Выбор режима игры", true);
        initializeUI();
    }
    
    private void initializeUI() {
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(getParent());
        setResizable(false);
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Заголовок
        JLabel titleLabel = new JLabel("Выберите режим игры:");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createVerticalStrut(20));
        
        // Кнопка PvP
        JButton pvpButton = new JButton("Человек vs Человек");
        pvpButton.setMaximumSize(new Dimension(300, 50));
        pvpButton.setFont(new Font("Arial", Font.PLAIN, 14));
        pvpButton.addActionListener(e -> {
            selectedMode = 0;
            dispose();
        });
        mainPanel.add(pvpButton);
        mainPanel.add(Box.createVerticalStrut(15));
        
        // Кнопка PvAI
        JButton pvaiButton = new JButton("Человек vs Компьютер");
        pvaiButton.setMaximumSize(new Dimension(300, 50));
        pvaiButton.setFont(new Font("Arial", Font.PLAIN, 14));
        pvaiButton.addActionListener(e -> {
            selectedMode = 1;
            showDifficultySelector();
        });
        mainPanel.add(pvaiButton);
        mainPanel.add(Box.createVerticalStrut(20));
        
        // Информационный текст
        JLabel infoLabel = new JLabel("<html>Выберите режим игры:<br>" +
                                     "• Человек vs Человек - локальная игра<br>" +
                                     "• Человек vs Компьютер - играйте против AI</html>");
        infoLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        infoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(infoLabel);
        
        add(mainPanel);
    }
    
    private void showDifficultySelector() {
        JDialog difficultyDialog = new JDialog(this, "Выбор сложности", true);
        difficultyDialog.setSize(350, 250);
        difficultyDialog.setLocationRelativeTo(this);
        difficultyDialog.setResizable(false);
        
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel label = new JLabel("Выберите уровень сложности:");
        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(label);
        panel.add(Box.createVerticalStrut(15));
        
        // Слайдер для выбора сложности
        JSlider difficultySlider = new JSlider(JSlider.HORIZONTAL, 1, 5, 3);
        difficultySlider.setMajorTickSpacing(1);
        difficultySlider.setPaintTicks(true);
        difficultySlider.setPaintLabels(true);
        difficultySlider.setFont(new Font("Arial", Font.PLAIN, 12));
        
        JLabel difficultyLabel = new JLabel("Средний (3)");
        difficultyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        difficultyLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        
        difficultySlider.addChangeListener(e -> {
            int value = difficultySlider.getValue();
            String[] levels = {"", "Очень легко", "Легко", "Средний", "Сложный", "Очень сложный"};
            difficultyLabel.setText(levels[value] + " (" + value + ")");
            selectedDifficulty = value;
        });
        
        panel.add(difficultySlider);
        panel.add(Box.createVerticalStrut(10));
        panel.add(difficultyLabel);
        panel.add(Box.createVerticalStrut(20));
        
        // Кнопка подтверждения
        JButton okButton = new JButton("Начать игру");
        okButton.setMaximumSize(new Dimension(200, 40));
        okButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        okButton.addActionListener(e -> {
            difficultyDialog.dispose();
            dispose();
        });
        panel.add(okButton);
        
        difficultyDialog.add(panel);
        difficultyDialog.setVisible(true);
    }
    
    public int getSelectedMode() {
        return selectedMode;
    }
    
    public int getSelectedDifficulty() {
        return selectedDifficulty;
    }
}
