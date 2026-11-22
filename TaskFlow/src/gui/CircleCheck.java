package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * 동그라미 모양 체크 (CircleCheck)
 *
 * <ul>
 *   <li>DOT: Figma 할 일 행 모양. 20px 빈 원, 선택하면 안에 검은 원(16px)이 찬다.</li>
 *   <li>CHECK: docx 예시의 Anytime / 사용 / 연결된 프로젝트 선택 모양. 선택하면 검은 원 안에 흰 체크.</li>
 * </ul>
 *
 * <p>locked가 true이면 회색으로 그리고 클릭해도 바뀌지 않는다. (현재 프로젝트 고정용)</p>
 */
public class CircleCheck extends JComponent {
    public enum Style { DOT, CHECK }

    private final Style style;
    private final int diameter;
    private boolean selected;
    private boolean locked;
    private final List<Consumer<Boolean>> listeners = new ArrayList<>();

    public CircleCheck(Style style, int diameter, boolean selected) {
        this.style = style;
        this.diameter = diameter;
        this.selected = selected;
        Dimension d = new Dimension(diameter + 2, diameter + 2);
        setPreferredSize(d);
        setMinimumSize(d);
        setMaximumSize(d);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                toggleByUser();
            }
        });
    }

    /** 사용자가 누른 것처럼 상태를 바꾸고 리스너에 알린다. (옆 글자 클릭에도 사용) */
    public void toggleByUser() {
        if (locked || !isEnabled()) return;
        setSelected(!selected);
        for (Consumer<Boolean> l : listeners) l.accept(selected);
    }

    public void addChangeListener(Consumer<Boolean> listener) {
        listeners.add(listener);
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        repaint();
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
        setCursor(locked ? Cursor.getDefaultCursor() : Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        repaint();
    }

    public boolean isLocked() {
        return locked;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = GuiTheme.smooth(g);
        int x = (getWidth() - diameter) / 2;
        int y = (getHeight() - diameter) / 2;
        Color main = locked ? GuiTheme.ICON_GRAY : GuiTheme.LINE;

        if (style == Style.DOT) {
            g2.setColor(Color.WHITE);
            g2.fillOval(x, y, diameter, diameter);
            g2.setColor(main);
            g2.setStroke(new BasicStroke(1.4f));
            g2.drawOval(x, y, diameter - 1, diameter - 1);
            if (selected) {
                int inner = Math.round(diameter * 0.8f);
                int o = (diameter - inner) / 2;
                g2.fillOval(x + o, y + o, inner, inner);
            }
        } else {
            if (selected) {
                g2.setColor(main);
                g2.fillOval(x, y, diameter, diameter);
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(diameter / 9f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                float s = diameter;
                g2.drawPolyline(
                        new int[]{Math.round(x + s * 0.27f), Math.round(x + s * 0.44f), Math.round(x + s * 0.74f)},
                        new int[]{Math.round(y + s * 0.52f), Math.round(y + s * 0.68f), Math.round(y + s * 0.36f)},
                        3);
            } else {
                g2.setColor(Color.WHITE);
                g2.fillOval(x, y, diameter, diameter);
                g2.setColor(new Color(0xBDBDBD));
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawOval(x, y, diameter - 1, diameter - 1);
            }
        }
        g2.dispose();
    }
}
