package Gui;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * 달력 하단에 테마 이미지를 표시하는 패널 클래스
 *
 * <p>이미지 파일을 불러와 패널 크기에 맞게 표시하고
 * 원본 이미지의 가로세로 비율 유지</p>
 *
 * <p>이미지가 지정되지 않은 경우 빈 영역 표시</p>
 */
public class ThemeImagePanel extends JPanel {
    private BufferedImage image;
    /**
     * 테마 이미지 표시 패널 생성
     */
    public ThemeImagePanel() {
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(420, 250));
    }

    /**
     * 지정된 경로의 이미지 불러오기
     *
     * @param imagePath 이미지 파일 경로 (없으면 null)
     * @return 이미지 설정 성공 시 true, 불러오기 실패 시 false
     */
    public boolean setImage(String imagePath) {
        if (imagePath == null || imagePath.isBlank()) {
            image = null;
            repaint();
            return true;
        }
        try {
            BufferedImage loadedImage = ImageIO.read(new File(imagePath));
            if (loadedImage == null) {
                return false;
            }
            image = loadedImage;
            repaint();
            return true;
        } catch (IOException e) {
            System.err.println("이미지 불러오기 실패: " + e.getMessage());
            return false;
        }
    }

    /**
     * 패널 크기에 맞게 이미지 표시
     *
     * <p>원본 이미지의 가로세로 비율을 유지하고
     * 패널 중앙에 이미지 배치</p>
     *
     * @param g 그래픽 객체
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (image == null) {
            return;
        }
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR
        );

        // 패널과 이미지의 크기
        int panelWidth = getWidth();
        int panelHeight = getHeight();
        int imageWidth = image.getWidth();
        int imageHeight = image.getHeight();

        // 원본 비율을 유지하기 위한 확대 및 축소 비율
        double scale = Math.min(
                (double) panelWidth / imageWidth,
                (double) panelHeight / imageHeight
        );

        int width = (int) (imageWidth * scale);
        int height = (int) (imageHeight * scale);

        // 이미지 중앙 배치
        int x = (panelWidth - width) / 2;
        int y = (panelHeight - height) / 2;

        g2.drawImage(image, x, y, width, height, this);
        g2.dispose();
    }
}