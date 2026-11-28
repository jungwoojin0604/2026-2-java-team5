package Gui;

import manager.ProjectManager;
import manager.TodoManager;
import model.Theme;

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
    private final ThemeImagePanel themeImagePanel;
    private final JPanel leftPanel;

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
        datePicker.setPreferredSize(new Dimension(420, 350));

        // 오른쪽 할 일 목록 영역
        todoPanel = new TodoPanel(todoManager, projectManager);

        // 날짜 선택 시 할 일 목록 갱신
        datePicker.setOnDateSelected(() -> {
            todoPanel.setCurrentDate(datePicker.getSelectedDate());
        });

        // 테마 이미지 영역 생성
        themeImagePanel = new ThemeImagePanel();

        // 왼쪽 영역 생성
        leftPanel = new JPanel(new BorderLayout(0, 10));
        leftPanel.setBackground(Color.WHITE);
        leftPanel.setPreferredSize(new Dimension(420, 750));

        // 왼쪽 상단 달력 및 하단 이미지 배치
        leftPanel.add(datePicker, BorderLayout.NORTH);
        leftPanel.add(themeImagePanel, BorderLayout.CENTER);

        // 전체 컴포넌트 배치
        add(leftPanel, BorderLayout.WEST);
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

    /**
     * 달력 하단의 테마 이미지 변경
     *
     * @param imagePath 표시할 이미지 파일 경로
     * @return 이미지 설정 성공 여부
     */
    public boolean setThemeImage(String imagePath) {
        return themeImagePanel.setImage(imagePath);
    }

    /**
     * Calendar 화면에 지정된 테마 적용
     *
     * <p>Calendar 전체 배경과 왼쪽 영역의 배경색 변경 및
     * 달력 하단 테마 이미지 표시</p>
     *
     * @param theme 적용할 테마 객체
     */
    public void applyTheme(Theme theme) {
        // Calendar 전체 배경색
        setBackground(theme.getBackgroundColor());

        // 왼쪽 영역 배경색
        leftPanel.setBackground(theme.getBackgroundColor());

        // 달력 테마 적용
        datePicker.applyTheme(theme);

        // 할 일 목록 테마 적용
        todoPanel.applyTheme(theme);

        // 이미지 영역 배경색
        themeImagePanel.setBackground(theme.getBackgroundColor());

        // 테마 이미지 설정
        if (!setThemeImage(theme.getImagePath())) {
            setThemeImage(null);
        }
        revalidate();
        repaint();
    }
}