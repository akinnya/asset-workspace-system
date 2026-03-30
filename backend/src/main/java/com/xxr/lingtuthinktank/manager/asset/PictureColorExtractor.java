package com.xxr.lingtuthinktank.manager.asset;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * 图片颜色提取工具
 */
@Slf4j
@Component
public class PictureColorExtractor {

    /**
     * 从图片文件中提取主色调
     *
     * @param imageFile 图片文件
     * @return 主色调的十六进制值（如 #FF5733）
     */
    public String extractMainColor(File imageFile) {
        try {
            BufferedImage image = ImageIO.read(imageFile);
            return extractMainColorFromImage(image);
        } catch (IOException e) {
            log.error("读取图片文件失败", e);
            return null;
        }
    }

    /**
     * 从图片URL中提取主色调
     *
     * @param imageUrl 图片URL
     * @return 主色调的十六进制值
     */
    public String extractMainColor(String imageUrl) {
        try {
            URL url = new URL(imageUrl);
            BufferedImage image = ImageIO.read(url);
            return extractMainColorFromImage(image);
        } catch (IOException e) {
            log.error("读取图片URL失败: {}", imageUrl, e);
            return null;
        }
    }

    /**
     * 从输入流中提取主色调
     *
     * @param inputStream 图片输入流
     * @return 主色调的十六进制值
     */
    public String extractMainColor(InputStream inputStream) {
        try {
            BufferedImage image = ImageIO.read(inputStream);
            return extractMainColorFromImage(image);
        } catch (IOException e) {
            log.error("读取图片流失败", e);
            return null;
        }
    }

    /**
     * 从BufferedImage中提取主色调
     *
     * @param image BufferedImage对象
     * @return 主色调的十六进制值
     */
    private String extractMainColorFromImage(BufferedImage image) {
        if (image == null) {
            return null;
        }

        // 缩小图片以提高性能
        int targetWidth = 100;
        int targetHeight = (int) ((double) image.getHeight() / image.getWidth() * targetWidth);
        BufferedImage resizedImage = resizeImage(image, targetWidth, targetHeight);

        // 统计颜色频率
        Map<Integer, Integer> colorCountMap = new HashMap<>();
        int width = resizedImage.getWidth();
        int height = resizedImage.getHeight();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = resizedImage.getRGB(x, y);
                // 忽略透明像素
                int alpha = (rgb >> 24) & 0xFF;
                if (alpha < 128) {
                    continue;
                }
                // 简化颜色（减少颜色数量）
                int simplifiedRgb = simplifyColor(rgb);
                colorCountMap.merge(simplifiedRgb, 1, Integer::sum);
            }
        }

        // 找出出现次数最多的颜色
        int mainColor = 0;
        int maxCount = 0;
        for (Map.Entry<Integer, Integer> entry : colorCountMap.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                mainColor = entry.getKey();
            }
        }

        // 转换为十六进制
        return rgbToHex(mainColor);
    }

    /**
     * 简化颜色（将相近的颜色归为一类）
     * 通过减少颜色精度来合并相似颜色
     */
    private int simplifyColor(int rgb) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;

        // 将颜色值简化到 16 级（0-255 => 0-15）
        r = (r / 16) * 16;
        g = (g / 16) * 16;
        b = (b / 16) * 16;

        return (r << 16) | (g << 8) | b;
    }

    /**
     * 将RGB值转换为十六进制字符串
     */
    private String rgbToHex(int rgb) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        return String.format("#%02X%02X%02X", r, g, b);
    }

    /**
     * 缩放图片
     */
    private BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = resizedImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        g2d.dispose();
        return resizedImage;
    }

    /**
     * 计算两个颜色之间的相似度（0-1之间，1表示完全相同）
     *
     * @param color1 颜色1（十六进制，如 #FF5733）
     * @param color2 颜色2（十六进制，如 #FF5733）
     * @return 相似度（0-1）
     */
    public double calculateColorSimilarity(String color1, String color2) {
        if (color1 == null || color2 == null) {
            return 0;
        }

        int rgb1 = hexToRgb(color1);
        int rgb2 = hexToRgb(color2);

        int r1 = (rgb1 >> 16) & 0xFF;
        int g1 = (rgb1 >> 8) & 0xFF;
        int b1 = rgb1 & 0xFF;

        int r2 = (rgb2 >> 16) & 0xFF;
        int g2 = (rgb2 >> 8) & 0xFF;
        int b2 = rgb2 & 0xFF;

        // 计算欧几里得距离
        double distance = Math.sqrt(Math.pow(r1 - r2, 2) + Math.pow(g1 - g2, 2) + Math.pow(b1 - b2, 2));
        // 最大距离是 sqrt(255^2 * 3) ≈ 441.67
        double maxDistance = Math.sqrt(3 * Math.pow(255, 2));

        return 1 - (distance / maxDistance);
    }

    /**
     * 将十六进制颜色转换为RGB值
     */
    private int hexToRgb(String hex) {
        if (hex.startsWith("#")) {
            hex = hex.substring(1);
        }
        return Integer.parseInt(hex, 16);
    }
}
