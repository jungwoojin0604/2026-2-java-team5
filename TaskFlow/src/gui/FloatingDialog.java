package gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.Area;
import java.awt.geom.Path2D;

/**
 * 버튼 옆에 말풍선처럼 떠 있는 창 (FloatingDialog)
 *
 * <p>docx 스케치처럼 "할 일 추가"를 누른 자리 옆에 창을 띄우고,
 * 왼쪽(또는 오른쪽) 가장자리에 그 자리를 가리키는 작은 화살표를 그린다.</p>
 *
 * <p>창 배경을 투명하게 만들 수 없는 환경(일부 리눅스)에서는 화살표 없이 사각형 창으로 뜬다.</p>
 *
 * <p>Esc 키를 누르면 저장 없이 닫힌다.</p>
 */
public class FloatingDialog extends JDialog {
    static final int ARROW = 10;
    private static final int GAP = 2;

    private final boolean translucent;
    private final Bubble bubble;

    public FloatingDialog(Window owner) {
        super(owner, ModalityType.APPLICATION_MODAL);
        setUndecorated(true);
        GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        translucent = device.isWindowTranslucencySupported(GraphicsDevice.WindowTranslucency.PERPIXEL_TRANSLUCENT);
        if (translucent) {
            setBackground(new Color(0, 0, 0, 0));
        }
        bubble = new Bubble();
        setContentPane(bubble);

        // Esc = 닫기(저장 안 함)
        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    /** 창 안에 들어갈 내용을 정한다. */
    public void setBody(JComponent body) {
        bubble.removeAll();
        bubble.add(body, BorderLayout.CENTER);
    }

    /**
     * anchor 오른쪽에 창을 띄운다. 화면 오른쪽이 모자라면 왼쪽에 띄운다.
     * anchor가 null이거나 화면에 없으면 owner 가운데에 띄운다.
     */
    public void showBeside(Component anchor) {
        pack();
        if (anchor == null || !anchor.isShowing()) {
            bubble.setArrow(Side.NONE, 0);
            setLocationRelativeTo(getOwner());
            setVisible(true);
            return;
        }
        Point p = anchor.getLocationOnScreen();
        Rectangle a = new Rectangle(p.x, p.y, anchor.getWidth(), anchor.getHeight());
        Rectangle screen = anchor.getGraphicsConfiguration().getBounds();
        Insets si = Toolkit.getDefaultToolkit().getScreenInsets(anchor.getGraphicsConfiguration());
        screen = new Rectangle(screen.x + si.left, screen.y + si.top,
                screen.width - si.left - si.right, screen.height - si.top - si.bottom);

        int w = getWidth();
        int h = getHeight();
        Side side = Side.LEFT;
        int x = a.x + a.width + GAP;
        if (x + w > screen.x + screen.width) {
            side = Side.RIGHT;
            x = a.x - w - GAP;
        }
        int centerY = a.y + a.height / 2;
        int y = centerY - 34;               // 화살표가 창 위쪽(첫 입력칸 근처)에 오도록
        y = Math.max(screen.y, Math.min(y, screen.y + screen.height - h));
        int arrowY = Math.max(16, Math.min(centerY - y, h - 16));

        bubble.setArrow(translucent ? side : Side.NONE, arrowY);
        setLocation(x, y);
        setVisible(true);
    }

    enum Side { NONE, LEFT, RIGHT }

    /** 흰 바탕 + 검은 1px 테두리 + 화살표를 그리는 바탕 패널 */
    private class Bubble extends JPanel {
        private Side side = Side.NONE;
        private int arrowY = 30;

        Bubble() {
            super(new BorderLayout());
            setOpaque(!translucent);
            setBackground(Color.WHITE);
            int pad = translucent ? ARROW : 0;
            // 화살표 자리를 양쪽에 미리 비워 두어, 어느 쪽에 떠도 창 크기가 같게 한다.
            setBorder(new EmptyBorder(1, pad + 1, 1, pad + 1));
        }

        void setArrow(Side side, int arrowY) {
            this.side = side;
            this.arrowY = arrowY;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = GuiTheme.smooth(g);
            int pad = translucent ? ARROW : 0;
            int w = getWidth();
            int h = getHeight();
            Area shape = new Area(new Rectangle(pad, 0, w - pad * 2 - 1, h - 1));
            if (side != Side.NONE) {
                Path2D tri = new Path2D.Float();
                if (side == Side.LEFT) {
                    tri.moveTo(pad + 1, arrowY - ARROW);
                    tri.lineTo(0, arrowY);
                    tri.lineTo(pad + 1, arrowY + ARROW);
                } else {
                    tri.moveTo(w - pad - 2, arrowY - ARROW);
                    tri.lineTo(w - 1, arrowY);
                    tri.lineTo(w - pad - 2, arrowY + ARROW);
                }
                tri.closePath();
                shape.add(new Area(tri));
            }
            g2.setColor(Color.WHITE);
            g2.fill(shape);
            g2.setColor(GuiTheme.LINE);
            g2.setStroke(new BasicStroke(1f));
            g2.draw(shape);
            g2.dispose();
        }
    }
}
