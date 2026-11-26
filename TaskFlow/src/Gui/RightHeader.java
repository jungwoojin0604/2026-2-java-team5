package Gui;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

/**
 * 오른쪽 화면 맨 위의 50px 바 (Figma panel2 / tabPanel)
 *
 * <p>왼쪽: 큰 제목(날짜 또는 프로젝트명, 32pt) + 회색 보조 정보(16pt)</p>
 *
 * <p>오른쪽: + 버튼(할 일 추가)과 검색칸</p>
 */
public class RightHeader extends JPanel {
    private final JLabel titleLabel;
    private final JLabel infoLabel;
    private final JButton addButton;
    private final UiKit.SearchBox searchBox;

    public RightHeader() {
        super(new BorderLayout());
        setBackground(Theme.HEADER_BG);
        setPreferredSize(new Dimension(780, 50));
        setBorder(BorderFactory.createEmptyBorder(0, 17, 0, 8));

        // 제목 + 보조 정보
        titleLabel = UiKit.label(" ", Theme.bold(32), Color.BLACK);
        infoLabel = UiKit.label("", Theme.plain(16), Theme.TEXT_GRAY);
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.X_AXIS));
        titlePanel.setOpaque(false);
        titlePanel.add(titleLabel);
        titlePanel.add(Box.createHorizontalStrut(14));
        titlePanel.add(infoLabel);
        add(titlePanel, BorderLayout.CENTER);

        // + 버튼 (오른쪽 끝에서 검색칸 167px + 간격 11px 왼쪽)
        addButton = UiKit.plusButton("할 일 추가");
        searchBox = new UiKit.SearchBox();
        JPanel tools = new JPanel();
        tools.setLayout(new BoxLayout(tools, BoxLayout.X_AXIS));
        tools.setOpaque(false);
        tools.add(addButton);
        tools.add(Box.createHorizontalStrut(11));
        tools.add(searchBox);
        JPanel toolsWrap = new JPanel(new java.awt.GridBagLayout());
        toolsWrap.setOpaque(false);
        toolsWrap.add(tools);
        add(toolsWrap, BorderLayout.EAST);
    }

    /** 제목과 보조 정보 바꾸기 */
    public void setTitle(String title, String info) {
        titleLabel.setText(title);
        infoLabel.setText(info == null ? "" : info);
    }

    public JButton getAddButton() {
        return addButton;
    }

    public UiKit.SearchBox getSearchBox() {
        return searchBox;
    }
}
