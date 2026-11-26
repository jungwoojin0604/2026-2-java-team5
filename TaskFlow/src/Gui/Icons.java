package Gui;

import javax.swing.Icon;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;

/**
 * Figma Components의 아이콘을 Java2D로 그린 모음
 *
 * <p>Swing은 SVG를 바로 읽지 못하므로 같은 크기·모양으로 직접 그림</p>
 */
public final class Icons {

    private Icons() {
    }

    /** Figma Check 부품의 종류 */
    public enum CheckType {
        /** 할 일 완료 동그라미 (꺼짐: 빈 원, 켜짐: 안쪽이 찬 원) */
        TODO,
        /** 창 안의 선택 동그라미 (꺼짐: 연한 원, 켜짐: 검은 원 + 흰 체크) */
        TOGGLE
    }

    /** 공통: 부드러운 선으로 그리기 시작 */
    private static Graphics2D smooth(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        return g2;
    }

    /** 흰 체크 표시 (20px 기준) */
    private static void drawCheckMark(Graphics2D g2, int x, int y) {
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        Path2D mark = new Path2D.Float();
        mark.moveTo(x + 6, y + 10.5);
        mark.lineTo(x + 9, y + 13.5);
        mark.lineTo(x + 14.5, y + 7.5);
        g2.draw(mark);
    }

    /** 20x20 동그라미 체크 (Figma Check: type=todo/toggle, state=off/on/locked) */
    public static Icon check(CheckType type, boolean on, boolean locked) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = smooth(g);
                if (type == CheckType.TODO) {
                    g2.setColor(Color.BLACK);
                    g2.setStroke(new BasicStroke(1.4f));
                    g2.draw(new Ellipse2D.Float(x + 1.2f, y + 1.2f, 17.6f, 17.6f));
                    if (on) {
                        g2.fill(new Ellipse2D.Float(x + 4, y + 4, 12, 12));
                    }
                } else if (locked) {
                    g2.setColor(Theme.CHECK_LOCKED);
                    g2.fill(new Ellipse2D.Float(x + 1, y + 1, 18, 18));
                    drawCheckMark(g2, x, y);
                } else if (on) {
                    g2.setColor(Color.BLACK);
                    g2.fill(new Ellipse2D.Float(x + 1, y + 1, 18, 18));
                    drawCheckMark(g2, x, y);
                } else {
                    g2.setColor(Theme.CHECK_OFF);
                    g2.setStroke(new BasicStroke(1.2f));
                    g2.draw(new Ellipse2D.Float(x + 1.5f, y + 1.5f, 17, 17));
                }
                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return 20;
            }

            @Override
            public int getIconHeight() {
                return 20;
            }
        };
    }

    /** 가는 선 + (Figma의 ExtraLight "+" 글자 대신) */
    public static Icon plus(int size) {
        return new SimpleIcon(size, size) {
            @Override
            void draw(Graphics2D g2, int x, int y) {
                g2.setColor(Color.BLACK);
                g2.setStroke(new BasicStroke(1.3f));
                float m = size / 2f;
                g2.draw(new Line2D.Float(x + 1, y + m, x + size - 1, y + m));
                g2.draw(new Line2D.Float(x + m, y + 1, x + m, y + size - 1));
            }
        };
    }

    /** 돋보기 (검색칸 왼쪽) */
    public static Icon search() {
        return new SimpleIcon(20, 20) {
            @Override
            void draw(Graphics2D g2, int x, int y) {
                g2.setColor(new Color(0x222222));
                g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.draw(new Ellipse2D.Float(x + 2, y + 2, 12.5f, 12.5f));
                g2.draw(new Line2D.Float(x + 12.6f, y + 12.6f, x + 18, y + 18));
            }
        };
    }

    /** X (창 닫기, 16px) */
    public static Icon close() {
        return new SimpleIcon(16, 16) {
            @Override
            void draw(Graphics2D g2, int x, int y) {
                g2.setColor(Color.BLACK);
                g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.draw(new Line2D.Float(x + 3, y + 3, x + 13, y + 13));
                g2.draw(new Line2D.Float(x + 13, y + 3, x + 3, y + 13));
            }
        };
    }

    /** 달력 (날짜칸 왼쪽, 16px) */
    public static Icon calendar() {
        return new SimpleIcon(16, 16) {
            @Override
            void draw(Graphics2D g2, int x, int y) {
                g2.setColor(Color.BLACK);
                g2.setStroke(new BasicStroke(1.3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawRoundRect(x + 2, y + 3, 12, 11, 2, 2);
                g2.draw(new Line2D.Float(x + 2, y + 6.5f, x + 14, y + 6.5f));
                g2.draw(new Line2D.Float(x + 5.5f, y + 1.5f, x + 5.5f, y + 4));
                g2.draw(new Line2D.Float(x + 10.5f, y + 1.5f, x + 10.5f, y + 4));
            }
        };
    }

    /** 아래 화살표 (날짜칸 오른쪽, 12px) */
    public static Icon chevronDown() {
        return new SimpleIcon(12, 12) {
            @Override
            void draw(Graphics2D g2, int x, int y) {
                g2.setColor(new Color(0x666666));
                g2.setStroke(new BasicStroke(1.3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                Path2D p = new Path2D.Float();
                p.moveTo(x + 2.5, y + 4.5);
                p.lineTo(x + 6, y + 8);
                p.lineTo(x + 9.5, y + 4.5);
                g2.draw(p);
            }
        };
    }

    /** 접기·펼치기 화살표 (오른쪽 목록의 프로젝트 줄, 18px) */
    public static Icon chevron(boolean expanded) {
        return new SimpleIcon(18, 18) {
            @Override
            void draw(Graphics2D g2, int x, int y) {
                g2.setColor(Color.BLACK);
                g2.setStroke(new BasicStroke(2.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                Path2D p = new Path2D.Float();
                if (expanded) {
                    p.moveTo(x + 3, y + 6);
                    p.lineTo(x + 9, y + 13);
                    p.lineTo(x + 15, y + 6);
                } else {
                    p.moveTo(x + 6, y + 3);
                    p.lineTo(x + 13, y + 9);
                    p.lineTo(x + 6, y + 15);
                }
                g2.draw(p);
            }
        };
    }

    /** (i) 정보 아이콘 (할 일 줄에 마우스를 올리면 표시, 18px) */
    public static Icon info() {
        return new SimpleIcon(18, 18) {
            @Override
            void draw(Graphics2D g2, int x, int y) {
                g2.setColor(new Color(0x6B6B6B));
                g2.setStroke(new BasicStroke(1.2f));
                g2.draw(new Ellipse2D.Float(x + 1.5f, y + 1.5f, 15, 15));
                g2.fill(new Ellipse2D.Float(x + 8.1f, y + 4.6f, 1.8f, 1.8f));
                g2.setStroke(new BasicStroke(1.5f));
                g2.draw(new Line2D.Float(x + 9, y + 8, x + 9, y + 13));
            }
        };
    }

    /** 위·아래 화살표 (시간 입력칸 오른쪽, 12x28) */
    public static Icon spin() {
        return new SimpleIcon(12, 28) {
            @Override
            void draw(Graphics2D g2, int x, int y) {
                g2.setColor(Color.BLACK);
                Path2D up = new Path2D.Float();
                up.moveTo(x + 3, y + 11);
                up.lineTo(x + 6, y + 7);
                up.lineTo(x + 9, y + 11);
                up.closePath();
                Path2D down = new Path2D.Float();
                down.moveTo(x + 3, y + 17);
                down.lineTo(x + 6, y + 21);
                down.lineTo(x + 9, y + 17);
                down.closePath();
                g2.fill(up);
                g2.fill(down);
            }
        };
    }

    /** 크기만 정하고 draw()만 구현하면 되는 아이콘 */
    private abstract static class SimpleIcon implements Icon {
        private final int width;
        private final int height;

        SimpleIcon(int width, int height) {
            this.width = width;
            this.height = height;
        }

        abstract void draw(Graphics2D g2, int x, int y);

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = smooth(g);
            draw(g2, x, y);
            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return width;
        }

        @Override
        public int getIconHeight() {
            return height;
        }
    }
}
