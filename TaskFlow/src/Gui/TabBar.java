package Gui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.function.IntConsumer;

/**
 * 왼쪽 위 Calendar / Project 탭 버튼 (Figma panel1 / tabPanel, 420x50)
 *
 * <p>선택된 탭은 하늘색, 나머지는 회색. 누르면 화면 전환 콜백 실행</p>
 */
public class TabBar extends JPanel {
    /** Calendar 탭 번호 */
    public static final int CALENDAR = 0;
    /** Project 탭 번호 */
    public static final int PROJECT = 1;

    public TabBar(int selected, IntConsumer onSelect) {
        super(new GridLayout(1, 2, 0, 0));
        setPreferredSize(new Dimension(420, 50));
        setBackground(Theme.TAB_BAR_BG);

        String[] names = {"Calendar", "Project"};
        for (int i = 0; i < names.length; i++) {
            JButton tab = new JButton(names[i]);
            tab.setFont(Theme.bold(24));
            tab.setForeground(Color.BLACK);
            tab.setBackground(i == selected ? Theme.ACCENT : Theme.TAB_OFF);
            tab.setOpaque(true);
            tab.setFocusPainted(false);
            // 아래 선은 모두, 가운데 세로선은 왼쪽 탭에만
            tab.setBorder(BorderFactory.createMatteBorder(0, 0, 1, i == 0 ? 1 : 0, Color.BLACK));
            tab.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            int index = i;
            tab.addActionListener(e -> onSelect.accept(index));
            add(tab);
        }
    }
}
