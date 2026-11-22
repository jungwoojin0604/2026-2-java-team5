package gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.function.Consumer;

/**
 * 날짜 입력칸 아래에 뜨는 작은 달력 (DatePickerPopup)
 *
 * <p>docx 예시처럼 "&lt; 2026년 12월 &gt;" 머리글로 달을 옮기고,
 * 날짜를 누르면 그 날짜가 입력칸에 들어가고 달력이 닫힌다.
 * 선택된 날은 검은 원, 오늘은 테두리 원으로 표시한다.</p>
 */
public class DatePickerPopup extends JPopupMenu {
    private static final String[] WEEK = {"일", "월", "화", "수", "목", "금", "토"};
    private static final int CELL = 34;

    private final Consumer<LocalDate> onPick;
    private final LocalDate selected;
    private YearMonth month;
    private final JLabel title = new JLabel("", SwingConstants.CENTER);
    private final JPanel grid = new JPanel(new GridLayout(0, 7, 0, 2));

    private DatePickerPopup(LocalDate initial, Consumer<LocalDate> onPick) {
        this.onPick = onPick;
        this.selected = initial;
        this.month = YearMonth.from(initial == null ? LocalDate.now() : initial);

        setBorder(GuiTheme.line());
        setBackground(Color.WHITE);
        setLayout(new BorderLayout());

        JPanel root = new JPanel(new BorderLayout(0, 6));
        root.setBackground(Color.WHITE);
        root.setBorder(new EmptyBorder(10, 10, 10, 10));

        JButton prev = GuiTheme.textButton("<", GuiTheme.regular(18));
        JButton next = GuiTheme.textButton(">", GuiTheme.regular(18));
        prev.setPreferredSize(new Dimension(CELL, 28));
        next.setPreferredSize(new Dimension(CELL, 28));
        prev.addActionListener(e -> { month = month.minusMonths(1); rebuild(); });
        next.addActionListener(e -> { month = month.plusMonths(1); rebuild(); });
        title.setFont(GuiTheme.bold(15));

        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        head.add(prev, BorderLayout.WEST);
        head.add(title, BorderLayout.CENTER);
        head.add(next, BorderLayout.EAST);

        grid.setOpaque(false);

        root.add(head, BorderLayout.NORTH);
        root.add(grid, BorderLayout.CENTER);
        add(root, BorderLayout.CENTER);
        rebuild();
    }

    /**
     * invoker 아래에 달력을 띄운다.
     *
     * @param invoker 기준이 되는 입력칸
     * @param initial 처음에 선택해 둘 날짜 (null이면 오늘이 있는 달)
     * @param onPick  날짜를 고르면 불리는 함수
     */
    public static void open(Component invoker, LocalDate initial, Consumer<LocalDate> onPick) {
        DatePickerPopup p = new DatePickerPopup(initial, onPick);
        p.show(invoker, 0, invoker.getHeight() + 2);
    }

    private void rebuild() {
        title.setText(month.getYear() + "년 " + month.getMonthValue() + "월");
        grid.removeAll();
        for (int i = 0; i < 7; i++) {
            JLabel w = new JLabel(WEEK[i], SwingConstants.CENTER);
            w.setFont(GuiTheme.regular(13));
            w.setForeground(GuiTheme.TEXT_HINT);
            w.setPreferredSize(new Dimension(CELL, 24));
            grid.add(w);
        }
        // 일요일 시작: DayOfWeek.SUNDAY(7) -> 0칸
        int blank = month.atDay(1).getDayOfWeek().getValue() % 7;
        for (int i = 0; i < blank; i++) grid.add(new JLabel());
        for (int d = 1; d <= month.lengthOfMonth(); d++) {
            grid.add(new DayCell(month.atDay(d)));
        }
        // 6주 높이를 유지해 달을 옮겨도 크기가 흔들리지 않게 한다.
        int used = blank + month.lengthOfMonth();
        for (int i = used; i < 42; i++) grid.add(new JLabel());
        grid.revalidate();
        grid.repaint();
        pack();
    }

    /** 날짜 한 칸 */
    private class DayCell extends JComponent {
        private final LocalDate date;
        private boolean hover;

        DayCell(LocalDate date) {
            this.date = date;
            setPreferredSize(new Dimension(CELL, CELL));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) { hover = true; repaint(); }

                @Override
                public void mouseExited(MouseEvent e) { hover = false; repaint(); }

                @Override
                public void mouseClicked(MouseEvent e) {
                    setVisible(false);
                    DatePickerPopup.this.setVisible(false);
                    onPick.accept(date);
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = GuiTheme.smooth(g);
            int s = Math.min(getWidth(), getHeight()) - 4;
            int x = (getWidth() - s) / 2;
            int y = (getHeight() - s) / 2;
            boolean isSel = date.equals(selected);
            boolean isToday = date.equals(LocalDate.now());
            if (isSel) {
                g2.setColor(GuiTheme.LINE);
                g2.fillOval(x, y, s, s);
            } else if (hover) {
                g2.setColor(GuiTheme.ROW_HOVER);
                g2.fillOval(x, y, s, s);
            }
            if (isToday && !isSel) {
                g2.setColor(GuiTheme.TAB_ACTIVE.darker());
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawOval(x, y, s - 1, s - 1);
            }
            g2.setFont(isSel ? GuiTheme.bold(14) : GuiTheme.regular(14));
            Color fg = isSel ? Color.WHITE
                    : date.getDayOfWeek() == DayOfWeek.SUNDAY ? GuiTheme.ERROR : GuiTheme.TEXT;
            g2.setColor(fg);
            String t = String.valueOf(date.getDayOfMonth());
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(t, (getWidth() - fm.stringWidth(t)) / 2,
                    (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }
}
