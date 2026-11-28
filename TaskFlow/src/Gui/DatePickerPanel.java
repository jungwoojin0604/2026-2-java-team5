package Gui;

import model.Theme;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.function.Function;
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
    // 현재 적용된 테마
    private Theme currentTheme;
    // 상단 연월 이동 영역
    private JPanel headerPanel;
    private JButton prevButton;
    private JButton nextButton;
    // 날짜 선택 시 실행할 콜백
    private Runnable onDateSelected = () -> {};

    // 날짜별 Todo 개수를 제공하는 함수
    private Function<LocalDate, Integer> todoCountProvider = date -> 0;

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
        headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);

        prevButton = new JButton("<");
        nextButton = new JButton(">");
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

    // 날짜별 Todo 개수를 가져올 함수를 등록
    public void setTodoCountProvider(
            Function<LocalDate, Integer> provider) {

        todoCountProvider = provider;
        refreshCalendar();
    }

    // Todo 추가·삭제·수정 후 달력의 점 표시 갱신
    public void refreshTodoDots() {
        refreshCalendar();
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
     * 날짜 선택 패널에 지정된 테마 적용
     *
     * <p>달력 배경, 연월 이동 버튼, 날짜 및 요일의 색상과
     * 폰트 변경</p>
     *
     * <p>현재 테마 정보를 유지하여 날짜 선택 및 월 이동 시에도
     * 동일한 테마 적용</p>
     *
     * @param theme 적용할 테마 객체
     */
    public void applyTheme(Theme theme) {
        currentTheme = theme;
        setBackground(theme.getBackgroundColor());
        headerPanel.setBackground(theme.getBackgroundColor());
        calendarPanel.setBackground(theme.getBackgroundColor());
        // 연월 표시
        monthLabel.setForeground(theme.getTextColor());
        monthLabel.setFont(
                theme.getFont().deriveFont(Font.BOLD)
        );

        // 이전 달 이동 버튼
        prevButton.setBackground(theme.getButtonColor());
        prevButton.setForeground(theme.getTextColor());
        prevButton.setFont(theme.getFont());

        // 다음 달 이동 버튼
        nextButton.setBackground(theme.getButtonColor());
        nextButton.setForeground(theme.getTextColor());
        nextButton.setFont(theme.getFont());

        // 달력 내부 갱신
        refreshCalendar();
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
            JLabel dayLabel = new JLabel(day, SwingConstants.CENTER);
            if (currentTheme != null) {
                dayLabel.setForeground(currentTheme.getTextColor());
                dayLabel.setFont(
                        currentTheme.getFont().deriveFont(Font.BOLD)
                );
            }
            calendarPanel.add(dayLabel);
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
            if (currentTheme != null) {
                dayButton.setBackground(currentTheme.getBackgroundColor());
                dayButton.setForeground(currentTheme.getTextColor());
                dayButton.setFont(currentTheme.getFont());
            } else {
                dayButton.setBackground(Color.WHITE);
                dayButton.setForeground(Color.BLACK);
            }
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
                if (currentTheme != null) {
                    Color accent = currentTheme.getAccentColor();
                    dayButton.setBackground(accent);
                    // 강조색 밝기에 따라 글자색 결정
                    double brightness = accent.getRed() * 0.299 + accent.getGreen() * 0.587 + accent.getBlue() * 0.114;
                    dayButton.setForeground(
                            brightness >= 150 ? Color.BLACK : Color.WHITE
                    );
                } else {
                    dayButton.setBackground(new Color(37, 99, 235));
                    dayButton.setForeground(Color.WHITE);
                }
                dayButton.setOpaque(true);
            }

            // 날짜 선택 이벤트
            dayButton.addActionListener(e -> {
                selectedDate = date;
                refreshCalendar();
                onDateSelected.run();
            });

            // 날짜 버튼과 Todo 점을 하나의 칸에 배치
            JPanel dateCell = new JPanel(new BorderLayout());
            dateCell.setBackground(
                    currentTheme != null
                            ? currentTheme.getBackgroundColor()
                            : Color.WHITE
            );
            dateCell.add(dayButton, BorderLayout.CENTER);

            // 해당 날짜의 Todo 개수 조회
            int count = Math.max(0, todoCountProvider.apply(date));

            // 칸이 좁아지는 것을 방지하기 위해 최대 5개까지 표시
            String dots = "●".repeat(Math.min(count, 5));

            JLabel dotLabel = new JLabel(
                    dots, SwingConstants.CENTER
            );

            dotLabel.setFont(new Font(
                    Font.SANS_SERIF, Font.PLAIN, 8
            ));
            dotLabel.setForeground(
                    currentTheme != null
                            ? currentTheme.getAccentColor()
                            : new Color(37, 99, 235)
            );
            dotLabel.setPreferredSize(new Dimension(1, 12));

            // 마우스를 올리면 실제 Todo 개수 확인
            if (count > 0) {
                dotLabel.setToolTipText("할 일 " + count + "개");
            }

            dateCell.add(dotLabel, BorderLayout.SOUTH);
            calendarPanel.add(dateCell);

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