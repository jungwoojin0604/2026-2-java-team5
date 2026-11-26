package Gui;

import manager.TodoManager;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.function.Consumer;

/**
 * Calendar 탭 왼쪽의 월 달력 (Figma F. Calendar 탭 – 월 달력)
 *
 * <p>위쪽 "&lt; 2027.11 &gt;"로 달을 옮기고, 아래 7x6 칸에 날짜 표시</p>
 *
 * <p>선택한 날은 하늘색 원, 할 일이 있는 날은 숫자 아래 점, 일요일은 빨간 글자,
 * 다른 달 날짜는 흐린 회색</p>
 *
 * <p>날짜를 누르면 오른쪽 목록이 그 날짜로 바뀜 (콜백으로 전달)</p>
 */
public class CalendarPanel extends JPanel {
    // Figma 칸 크기
    private static final int CELL_W = 57;
    private static final int CELL_H = 84;
    private static final int WEEKDAY_H = 30;

    private final TodoManager todoManager;
    private YearMonth currentMonth;
    private LocalDate selectedDate = LocalDate.now();
    private boolean anytimeSelected = false;

    private final JLabel monthLabel;
    private final JPanel gridPanel;
    private final JButton anytimeButton;

    private Consumer<LocalDate> onDateSelected = date -> {
    };
    private Runnable onAnytime = () -> {
    };

    public CalendarPanel(TodoManager todoManager) {
        this.todoManager = todoManager;
        this.currentMonth = YearMonth.from(selectedDate);

        setLayout(new BorderLayout());
        setBackground(Theme.PANEL_BG);

        // 1. 달 이동 줄: <  2027.11  >
        JLabel prev = UiKit.label("<", Theme.plain(30), Color.BLACK);
        JLabel next = UiKit.label(">", Theme.plain(30), Color.BLACK);
        monthLabel = new JLabel("", SwingConstants.CENTER);
        monthLabel.setFont(Theme.bold(30));
        monthLabel.setPreferredSize(new Dimension(180, 41));
        JPanel monthBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        monthBar.setOpaque(false);
        monthBar.setBorder(BorderFactory.createEmptyBorder(28, 0, 16, 0));
        monthBar.add(prev);
        monthBar.add(monthLabel);
        monthBar.add(next);
        add(monthBar, BorderLayout.NORTH);

        UiKit.onClick(prev, () -> moveMonth(-1));
        UiKit.onClick(next, () -> moveMonth(1));

        // 2. 요일 + 날짜 칸 (가운데 정렬, 왼쪽 9px)
        gridPanel = new JPanel(new GridLayout(0, 7, 0, 0));
        gridPanel.setOpaque(false);
        gridPanel.setBorder(BorderFactory.createMatteBorder(1, 1, 0, 0, Theme.GRID_LINE));
        JPanel gridWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        gridWrap.setOpaque(false);
        gridWrap.setBorder(BorderFactory.createEmptyBorder(0, 9, 0, 0));
        gridWrap.add(gridPanel);
        add(gridWrap, BorderLayout.CENTER);

        // 3. 아래 버튼: 오늘로 이동, 날짜 없는 할 일(Anytime) 보기
        JButton todayButton = smallButton("오늘");
        anytimeButton = smallButton("Anytime");
        todayButton.addActionListener(e -> {
            currentMonth = YearMonth.now();
            selectDate(LocalDate.now());
        });
        anytimeButton.addActionListener(e -> {
            anytimeSelected = true;
            refresh();
            onAnytime.run();
        });
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 11, 0));
        bottom.setOpaque(false);
        bottom.setBorder(BorderFactory.createEmptyBorder(0, 0, 22, 0));
        bottom.add(todayButton);
        bottom.add(anytimeButton);
        add(bottom, BorderLayout.SOUTH);

        refresh();
    }

    /** 날짜를 눌렀을 때 실행할 동작 */
    public void setOnDateSelected(Consumer<LocalDate> onDateSelected) {
        this.onDateSelected = onDateSelected;
    }

    /** Anytime 버튼을 눌렀을 때 실행할 동작 */
    public void setOnAnytime(Runnable onAnytime) {
        this.onAnytime = onAnytime;
    }

    /** 할 일이 바뀌었을 때 점 표시를 다시 그림 */
    public void refresh() {
        monthLabel.setText(String.format("%d.%02d", currentMonth.getYear(), currentMonth.getMonthValue()));
        anytimeButton.setBackground(anytimeSelected ? Theme.ACCENT : Theme.BUTTON_GRAY);

        gridPanel.removeAll();
        String[] days = {"일", "월", "화", "수", "목", "금", "토"};
        for (int i = 0; i < 7; i++) {
            JLabel label = new JLabel(days[i], SwingConstants.CENTER);
            label.setFont(Theme.bold(13));
            label.setForeground(i == 0 ? Theme.RED : Theme.TEXT_GRAY);
            label.setOpaque(true);
            label.setBackground(Theme.WEEKDAY_BG);
            label.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Theme.GRID_LINE));
            label.setPreferredSize(new Dimension(CELL_W, WEEKDAY_H));
            gridPanel.add(label);
        }

        // 첫 칸은 그 달 1일이 있는 주의 일요일
        LocalDate first = currentMonth.atDay(1);
        LocalDate start = first.minusDays(first.getDayOfWeek().getValue() % 7);
        for (int i = 0; i < 42; i++) {
            gridPanel.add(new DayCell(start.plusDays(i)));
        }
        gridPanel.revalidate();
        gridPanel.repaint();
    }

    private void moveMonth(int delta) {
        currentMonth = currentMonth.plusMonths(delta);
        refresh();
    }

    private void selectDate(LocalDate date) {
        selectedDate = date;
        anytimeSelected = false;
        refresh();
        onDateSelected.accept(date);
    }

    /** 아래쪽 작은 회색 버튼 (Figma + 버튼과 같은 색·테두리) */
    private static JButton smallButton(String text) {
        JButton button = new JButton(text);
        button.setFont(Theme.bold(15));
        button.setForeground(Color.BLACK);
        button.setBackground(Theme.BUTTON_GRAY);
        button.setOpaque(true);
        button.setFocusPainted(false);
        button.setBorder(UiKit.blackLine());
        button.setPreferredSize(new Dimension(100, 35));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    /** 달력 한 칸 (Figma CalendarCell: default / selected / muted) */
    private class DayCell extends JPanel {
        private final LocalDate date;
        private final boolean muted;
        private final boolean hasTodo;
        private boolean hover;

        DayCell(LocalDate date) {
            this.date = date;
            this.muted = !YearMonth.from(date).equals(currentMonth);
            this.hasTodo = !todoManager.getTodo(date).isEmpty();
            setOpaque(false);
            setPreferredSize(new Dimension(CELL_W, CELL_H));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (muted) {
                        // 다른 달 날짜를 누르면 그 달로 이동
                        currentMonth = YearMonth.from(DayCell.this.date);
                    }
                    selectDate(DayCell.this.date);
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    hover = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hover = false;
                    repaint();
                }
            });
            String tip = date.format(Theme.DOT_DATE);
            if (hasTodo) {
                tip += " · 할 일 " + todoManager.getTodo(date).size() + "개";
            }
            setToolTipText(tip);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();

            if (hover) {
                g2.setColor(new Color(0xEAECEF));
                g2.fillRect(0, 0, w, h);
            }

            // 오른쪽·아래 선
            g2.setColor(Theme.GRID_LINE);
            g2.drawLine(w - 1, 0, w - 1, h - 1);
            g2.drawLine(0, h - 1, w - 1, h - 1);

            boolean selected = !anytimeSelected && date.equals(selectedDate);
            boolean today = date.equals(LocalDate.now());
            int circle = 30;
            int cx = (w - circle) / 2;
            int cy = 6;

            // 선택한 날: 하늘색 원 + 검은 테두리
            if (selected) {
                g2.setColor(Theme.ACCENT);
                g2.fillOval(cx, cy, circle, circle);
                g2.setColor(Color.BLACK);
                g2.drawOval(cx, cy, circle - 1, circle - 1);
            }

            // 숫자 색: 다른 달 > 일요일 > 기본
            Color color;
            if (muted) {
                color = Theme.TEXT_MUTED;
            } else if (date.getDayOfWeek() == DayOfWeek.SUNDAY) {
                color = Theme.RED;
            } else {
                color = Color.BLACK;
            }
            Font font = (selected || today) ? Theme.bold(15) : Theme.plain(15);
            g2.setFont(font);
            g2.setColor(color);
            String text = String.valueOf(date.getDayOfMonth());
            FontMetrics fm = g2.getFontMetrics();
            int tx = (w - fm.stringWidth(text)) / 2;
            int ty = cy + (circle + fm.getAscent() - fm.getDescent()) / 2;
            g2.drawString(text, tx, ty);

            // 오늘: 숫자 아래 짧은 밑줄
            if (today && !selected) {
                g2.fillRect(w / 2 - 6, cy + circle - 3, 12, 2);
            }

            // 할 일 있는 날: 숫자 아래 점 두 개 (13x5)
            if (hasTodo) {
                g2.setColor(muted ? Theme.TEXT_MUTED : Color.BLACK);
                int dotY = cy + circle + 5;
                int left = (w - 13) / 2;
                g2.fillOval(left, dotY, 5, 5);
                g2.fillOval(left + 8, dotY, 5, 5);
            }
            g2.dispose();
        }
    }
}
