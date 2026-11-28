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

        // 왼쪽 달력 영역
        datePicker = new DatePickerPanel(LocalDate.now());
        // 해당 날짜에 등록된 Todo 수를 달력에 전달
        datePicker.setTodoCountProvider(
                date -> todoManager.getTodo(date).size()
        );
        datePicker.setPreferredSize(new Dimension(420, 750));

        // 오른쪽 할 일 목록 영역
        todoPanel = new TodoPanel(todoManager, projectManager);

        // 날짜 선택 시 할 일 목록 갱신
        datePicker.setOnDateSelected(() -> {
            todoPanel.setCurrentDate(datePicker.getSelectedDate());
        });

        // 컴포넌트 배치
        add(datePicker, BorderLayout.WEST);
        add(todoPanel, BorderLayout.CENTER);
    }

    /**
     * Calendar 화면의 할 일 목록 갱신
     *
     * <p>현재 선택된 날짜 및 조회 조건을 유지하고
     * 변경된 할 일과 프로젝트 정보를 화면에 반영</p>
     */
    // Todo 목록과 달력 점 표시를 함께 갱신
    public void refreshCalendarPanel() {
        todoPanel.refreshTodoList();
        datePicker.refreshTodoDots();
    }

    // Todo 변경 시 달력의 점과 Project 화면 함께 갱신
    public void setOnDataChanged(Runnable onDataChanged) {
        todoPanel.setOnDataChanged(() -> {
            datePicker.refreshTodoDots();

            if (onDataChanged != null) {
                onDataChanged.run();
            }
        });
    }

}