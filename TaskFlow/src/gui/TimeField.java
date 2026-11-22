package gui;

import javax.swing.*;
import java.awt.*;
import java.text.ParseException;
import java.time.DateTimeException;
import java.time.LocalTime;

/**
 * 시·분 입력칸 (TimeField)
 *
 * <p>docx 스케치처럼 [18] : [00] 두 칸이고, ▲▼로 조절하거나 숫자를 직접 입력할 수 있다.
 * 시는 0~23, 분은 0~59. 범위를 벗어난 값을 입력하면 {@link #getTime()}이 예외를 던진다.</p>
 */
public class TimeField extends JPanel {
    private final JSpinner hour;
    private final JSpinner minute;

    public TimeField(LocalTime initial) {
        super(new FlowLayout(FlowLayout.LEFT, 0, 0));
        setOpaque(false);
        LocalTime t = initial == null ? LocalTime.of(18, 0) : initial;
        hour = spinner(t.getHour(), 23);
        minute = spinner(t.getMinute(), 59);

        JLabel colon = new JLabel(" : ");
        colon.setFont(GuiTheme.bold(18));

        add(hour);
        add(label(" 시"));
        add(colon);
        add(minute);
        add(label(" 분"));
    }

    private static JLabel label(String s) {
        JLabel l = new JLabel(s);
        l.setFont(GuiTheme.regular(14));
        l.setForeground(GuiTheme.TEXT_HINT);
        return l;
    }

    private static JSpinner spinner(int value, int max) {
        JSpinner s = new JSpinner(new SpinnerNumberModel(value, 0, max, 1));
        JSpinner.NumberEditor editor = new JSpinner.NumberEditor(s, "00");
        s.setEditor(editor);
        JFormattedTextField tf = editor.getTextField();
        tf.setFont(GuiTheme.regular(17));
        tf.setHorizontalAlignment(JTextField.CENTER);
        tf.setColumns(2);
        // 직접 입력 중 잘못된 값이면 이전 값으로 되돌리지 않고 그대로 두어, 저장할 때 안내한다.
        tf.setFocusLostBehavior(JFormattedTextField.COMMIT);
        s.setBorder(GuiTheme.line());
        s.setPreferredSize(new Dimension(72, 34));
        return s;
    }

    /**
     * 입력된 시간을 돌려준다.
     *
     * @throws DateTimeException 직접 입력한 값이 숫자가 아니거나 범위를 벗어났을 때
     */
    public LocalTime getTime() {
        return LocalTime.of(read(hour), read(minute));
    }

    private static int read(JSpinner s) {
        try {
            s.commitEdit();
        } catch (ParseException e) {
            throw new DateTimeException("시간 형식 오류", e);
        }
        return ((Number) s.getValue()).intValue();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        hour.setEnabled(enabled);
        minute.setEnabled(enabled);
    }
}
