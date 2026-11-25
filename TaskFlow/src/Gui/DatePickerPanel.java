package Gui;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.YearMonth;

public class DatePickerPanel extends JPanel {

    // 실제 선택된 날짜
    private LocalDate selectedDate;

    // 현재 달력에 표시되는 연도 및 월
    private YearMonth currentMonth;

    private JLabel monthLabel;
    private JPanel calendarPanel;

    public DatePickerPanel(LocalDate defaultDate) {

        selectedDate = defaultDate == null
                ? LocalDate.now() : defaultDate;

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

    // 현재 선택된 날짜 반환
    public LocalDate getSelectedDate() {
        return selectedDate;
    }

    // 현재 연월에 해당하는 달력 표시
    private void refreshCalendar() {

        calendarPanel.removeAll();

        monthLabel.setText(
                currentMonth.getYear() + "년 "
                        + currentMonth.getMonthValue() + "월"
        );

        // 요일 표시
        String[] days = {"일", "월", "화", "수", "목", "금", "토"};

        for (String day : days) {
            calendarPanel.add(
                    new JLabel(day, SwingConstants.CENTER)
            );
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

    // 날짜 선택 영역 활성화 및 비활성화
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        setChildrenEnabled(this, enabled);
    }

    // 내부 컴포넌트까지 활성화 상태 변경
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