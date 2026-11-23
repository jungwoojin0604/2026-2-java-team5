
package Gui;

import model.Project;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class ProjectItemPanel extends JPanel {

    // 프로젝트 한 개의 이름, D-Day, 진행률을 표시
    public ProjectItemPanel(Project project, int completed, int total) {

        setLayout(new BorderLayout(10, 5));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 1. 프로젝트 이름 표시
        JLabel nameLabel = new JLabel(project.getName());

        // 2. 프로젝트 마감일까지 남은 날짜 계산
        String dDay = "미설정";

        if (project.getDeadLine() != null) {
            long days = ChronoUnit.DAYS.between(
                    LocalDate.now(),
                    project.getDeadLine().toLocalDate()
            );

            if (days > 0) {
                dDay = "D-" + days;
            } else if (days == 0) {
                dDay = "D-Day";
            } else {
                dDay = "D+" + (-days);
            }
        }

        JLabel dDayLabel = new JLabel(dDay);

        // 3. 상단에 프로젝트 이름과 D-Day 배치
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        topPanel.add(nameLabel, BorderLayout.WEST);
        topPanel.add(dDayLabel, BorderLayout.EAST);

        // 4. 완료된 할 일의 비율 계산
        int progress = total == 0 ? 0 : completed * 100 / total;

        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setValue(progress);
        progressBar.setStringPainted(true);

        // 5. 프로젝트 항목 배치
        add(topPanel, BorderLayout.NORTH);
        add(progressBar, BorderLayout.SOUTH);

        setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        setAlignmentX(Component.LEFT_ALIGNMENT);
    }
}

