package Gui;

import model.Project;
import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

// 왼쪽 목록에 프로젝트 한 개를 표시한다.
public class ProjectItemPanel extends JPanel {

    public ProjectItemPanel(Project project, int progress, boolean selected,
                            Runnable onSelect, Runnable onRename,
                            Runnable onDeadline, Runnable onDelete) {
        setLayout(new BorderLayout());
        setBackground(selected ? new Color(222, 237, 249) : Color.WHITE);
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

        JButton nameButton = new JButton(project.getName());
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
        info.add(new JLabel(dDay));

        JProgressBar bar = new JProgressBar(0, 100);
        bar.setValue(progress);
        bar.setPreferredSize(new Dimension(55, 12));
        info.add(bar);
        info.add(new JLabel(progress + "%"));

        // 프로젝트 수정·삭제 기능은 간단한 팝업 메뉴로 제공한다.
        JButton menuButton = new JButton("···");
        JPopupMenu menu = new JPopupMenu();
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
}
