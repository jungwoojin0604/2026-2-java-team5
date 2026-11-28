package Gui;

import manager.ThemeManager;
import model.Theme;

public class ThemeTest {
    public static void main(String[] args) {

        ThemeManager themeManager = new ThemeManager();

        System.out.println("전체 테마: "
                + themeManager.getAllThemes());

        System.out.println("초기 테마: "
                + themeManager.getCurrentTheme());

        // 쿠 테마 선택
        themeManager.setCurrentTheme("쿠");

        Theme theme = themeManager.getCurrentTheme();

        System.out.println("변경된 테마: " + theme);
        System.out.println("배경색: " + theme.getBackgroundColor());
        System.out.println("강조색: " + theme.getAccentColor());
        System.out.println("폰트: " + theme.getFont());
        System.out.println("이미지 경로: " + theme.getImagePath());

        // 이미지 파일 존재 여부 확인
        System.out.println("이미지 존재 여부: "
                + new java.io.File(theme.getImagePath()).exists());

        // 기본 테마로 복귀
        themeManager.setCurrentTheme("기본");

        System.out.println("최종 테마: "
                + themeManager.getCurrentTheme());
    }
}