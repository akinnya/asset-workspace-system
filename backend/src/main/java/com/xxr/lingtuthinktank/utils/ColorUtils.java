package com.xxr.lingtuthinktank.utils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * 颜色工具类
 * 用于提取图片主色调及分类
 */
public class ColorUtils {

    /**
     * 获取图片主色调 (Hex)
     */
    public static String getMainColor(File file) {
        try {
            BufferedImage image = ImageIO.read(file);
            // 简单取中心点或压缩取样
            // 这里取巧：压缩到 1x1
            BufferedImage resized = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = resized.createGraphics();
            g.drawImage(image, 0, 0, 1, 1, null);
            g.dispose();

            int rgb = resized.getRGB(0, 0);
            return String.format("#%06X", (0xFFFFFF & rgb));
        } catch (Exception e) {
            return "#FFFFFF"; // 默认白
        }
    }

    /**
     * 将 Hex 颜色归类到标准色系
     * 支持：红、橙、黄、绿、青、蓝、紫、粉、黑、白、灰、棕
     */
    public static String classifyColor(String hexColor) {
        if (hexColor == null || !hexColor.startsWith("#")) {
            return "其他";
        }

        Color color = Color.decode(hexColor);
        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        float hue = hsb[0] * 360;
        float saturation = hsb[1] * 100;
        float brightness = hsb[2] * 100;

        // 黑白灰判断
        if (brightness < 15)
            return "黑色";
        if (brightness > 90 && saturation < 10)
            return "白色";
        if (saturation < 10)
            return "灰色";

        // 下面是色相环判断
        // Red: 0-10, 345-360
        // Orange: 10-35
        // Yellow: 35-65
        // Green: 65-165
        // Cyan: 165-190
        // Blue: 190-260
        // Purple: 260-315
        // Pink: 315-345

        if (hue >= 0 && hue < 10)
            return "红色";
        if (hue >= 10 && hue < 35)
            return "橙色";
        if (hue >= 35 && hue < 65)
            return "黄色";
        if (hue >= 65 && hue < 165)
            return "绿色";
        if (hue >= 165 && hue < 190)
            return "青色";
        if (hue >= 190 && hue < 260)
            return "蓝色";
        if (hue >= 260 && hue < 315)
            return "紫色";
        if (hue >= 315 && hue < 345)
            return "粉色";
        if (hue >= 345 && hue <= 360)
            return "红色";

        return "其他";
    }
}
