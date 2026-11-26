package Gui;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.RootPaneContainer;
import javax.swing.SwingUtilities;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.geom.Path2D;

/**
 * 버튼 바로 아래에 말풍선처럼 뜨는 창 (Figma의 할 일 추가 / 프로젝트 추가 창)
 *
 * <p>별도 창(JDialog)이 아니라 메인 창 위의 레이어(JLayeredPane)에 그려서
 * 위쪽 화살표와 그림자가 운영체제와 상관없이 같은 모양으로 보임</p>
 *
 * <p>창이 떠 있는 동안 뒤 화면은 클릭되지 않음. X 버튼이나 Esc로 닫음</p>
 */
public class Popover {
    private static final int ARROW_H = 12;
    private static final int ARROW_W = 22;
    private static final int SIDE = 8;
    private static final int BOTTOM = 12;
    // Figma: 창 왼쪽 끝은 버튼 가운데에서 62px 왼쪽, 창과 화면 끝 사이는 최소 10px
    private static final int ANCHOR_OFFSET = 62;
    private static final int EDGE = 10;

    private final Component anchor;
    private final JComponent content;
    private final JLayeredPane layer;
    private final JPanel blocker;
    private final Body body;
    private final ComponentAdapter resizeWatcher;
    private boolean open = true;
    private Runnable onClosed = () -> {
    };

    private Popover(Component anchor, JComponent content) {
        this.anchor = anchor;
        this.content = content;

        JRootPane root = anchor instanceof RootPaneContainer rpc
                ? rpc.getRootPane()
                : SwingUtilities.getRootPane(anchor);
        if (root == null) {
            throw new IllegalStateException("창에 붙어 있는 컴포넌트에서만 열 수 있습니다.");
        }
        layer = root.getLayeredPane();

        // 뒤 화면 클릭을 막는 투명한 판
        blocker = new JPanel(null);
        blocker.setOpaque(false);
        blocker.addMouseListener(new MouseAdapter() {
        });
        blocker.addMouseMotionListener(new MouseAdapter() {
        });
        blocker.setBounds(0, 0, layer.getWidth(), layer.getHeight());

        body = new Body();
        body.add(content);

        layer.add(blocker, JLayeredPane.MODAL_LAYER);
        layer.add(body, JLayeredPane.MODAL_LAYER, 0);

        // Esc로 닫기
        body.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("ESCAPE"), "closePopover");
        body.getActionMap().put("closePopover", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                close();
            }
        });

        resizeWatcher = new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                relayout();
            }
        };
        layer.addComponentListener(resizeWatcher);
        relayout();
    }

    /**
     * 말풍선 창 띄우기
     *
     * @param anchor  기준 버튼 (창이면 화살표 없이 가운데에 띄움)
     * @param content 창 안에 넣을 내용 (흰 바탕·검은 테두리는 content가 직접 가짐)
     * @return 열린 창 (닫기, 크기 다시 맞추기에 사용)
     */
    public static Popover show(Component anchor, JComponent content) {
        return new Popover(anchor, content);
    }

    /** 닫힌 뒤 실행할 동작 */
    public void setOnClosed(Runnable onClosed) {
        this.onClosed = onClosed;
    }

    /** 창 닫기 */
    public void close() {
        if (!open) {
            return;
        }
        open = false;
        layer.removeComponentListener(resizeWatcher);
        layer.remove(body);
        layer.remove(blocker);
        layer.repaint();
        onClosed.run();
    }

    /** 내용이 바뀌어 높이가 달라졌을 때 크기와 위치를 다시 맞춤 */
    public void relayout() {
        int layerW = layer.getWidth();
        int layerH = layer.getHeight();
        blocker.setBounds(0, 0, layerW, layerH);

        Dimension size = content.getPreferredSize();
        int boxW = size.width;
        int boxH = size.height;
        boolean centered = anchor instanceof RootPaneContainer;

        int boxX;
        int boxY;
        int arrowX = -1;
        if (centered) {
            boxX = (layerW - boxW) / 2;
            boxY = Math.max(EDGE, (layerH - boxH) / 2);
        } else {
            Rectangle a = SwingUtilities.convertRectangle(
                    anchor.getParent(), anchor.getBounds(), layer);
            int anchorCenter = a.x + a.width / 2;
            boxX = clamp(anchorCenter - ANCHOR_OFFSET, EDGE, layerW - EDGE - boxW);
            boxY = a.y + a.height + 1 + ARROW_H;
            if (boxY + boxH > layerH - EDGE) {
                // 아래 공간이 모자라면 위로 올리고 화살표는 숨김
                boxY = Math.max(EDGE, layerH - EDGE - boxH);
            } else {
                arrowX = clamp(anchorCenter - boxX, ARROW_W, boxW - ARROW_W) + SIDE;
            }
        }

        body.arrowX = arrowX;
        content.setBounds(SIDE, ARROW_H, boxW, boxH);
        body.setBounds(boxX - SIDE, boxY - ARROW_H, boxW + SIDE * 2, boxH + ARROW_H + BOTTOM);
        body.revalidate();
        body.repaint();
        layer.repaint();
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(value, Math.max(min, max)));
    }

    /** 그림자와 위쪽 화살표를 그리는 바깥 판 */
    private class Body extends JPanel {
        int arrowX = -1;

        Body() {
            super(null);
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            // Figma drop-shadow(0, 4, 8, 14%)를 겹친 사각형으로 흉내
            Graphics2D g2 = (Graphics2D) g.create();
            Rectangle box = content.getBounds();
            for (int i = 6; i >= 1; i--) {
                g2.setColor(new Color(0, 0, 0, 7));
                g2.fillRect(box.x - i + 1, box.y - i + 4, box.width + (i - 1) * 2, box.height + (i - 1) * 2);
            }
            g2.dispose();
        }

        @Override
        public void paint(Graphics g) {
            super.paint(g);
            if (arrowX < 0) {
                return;
            }
            // 화살표: 흰색으로 채우고 양쪽 빗변만 검은 선
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int half = ARROW_W / 2;
            Path2D fill = new Path2D.Float();
            fill.moveTo(arrowX - half, ARROW_H + 1);
            fill.lineTo(arrowX, 0.5);
            fill.lineTo(arrowX + half, ARROW_H + 1);
            fill.closePath();
            g2.setColor(Color.WHITE);
            g2.fill(fill);
            Path2D edge = new Path2D.Float();
            edge.moveTo(arrowX - half, ARROW_H + 0.5);
            edge.lineTo(arrowX, 0.5);
            edge.lineTo(arrowX + half, ARROW_H + 0.5);
            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(1f));
            g2.draw(edge);
            g2.dispose();
        }

        @Override
        public boolean contains(int x, int y) {
            // 그림자 부분은 클릭이 뒤 판으로 가도록
            Point p = new Point(x, y);
            return content.getBounds().contains(p) || (arrowX >= 0 && y < ARROW_H);
        }
    }
}
