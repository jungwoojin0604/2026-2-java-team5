package Gui;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;

/**
 * 날짜 선택 기능을 제공하는 달력 패널 클래스 (Figma DatePicker 모양)
 *
 * <p>연도와 월을 기준으로 달력을 표시하고
 * 이전 달 및 다음 달 이동과 날짜 선택 기능 제공</p>
 *
 * <p>선택한 날짜는 검은 원 + 흰 굵은 글자, 일요일은 빨간 글자로 표시</p>
 *
 * <p>날짜 선택 시 콜백을 실행하여 외부 화면과 연동할 수 있고
 * 날짜 선택 영역의 활성화 및 비활성화 가능</p>
 */
public class DatePickerPanel extends JPanel {
    // 날짜 한 칸 크기 (Figma 34x32)
    private static final int CELL_W = 34;
    private static final int CELL_H = 32;

    // 실제 선택된 날짜
    private LocalDate selectedDate;
    // 현재 달력에 표시되는 연도 및 월
    private YearMonth currentMonth;
    private final JLabel monthLabel;
    private final JPanel calendarPanel;
    // 날짜 선택 시 실행할 콜백
    private Runnable onDateSelected = () -> {
    };

    /**
     * 기본 날짜를 기준으로 날짜 선택 패널 생성
     *
     * @param defaultDate 기본 선택 날짜 (null이면 오늘 날짜 사용)
     */
    public DatePickerPanel(LocalDate defaultDate) {
        selectedDate = defaultDate == null ? LocalDate.now() : defaultDate;
        currentMonth = YearMonth.from(selectedDate);
        setLayout(new BorderLayout(0, 4));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createCompoundBorder(
                UiKit.blackLine(), BorderFactory.createEmptyBorder(10, 12, 12, 12)));

        // 상단 연월 이동 영역: <  2027년 11월  >
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 4, 10));

        JLabel prevButton = UiKit.label("<", Theme.plain(18), Color.BLACK);
        JLabel nextButton = UiKit.label(">", Theme.plain(18), Color.BLACK);
        monthLabel = new JLabel("", SwingConstants.CENTER);
        monthLabel.setFont(Theme.bold(16));

        headerPanel.add(prevButton, BorderLayout.WEST);
        headerPanel.add(monthLabel, BorderLayout.CENTER);
        headerPanel.add(nextButton, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // 달력 영역: 요일 1행 + 날짜 6행
        calendarPanel = new JPanel(new GridLayout(7, 7, 0, 0));
        calendarPanel.setOpaque(false);
        calendarPanel.setPreferredSize(new Dimension(CELL_W * 7, CELL_H * 7));
        add(calendarPanel, BorderLayout.CENTER);

        // 이전 달 / 다음 달 이동
        UiKit.onClick(prevButton, () -> {
            currentMonth = currentMonth.minusMonths(1);
            refreshCalendar();
        });
        UiKit.onClick(nextButton, () -> {
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
     * @param onDateSelected 날짜 선택 시 실행할 콜백
     */
    public void setOnDateSelected(Runnable onDateSelected) {
        this.onDateSelected = onDateSelected;
    }

    /**
     * 현재 표시 중인 연도와 월을 기준으로 달력 갱신
     */
    private void refreshCalendar() {
        calendarPanel.removeAll();
        monthLabel.setText(currentMonth.getYear() + "년 " + currentMonth.getMonthValue() + "월");

        // 요일 표시 (회색 13pt)
        String[] days = {"일", "월", "화", "수", "목", "금", "토"};
        for (String day : days) {
            JLabel label = new JLabel(day, SwingConstants.CENTER);
            label.setFont(Theme.plain(13));
            label.setForeground(Theme.TEXT_LABEL);
            calendarPanel.add(label);
        }

        // 해당 월의 첫날과 시작 요일 계산
        int startDay = currentMonth.atDay(1).getDayOfWeek().getValue() % 7;
        int lastDay = currentMonth.lengthOfMonth();

        // 첫날 이전의 빈칸
        for (int i = 0; i < startDay; i++) {
            calendarPanel.add(new JLabel(""));
        }

        // 날짜 칸 생성
        for (int day = 1; day <= lastDay; day++) {
            calendarPanel.add(new DayCell(currentMonth.atDay(day)));
        }

        // 남은 달력 칸 채우기
        for (int i = startDay + lastDay; i < 42; i++) {
            calendarPanel.add(new JLabel(""));
        }

        calendarPanel.revalidate();
        calendarPanel.repaint();
    }

    /** 날짜 한 칸: 선택된 날은 검은 원 위에 흰 굵은 숫자 */
    private class DayCell extends JLabel {
        private final LocalDate date;

        DayCell(LocalDate date) {
            super(String.valueOf(date.getDayOfMonth()), SwingConstants.CENTER);
            this.date = date;
            boolean selected = date.equals(selectedDate);
            setFont(selected ? Theme.bold(14) : Theme.plain(14));
            if (selected) {
                setForeground(Color.WHITE);
            } else if (date.getDayOfWeek() == DayOfWeek.SUNDAY) {
                setForeground(Theme.RED);
            } else {
                setForeground(Color.BLACK);
            }
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            // 날짜 선택 이벤트
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (!DatePickerPanel.this.isEnabled()) {
                        return;
                    }
                    selectedDate = DayCell.this.date;
                    refreshCalendar();
                    onDateSelected.run();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (date.equals(selectedDate)) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.BLACK);
                int d = 28;
                g2.fillOval((getWidth() - d) / 2, (getHeight() - d) / 2, d, d);
                g2.dispose();
            }
            super.paintComponent(g);
        }
    }

    /**
     * 날짜 선택 패널의 활성화 상태 변경
     *
     * @param enabled true면 활성화, false면 비활성화
     */
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        setChildrenEnabled(this, enabled);
    }

    /**
     * 컨테이너 내부 컴포넌트의 활성화 상태 변경 (재귀)
     */
    private void setChildrenEnabled(Container container, boolean enabled) {
        for (Component component : container.getComponents()) {
            component.setEnabled(enabled);
            if (component instanceof Container child) {
                setChildrenEnabled(child, enabled);
            }
        }
    }
}
