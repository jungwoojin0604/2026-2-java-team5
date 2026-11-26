package Gui;

import model.Project;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Project 탭 왼쪽 목록의 프로젝트 한 줄 (Figma ProjectRow, 418x48)
 *
 * <p>이름(굵게 20pt), D-Day, 진행률 막대(JProgressBar), 완료 비율(%), ⋯ 메뉴 버튼 표시</p>
 *
 * <p>줄을 누르면 오른쪽에 그 프로젝트의 할 일이 표시되고(선택한 줄은 연한 하늘색),
 * ⋯ 버튼 또는 우클릭으로 수정·삭제 메뉴 표시</p>
 */
public class ProjectItemPanel extends JPanel {
    /** Figma 프로젝트 줄 높이 */
    public static final int ROW_HEIGHT = 48;

    private final Project project;
    private final boolean selected;
    private final JLabel moreLabel;

    /**
     * 프로젝트 한 줄 생성
     *
     * @param project    표시할 프로젝트
     * @param completed  완료한 할 일 수
     * @param total      전체 할 일 수
     * @param selected   현재 선택된 프로젝트인지
     * @param onSelected 줄을 눌렀을 때 실행할 동작
     * @param onEdit     메뉴에서 "수정"을 눌렀을 때 실행할 동작
     * @param onDelete   메뉴에서 "삭제"를 눌렀을 때 실행할 동작
     */
    public ProjectItemPanel(Project project, int completed, int total, boolean selected,
                            Runnable onSelected, Runnable onEdit, Runnable onDelete) {
        this.project = project;
        this.selected = selected;

        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setBackground(selected ? Theme.ROW_SELECTED : Theme.PANEL_BG);
        setOpaque(true);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.ROW_DIVIDER),
                BorderFactory.createEmptyBorder(0, 16, 0, 10)));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // 1. 프로젝트 이름 (길면 ...으로 줄임)
        JLabel nameLabel = UiKit.label(project.getName(), Theme.bold(20), Color.BLACK);
        nameLabel.setMinimumSize(new Dimension(0, 30));
        nameLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        nameLabel.setToolTipText(project.getName());

        // 2. D-Day
        String dDay = project.getDeadLine() == null
                ? "미설정"
                : Theme.dDay(project.getDeadLine().toLocalDate());
        JLabel dDayLabel = UiKit.label(dDay, Theme.plain(14), Theme.TEXT_GRAY);

        // 3. 완료한 할 일의 비율 (진행률 막대 + %)
        int progress = total == 0 ? 0 : completed * 100 / total;
        JLabel percentLabel = UiKit.label(progress + "%", Theme.plain(14), Color.BLACK);
        percentLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        Dimension percentSize = new Dimension(40, 20);
        percentLabel.setPreferredSize(percentSize);
        percentLabel.setMaximumSize(percentSize);

        // 4. ⋯ 메뉴 버튼
        moreLabel = UiKit.label("⋯", Theme.bold(18), Color.BLACK);
        moreLabel.setToolTipText("수정·삭제");

        add(nameLabel);
        add(Box.createHorizontalStrut(8));
        add(dDayLabel);
        add(Box.createHorizontalStrut(8));
        add(centered(UiKit.progressBar(progress)));
        add(Box.createHorizontalStrut(8));
        add(percentLabel);
        add(Box.createHorizontalStrut(8));
        add(moreLabel);

        Dimension size = new Dimension(418, ROW_HEIGHT);
        setPreferredSize(size);
        setMaximumSize(new Dimension(Integer.MAX_VALUE, ROW_HEIGHT));
        setAlignmentX(Component.LEFT_ALIGNMENT);
        setToolTipText(total == 0
                ? "연결된 할 일 없음"
                : "할 일 " + completed + " / " + total + "개 완료");

        JPopupMenu menu = UiKit.contextMenu(
                new String[]{"수정", "삭제"},
                new Runnable[]{onEdit, onDelete});

        // ⋯ 클릭: 메뉴, 줄 클릭: 선택, 우클릭: 메뉴
        moreLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        moreLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                menu.show(moreLabel, moreLabel.getWidth() - 120 + 10, moreLabel.getHeight() + 4);
            }
        });
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showMenuAt(menu, e);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showMenuAt(menu, e);
                } else if (SwingUtilities.isLeftMouseButton(e) && contains(e.getPoint())) {
                    onSelected.run();
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                if (!ProjectItemPanel.this.selected) {
                    setBackground(new Color(0xEAECEF));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setBackground(ProjectItemPanel.this.selected ? Theme.ROW_SELECTED : Theme.PANEL_BG);
            }
        });
    }

    private void showMenuAt(JPopupMenu menu, MouseEvent e) {
        Point p = e.getPoint();
        menu.show(this, p.x, p.y);
    }

    /** 이 줄의 프로젝트 */
    public Project getProject() {
        return project;
    }

    /** ⋯ 버튼 (수정 창 위치 기준) */
    public JLabel getMoreButton() {
        return moreLabel;
    }

    /** 높이가 다른 부품을 세로 가운데에 두기 */
    private static Component centered(Component component) {
        Box box = Box.createVerticalBox();
        box.add(Box.createVerticalGlue());
        box.add(component);
        box.add(Box.createVerticalGlue());
        box.setMaximumSize(component.getPreferredSize().width == 0
                ? new Dimension(90, ROW_HEIGHT)
                : new Dimension(component.getPreferredSize().width, ROW_HEIGHT));
        return box;
    }
}
