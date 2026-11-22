package gui;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;

/**
 * 날짜 입력칸 (DateField)
 *
 * <p>칸을 누르면 달력({@link DatePickerPopup})이 뜨고, 직접 "yyyy-MM-dd"로 입력할 수도 있다.
 * 형식 검사는 콘솔 버전(TodoManager.inputTodo)과 같은 STRICT 규칙을 쓴다. (2월 30일 등 거부)</p>
 */
public class DateField extends JPanel {
    public static final DateTimeFormatter FORMAT =
            DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);

    private final JTextField text = new JTextField();

    public DateField(LocalDate initial) {
        super(new BorderLayout(6, 0));
        setBackground(Color.WHITE);
        setBorder(new CompoundBorder(GuiTheme.line(), new EmptyBorder(6, 10, 6, 8)));

        JLabel icon = new JLabel(new CalendarIcon());
        text.setFont(GuiTheme.regular(15));
        text.setBorder(null);
        text.setText(initial == null ? "" : initial.format(FORMAT));
        text.setToolTipText("yyyy-MM-dd");

        JLabel arrow = new JLabel(new DownIcon());

        add(icon, BorderLayout.WEST);
        add(text, BorderLayout.CENTER);
        add(arrow, BorderLayout.EAST);

        MouseAdapter open = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openPicker();
            }
        };
        icon.addMouseListener(open);
        arrow.addMouseListener(open);
        text.addMouseListener(open);
        icon.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        arrow.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void openPicker() {
        LocalDate current;
        try {
            current = getDate();
        } catch (DateTimeException e) {
            current = null;
        }
        DatePickerPopup.open(this, current, d -> text.setText(d.format(FORMAT)));
    }

    /**
     * 입력된 날짜를 돌려준다.
     *
     * @return 입력된 날짜, 비어 있으면 null
     * @throws DateTimeException 형식이 잘못되었거나 없는 날짜일 때
     */
    public LocalDate getDate() {
        String s = text.getText().trim();
        if (s.isEmpty()) return null;
        return LocalDate.parse(s, FORMAT);
    }

    public void setDate(LocalDate date) {
        text.setText(date == null ? "" : date.format(FORMAT));
    }

    /** 아래 화살표 아이콘 (12px). 글꼴마다 ▾ 글자가 없을 수 있어 직접 그린다. */
    private static class DownIcon implements Icon {
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = GuiTheme.smooth(g);
            g2.setColor(GuiTheme.TEXT_HINT);
            g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawPolyline(new int[]{x + 2, x + 6, x + 10}, new int[]{y + 4, y + 8, y + 4}, 3);
            g2.dispose();
        }

        @Override
        public int getIconWidth() { return 12; }

        @Override
        public int getIconHeight() { return 12; }
    }

    /** 작은 달력 모양 아이콘 (16px) */
    private static class CalendarIcon implements Icon {
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = GuiTheme.smooth(g);
            g2.setColor(GuiTheme.TEXT);
            g2.setStroke(new BasicStroke(1.4f));
            g2.drawRoundRect(x + 1, y + 3, 14, 12, 3, 3);
            g2.drawLine(x + 1, y + 7, x + 15, y + 7);
            g2.drawLine(x + 5, y + 1, x + 5, y + 4);
            g2.drawLine(x + 11, y + 1, x + 11, y + 4);
            g2.dispose();
        }

        @Override
        public int getIconWidth() { return 16; }

        @Override
        public int getIconHeight() { return 16; }
    }
}
