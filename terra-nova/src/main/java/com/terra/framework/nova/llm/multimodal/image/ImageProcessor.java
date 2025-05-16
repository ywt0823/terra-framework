package com.terra.framework.nova.llm.multimodal.image;

import java.io.InputStream;
import java.nio.file.Path;

/**
 * 多模态图像处理器接口
 * <p>
 * 用于处理图像以便于与大模型进行交互
 * </p>
 */
public interface ImageProcessor {

    /**
     * 从路径加载图像
     *
     * @param imagePath 图像文件路径
     * @return 图像表示形式
     */
    ImageRepresentation processFromPath(Path imagePath);

    /**
     * 从InputStream加载图像
     *
     * @param imageStream 图像流
     * @return 图像表示形式
     */
    ImageRepresentation processFromStream(InputStream imageStream);

    /**
     * 从Base64字符串加载图像
     *
     * @param base64Image Base64编码的图像
     * @return 图像表示形式
     */
    ImageRepresentation processFromBase64(String base64Image);

    /**
     * 从图像URL加载图像
     *
     * @param imageUrl 图像URL
     * @return 图像表示形式
     */
    ImageRepresentation processFromUrl(String imageUrl);
} 