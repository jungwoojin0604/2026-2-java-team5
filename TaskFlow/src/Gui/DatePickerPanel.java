package Gui;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.YearMonth;

/**
 * 날짜 선택 기능을 제공하는 달력 패널 클래스
 *
 * <p>연도와 월을 기준으로 달력을 표시하고
 * 이전 달 및 다음 달 이동과 날짜 선택 기능 제공</p>
 *
 * <p>날짜 선택 시 콜백을 실행하여 외부 화면과 연동할 수 있고
 * 날짜 선택 영역의 활성화 및 비활성화 가능</p>
 */
public class DatePickerPanel extends JPanel {
    // 실제 선택된 날짜
    private LocalDate selectedDate;
    // 현재 달력에 표시되는 연도 및 월
    private YearMonth currentMonth;
    private JLabel monthLabel;
    private JPanel calendarPanel;
    // 날짜 선택 시 실행할 콜백
    private Runnable onDateSelected = () -> {};

    /**
     * 기본 날짜를 기준으로 날짜 선택 패널 생성
     *
     * <p>기본 날짜에 해당하는 달력 표시 및
     * 월 이동 버튼과 날짜 선택 이벤트 설정</p>
     *
     * @param defaultDate 기본 선택 날짜 (null이면 오늘 날짜 사용)
     */
    public DatePickerPanel(LocalDate defaultDate) {
        selectedDate = defaultDate == null ? LocalDate.now() : defaultDate;
        currentMonth = YearMonth.from(selectedDate);
        setLayout(new BorderLayout(5, 5));
        setBackground(Color.WHITE);

        // 상단 연월 이동 영역
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);

        JButton prevButton = new JButton("<");
        JButton nextButton = new JButton(">");
        monthLabel = new JLabel("", SwingConstants.CENTER);

        headerPanel.add(prevButton, BorderLayout.WEST);
        headerPanel.add(monthLabel, BorderLayout.CENTER);
        headerPanel.add(nextButton, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // 달력 영역: 요일 1행 + 날짜 6행
        calendarPanel = new JPanel(
                new GridLayout(7, 7, 2, 2)
        );
        calendarPanel.setBackground(Color.WHITE);

        add(calendarPanel, BorderLayout.CENTER);

        // 이전 달 이동
        prevButton.addActionListener(e -> {
            currentMonth = currentMonth.minusMonths(1);
            refreshCalendar();
        });

        // 다음 달 이동
        nextButton.addActionListener(e -> {
            currentMonth = currentMonth.plusMonths(1);
            refreshCalendar();
        });

        refreshCalendar();
    }

    /**
     * 현재 선택된 날짜 반환
     *
     * @return 현재 선택된 날짜
     */
    public LocalDate getSelectedDate() {
        return selectedDate;
    }

    /**
     * 날짜 선택 시 실행할 콜백 설정
     *
     * <p>날짜 선택 이후 외부 화면의 날짜 표시,
     * 할 일 목록 갱신 및 팝업 종료 등의 동작과 연동 가능</p>
     *
     * @param onDateSelected 날짜 선택 시 실행할 콜백
     */
    public void setOnDateSelected(Runnable onDateSelected) {
        this.onDateSelected = onDateSelected;
    }

    /**
     * 현재 표시 중인 연도와 월을 기준으로 달력 갱신
     *
     * <p>기존 달력 컴포넌트 제거 후
     * 요일, 빈칸, 날짜 버튼을 순서대로 배치</p>
     *
     * <p>주말 및 선택된 날짜를 색상으로 구분하고
     * 날짜 선택 시 달력 갱신 및 등록된 콜백 실행</p>
     */
    private void refreshCalendar() {
        calendarPanel.removeAll();
        monthLabel.setText(
                currentMonth.getYear() + "년 "
                        + currentMonth.getMonthValue() + "월"
        );
        // 요일 표시
        String[] days = {"일", "월", "화", "수", "목", "금", "토"};
        for (String day : days) {
            calendarPanel.add(new JLabel(day, SwingConstants.CENTER));
        }
        // 해당 월의 첫날과 시작 요일 계산
        LocalDate firstDate = currentMonth.atDay(1);
        int startDay = firstDate.getDayOfWeek().getValue() % 7;
        int lastDay = currentMonth.lengthOfMonth();

        // 첫날 이전의 빈칸
        for (int i = 0; i < startDay; i++) {
            calendarPanel.add(new JLabel(""));
        }

        // 날짜 버튼 생성
        for (int day = 1; day <= lastDay; day++) {

            LocalDate date = currentMonth.atDay(day);

            JButton dayButton = new JButton(String.valueOf(day));
            dayButton.setMargin(new Insets(2, 2, 2, 2));

            dayButton.setBackground(Color.WHITE);
            dayButton.setForeground(Color.BLACK);
            dayButton.setBorderPainted(false);
            dayButton.setFocusPainted(false);
            dayButton.setContentAreaFilled(false);
            dayButton.setOpaque(true);

            // 일요일과 토요일 색상 구분
            if (date.getDayOfWeek() == java.time.DayOfWeek.SUNDAY) {
                dayButton.setForeground(Color.RED);
            } else if (date.getDayOfWeek() == java.time.DayOfWeek.SATURDAY) {
                dayButton.setForeground(Color.BLUE);
            }

            // 선택된 날짜 강조
            if (date.equals(selectedDate)) {
                dayButton.setBackground(new Color(37, 99, 235));
                dayButton.setForeground(Color.WHITE);
                dayButton.setOpaque(true);
            }

            // 날짜 선택 이벤트
            dayButton.addActionListener(e -> {
                selectedDate = date;
                refreshCalendar();
                onDateSelected.run();
            });

            calendarPanel.add(dayButton);
        }

        // 남은 달력 칸 채우기
        for (int i = startDay + lastDay; i < 42; i++) {
            calendarPanel.add(new JLabel(""));
        }

        calendarPanel.revalidate();
        calendarPanel.repaint();
    }

    /**
     * 날짜 선택 패널의 활성화 상태 변경
     *
     * <p>패널뿐만 아니라 내부 날짜 선택 버튼 및
     * 월 이동 버튼의 활성화 상태도 함께 변경</p>
     *
     * @param enabled true면 활성화, false면 비활성화
     */
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        setChildrenEnabled(this, enabled);
    }

    /**
     * 컨테이너 내부 컴포넌트의 활성화 상태 변경
     *
     * <p>내부 컴포넌트가 컨테이너인 경우
     * 재귀 호출을 통해 하위 컴포넌트의 활성화 상태까지 변경</p>
     *
     * @param container 상태를 변경할 컴포넌트들을 포함하는 컨테이너
     * @param enabled true면 활성화, false면 비활성화
     */
    private void setChildrenEnabled(Container container,
                                    boolean enabled) {
        for (Component component : container.getComponents()) {
            component.setEnabled(enabled);

            if (component instanceof Container child) {
                setChildrenEnabled(child, enabled);
            }
        }
    }
}