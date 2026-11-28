package Gui;

import manager.ProjectManager;
import manager.TodoManager;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;

/**
 * 날짜별 할 일 관리를 위한 Calendar 화면 클래스
 *
 * <p>왼쪽에 날짜 선택 달력, 오른쪽에 할 일 목록 배치</p>
 * <p>달력에서 날짜 선택 시 해당 날짜의 할 일 목록 갱신</p>
 */
public class CalendarPanel extends JPanel {
    private final DatePickerPanel datePicker;
    private final TodoPanel todoPanel;

    /**
     * Calendar 화면 생성 및 날짜 선택 이벤트 연동
     *
     * @param todoManager 할 일 관리 객체
     * @param projectManager 프로젝트 관리 객체
     */
    public CalendarPanel(TodoManager todoManager, ProjectManager projectManager) {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);


        // 왼쪽 전체 영역
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setPreferredSize(new Dimension(420, 750));
        leftPanel.setBackground(new Color(245, 245, 245));
        leftPanel.setBorder(
                BorderFactory.createEmptyBorder(10, 10, 0, 10)
        );

        // 달력을 왼쪽 상단에만 표시
        datePicker = new DatePickerPanel(LocalDate.now());
        datePicker.setPreferredSize(new Dimension(400, 320));

        leftPanel.add(datePicker, BorderLayout.NORTH);


        // 오른쪽 할 일 목록 영역
        todoPanel = new TodoPanel(todoManager, projectManager);

        // 날짜 선택 시 할 일 목록 갱신
        datePicker.setOnDateSelected(() -> {
            todoPanel.setCurrentDate(datePicker.getSelectedDate());
        });

        // 왼쪽 달력 영역과 오른쪽 Todo 영역 배치
        add(leftPanel, BorderLayout.WEST);
        add(todoPanel, BorderLayout.CENTER);
    }
}