package com.terra.framework.nova.llm.multimodal.image;

import lombok.Builder;
import lombok.Data;

/**
 * 图像表示实体类
 */
@Data
@Builder
public class ImageRepresentation {

    /**
     * 图像的Base64表示
     */
    private String base64Data;

    /**
     * 图像URL
     */
    private String url;

    /**
     * 图像格式
     */
    private ImageFormat format;

    /**
     * 宽度
     */
    private int width;

    /**
     * 高度
     */
    private int height;

    /**
     * 图像格式枚举
     */
    public enum ImageFormat {
        /**
         * JPEG格式
         */
        JPEG,

        /**
         * PNG格式
         */
        PNG,

        /**
         * GIF格式
         */
        GIF,

        /**
         * WEBP格式
         */
        WEBP
    }
} 