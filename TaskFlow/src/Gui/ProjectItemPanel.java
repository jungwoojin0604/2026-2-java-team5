
package Gui;

import model.Project;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import model.Todo;
import java.util.List;
import java.util.ArrayList;
public class ProjectItemPanel extends JPanel {
    // 프로젝트에 연결된 Todo와 체크박스를 보관
    private final List<Todo> todos;
    private final List<JCheckBox> todoCheckBoxes = new ArrayList<>();

    // 진행률을 나중에도 수정할 수 있도록 필드로 선언
    private final JProgressBar progressBar;
    // 프로젝트 한 개의 이름, D-Day, 진행률을 표시
    // 프로젝트 선택 시 실행할 동작을 전달받음
    // 프로젝트 정보와 연결된 Todo 목록을 전달받아 표시
    // Todo 상태가 변경되었을 때 실행할 작업도 전달받음
    public ProjectItemPanel(Project project, int completed,
                            int total, List<Todo> todos,
                            Runnable onSelected,
                            Runnable onTodoChanged) {
        // 전달받은 Todo 목록 저장
        this.todos = todos;
        setLayout(new BorderLayout(10, 5));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 1. 프로젝트 이름 표시
        // 프로젝트 이름을 클릭할 수 있는 버튼으로 생성
        JButton nameButton = new JButton(project.getName());


        // 일반 텍스트처럼 보이도록 버튼 디자인 설정
        nameButton.setBorderPainted(false);
        nameButton.setContentAreaFilled(false);
        nameButton.setFocusPainted(false);
        nameButton.setMargin(new Insets(0, 0, 0, 0));

        // 프로젝트 이름 클릭 시 전달받은 동작 실행
        nameButton.addActionListener(e -> onSelected.run());

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

        // 프로젝트 펼치기 버튼
        JButton expandButton = new JButton("▶");

// 펼치기 버튼과 프로젝트 이름을 가로로 배치
        JPanel namePanel = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 5, 0)
        );
        namePanel.setOpaque(false);

        namePanel.add(expandButton);
        namePanel.add(nameButton);

        topPanel.add(namePanel, BorderLayout.WEST);
        topPanel.add(dDayLabel, BorderLayout.EAST);

        // 4. 완료된 할 일의 비율 계산
        int progress = total == 0 ? 0 : completed * 100 / total;

        progressBar = new JProgressBar(0, 100);
        progressBar.setValue(progress);
        progressBar.setStringPainted(true);


        // 프로젝트 이름과 진행률을 하나로 묶기
        JPanel summaryPanel = new JPanel(new BorderLayout(0, 5));
        summaryPanel.setOpaque(false);

        summaryPanel.add(topPanel, BorderLayout.NORTH);
        summaryPanel.add(progressBar, BorderLayout.SOUTH);

        add(summaryPanel, BorderLayout.NORTH);

        // 프로젝트에 연결된 Todo 목록
        JPanel todoListPanel = new JPanel();
        todoListPanel.setLayout(
                new BoxLayout(todoListPanel, BoxLayout.Y_AXIS)
        );
        todoListPanel.setOpaque(false);

        if (todos.isEmpty()) {
            todoListPanel.add(
                    new JLabel("연결된 할 일이 없습니다.")
            );
        } else {


            for (Todo todo : todos) {

                // 현재 완료 상태에 맞는 체크박스 생성
                JCheckBox checkBox = new JCheckBox(
                        todo.getContent(),
                        todo.isDone()
                );

                // 완료 여부에 따라 취소선 표시
                checkBox.setText(formatTodoText(todo));
                checkBox.setOpaque(false);
                checkBox.setAlignmentX(Component.LEFT_ALIGNMENT);

                // Todo와 연결된 체크박스 저장
                todoCheckBoxes.add(checkBox);

                // 체크박스를 클릭하면 Todo 상태 변경
                checkBox.addActionListener(e -> {

                    todo.setDone(checkBox.isSelected());

                    // 모든 프로젝트의 진행률과 체크박스 갱신 요청
                    onTodoChanged.run();
                });

                todoListPanel.add(checkBox);
                todoListPanel.add(Box.createVerticalStrut(5));
            }


        }

        // 처음에는 Todo 목록을 숨김
        todoListPanel.setVisible(false);
        add(todoListPanel, BorderLayout.CENTER);

        setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        setAlignmentX(Component.LEFT_ALIGNMENT);

        // 펼치기 버튼을 클릭하면 Todo 목록 표시 상태 변경
        expandButton.addActionListener(e -> {

            boolean expanded = !todoListPanel.isVisible();

            todoListPanel.setVisible(expanded);
            expandButton.setText(expanded ? "▼" : "▶");

            // 펼쳤을 때 Todo 목록만큼 패널 높이 증가
            int height = expanded
                    ? 80 + Math.max(1, todos.size()) * 28
                    : 80;

            setMaximumSize(
                    new Dimension(Integer.MAX_VALUE, height)
            );

            revalidate();
            repaint();
        });

    }

    // Todo의 완료 상태에 따라 취소선이 있는 문자열 반환
    private static String formatTodoText(Todo todo) {

        // HTML 특수문자가 글자 그대로 표시되도록 처리
        String content = todo.getContent()
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");

        if (todo.isDone()) {
            return "<html><strike>" + content + "</strike></html>";
        }

        return content;

    }
        // Todo 완료 상태에 맞춰 체크박스와 진행률을 갱신
        public void refreshTodoDisplay() {

            int completed = 0;

            // 프로젝트에 연결된 모든 Todo 확인
            for (int i = 0; i < todos.size(); i++) {

                Todo todo = todos.get(i);
                JCheckBox checkBox = todoCheckBoxes.get(i);

                // 실제 Todo 완료 상태를 체크박스에 반영
                checkBox.setSelected(todo.isDone());

                // 완료 상태에 따라 취소선 변경
                checkBox.setText(formatTodoText(todo));

                if (todo.isDone()) {
                    completed++;
                }
            }

            // 프로젝트 진행률 다시 계산
            int progress = todos.isEmpty()
                    ? 0
                    : completed * 100 / todos.size();

            progressBar.setValue(progress);
        }
}

