package main;

import javax.swing.*;
import java.awt.*;

/**
 * GameModeSelector - диалоговое окно для выбора режима игры
 * Теперь поддерживает только PvP режим (Человек vs Человек)
 */
public class GameModeSelector extends JDialog {
    private int selectedMode = 0;  // Только PvP режим (0)
    
    public GameModeSelector(JFrame parent) {
        super(parent, "Добро пожаловать в шахматы", true);
        initializeUI();
    }
    
    private void initializeUI() {
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(400, 250);
        setLocationRelativeTo(getParent());
        setResizable(false);
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Заголовок
        JLabel titleLabel = new JLabel("Добро пожаловать в шахматы!");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createVerticalStrut(30));
        
        // Описание режима
        JLabel descriptionLabel = new JLabel("Игра поддерживает режим:");
        descriptionLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        descriptionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(descriptionLabel);
        mainPanel.add(Box.createVerticalStrut(15));
        
        // Кнопка PvP
        JButton pvpButton = new JButton("Человек vs Человек");
        pvpButton.setMaximumSize(new Dimension(300, 60));
        pvpButton.setFont(new Font("Arial", Font.BOLD, 16));
        pvpButton.addActionListener(e -> {
            selectedMode = 0;  // PvP режим
            dispose();
        });
        mainPanel.add(pvpButton);
        mainPanel.add(Box.createVerticalStrut(30));
        
        // Информационный текст
        JLabel infoLabel = new JLabel("<html><center>Локальная игра для двух игроков<br>" +
                                     "на одном компьютере</center></html>");
        infoLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        infoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(infoLabel);
        
        add(mainPanel);
    }
    
    public int getSelectedMode() {
        return selectedMode;  // Всегда возвращает 0 (PvP)
    }
    
    public int getSelectedDifficulty() {
        return 0;  // Не используется, но возвращаем 0 для совместимости
    }
}
