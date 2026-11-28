package Gui;

import model.Project;
import model.Theme;
import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

// 왼쪽 목록에 프로젝트 한 개를 표시한다.
public class ProjectItemPanel extends JPanel {
    // 프로젝트 선택 상태
    private final boolean selected;
    // 프로젝트 표시 컴포넌트
    private final JButton nameButton;
    private final JLabel dDayLabel;
    private final JLabel progressLabel;
    private final JProgressBar bar;
    private final JButton menuButton;
    private final JPopupMenu menu;
    public ProjectItemPanel(Project project, int progress, boolean selected,
                            Runnable onSelect, Runnable onRename,
                            Runnable onDeadline, Runnable onDelete) {
        this.selected = selected;
        setLayout(new BorderLayout());
        setBackground(selected ? new Color(222, 237, 249) : Color.WHITE);
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

        nameButton = new JButton(project.getName());
        nameButton.setBorderPainted(false);
        nameButton.setContentAreaFilled(false);
        nameButton.addActionListener(e -> onSelect.run());
        add(nameButton, BorderLayout.CENTER);

        JPanel info = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 6));
        info.setOpaque(false);

        String dDay = "미설정";
        if (project.getDeadLine() != null) {
            long days = ChronoUnit.DAYS.between(
                    LocalDate.now(), project.getDeadLine().toLocalDate());
            dDay = days > 0 ? "D-" + days : days == 0 ? "D-Day" : "D+" + (-days);
        }
        dDayLabel = new JLabel(dDay);
        info.add(dDayLabel);

        bar = new JProgressBar(0, 100);
        bar.setValue(progress);
        bar.setOpaque(true);
        bar.setPreferredSize(new Dimension(55, 12));
        info.add(bar);
        progressLabel = new JLabel(progress + "%");
        info.add(progressLabel);

        // 프로젝트 수정·삭제 기능은 간단한 팝업 메뉴로 제공한다.
        menuButton = new JButton("···");
        menu = new JPopupMenu();
        JMenuItem rename = new JMenuItem("이름 수정");
        JMenuItem deadline = new JMenuItem("마감일 수정");
        JMenuItem delete = new JMenuItem("삭제");
        rename.addActionListener(e -> onRename.run());
        deadline.addActionListener(e -> onDeadline.run());
        delete.addActionListener(e -> onDelete.run());
        menu.add(rename);
        menu.add(deadline);
        menu.add(delete);
        menuButton.addActionListener(e -> menu.show(menuButton, 0, menuButton.getHeight()));
        info.add(menuButton);
        add(info, BorderLayout.EAST);
    }

    /**
     * 개별 프로젝트 항목에 지정된 테마 적용
     *
     * <p>선택 상태에 따른 배경색과 프로젝트 이름,
     * D-Day, 진행률 및 메뉴의 색상과 폰트 변경</p>
     *
     * @param theme 적용할 테마 객체
     */
    public void applyTheme(Theme theme) {
        Color panelColor = theme.getPanelColor();
        Color accentColor = theme.getAccentColor();
        // 선택된 프로젝트의 배경색 생성
        Color selectedColor = new Color(
                (panelColor.getRed() * 7 + accentColor.getRed() * 3) / 10,
                (panelColor.getGreen() * 7 + accentColor.getGreen() * 3) / 10,
                (panelColor.getBlue() * 7 + accentColor.getBlue() * 3) / 10
        );
        // 선택 여부에 따른 배경색 적용
        setBackground(selected ? selectedColor : panelColor);
        // 프로젝트 이름
        nameButton.setForeground(theme.getTextColor());
        nameButton.setFont(theme.getFont());
        // D-Day
        dDayLabel.setForeground(theme.getSecondaryTextColor());
        dDayLabel.setFont(theme.getFont());
        // 진행률 표시
        bar.setForeground(theme.getAccentColor());
        bar.setBackground(theme.getBackgroundColor());
        progressLabel.setForeground(theme.getTextColor());
        progressLabel.setFont(theme.getFont());
        // 메뉴 버튼
        menuButton.setBackground(theme.getButtonColor());
        menuButton.setForeground(theme.getTextColor());
        menuButton.setFont(theme.getFont());
        // 팝업 메뉴
        menu.setBackground(theme.getPanelColor());
        for (Component component : menu.getComponents()) {
            if (component instanceof JMenuItem item) {
                item.setBackground(theme.getPanelColor());
                item.setForeground(theme.getTextColor());
                item.setFont(theme.getFont());
                item.setOpaque(true);
            }
        }
        revalidate();
        repaint();
    }
}
