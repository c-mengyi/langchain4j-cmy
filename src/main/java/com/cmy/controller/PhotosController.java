package com.cmy.controller;

import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversation;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationParam;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationResult;
import com.alibaba.dashscope.common.MultiModalMessage;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.exception.UploadFileException;
import com.cmy.entity.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.commonmark.Extension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.cmy.entity.PhotoFormDTO;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import org.commonmark.node.Document;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

@Tag(name = "chen")
@RestController
@RequestMapping("/youxi")
public class PhotosController {

    @Operation(summary = "分析多张图片并按照模板输出")
    @PostMapping(value = "/photos")
    public Result handleMultiImageAnalysis(@RequestBody PhotoFormDTO dto) throws NoApiKeyException, UploadFileException {
        // 1. 读取系统提示词（从resources目录的子文件夹）
        String systemPrompt = readResourceFile("prompts/youxi-prompt-template.txt");

        // 2. 构建系统消息
        MultiModalMessage systemMessage = MultiModalMessage.builder()
                .role(Role.SYSTEM.getValue())
                .content(Arrays.asList(Collections.singletonMap("text", systemPrompt)))
                .build();

        // 3. 定义图片URL列表（支持多张图片）
        // 校验输入
        if (dto.getImageUrls() == null || dto.getImageUrls().isEmpty()) {
            return Result.error("至少需要提供一张图片URL");
        }

        // 过滤无效URL
        List<String> validUrls = dto.getImageUrls().stream()
                .filter(url -> url != null && url.startsWith("http"))
                .collect(Collectors.toList());

        if (validUrls.isEmpty()) {
            return Result.error("所有提供的图片URL均无效");
        }

        // 4. 构建用户消息（文本+图片列表，支持综合分析提示）
        String userPrompt = dto.getTextInput() != null ? dto.getTextInput().trim() : "以下是同一物品的图片，请按模板分析图片：";

        MultiModalMessage userMessage = MultiModalMessage.builder()
                .role(Role.USER.getValue())
                .content(buildMultiImageContent(validUrls, userPrompt))
                .build();

        // 5. 调用多模态模型（qwen-vl-max-latest支持多图）
        MultiModalConversation conv = new MultiModalConversation();
        MultiModalConversationResult result = conv.call(
                MultiModalConversationParam.builder()
                        .apiKey(System.getenv("DASHSCOPE_API_KEY"))
                        .model("qwen-vl-max-latest")
                        .messages(Arrays.asList(systemMessage, userMessage))
                        .build()
        );

        // 6. 输出综合分析结果
        return Result.success(extractResultText(result));

    }
//    private String extractResultText(MultiModalConversationResult result) {
//        return result.getOutput().getChoices().stream()
//                .map(choice -> choice.getMessage().getContent())
//                .flatMap(List::stream)
//                .filter(content -> content.containsKey("text"))
//                .map(content -> (String) content.get("text"))
//                .reduce((a, b) -> a + "\n" + b) // 合并多段文本（如换行）
//                .orElse("");
//    }

    private String extractResultText(MultiModalConversationResult result) {
        // 1. 提取所有文本内容
        List<String> contents = result.getOutput().getChoices().stream()
                .map(choice -> choice.getMessage().getContent())
                .flatMap(List::stream)
                .filter(content -> content.containsKey("text"))
                .map(content -> (String) content.get("text"))
                .collect(Collectors.toList());

        // 2. 配置Markdown解析器（支持表格）
        List<Extension> extensions = Arrays.asList(TablesExtension.create());
        Parser parser = Parser.builder()
                .extensions(extensions)
                .build();
        HtmlRenderer renderer = HtmlRenderer.builder()
                .extensions(extensions) // 启用表格扩展
                .escapeHtml(false) // 不转义HTML标签（确保表格正确渲染）
                .build();

        // 3. 合并解析后的HTML，去除多余换行符
        StringBuilder combinedHtml = new StringBuilder();
        for (String content : contents) {
            Document document = (Document) parser.parse(content);
            String html = renderer.render(document);
            // 去除HTML中的换行符（保留段落结构）
            html = html.replaceAll("\\n", "");
            combinedHtml.append(html);
        }
        return combinedHtml.toString();
    }
    /**
     * 构建包含多张图片和文本的content列表
     */
    private static List<java.util.Map<String, Object>> buildMultiImageContent(List<String> imageUrls, String textPrompt) {
        List<java.util.Map<String, Object>> contentList = new java.util.ArrayList<>();
        contentList.add(Collections.singletonMap("text", textPrompt)); // 先添加文本提示
        imageUrls.forEach(url -> contentList.add(Collections.singletonMap("image", url))); // 按顺序添加所有图片
        return contentList;
    }

    /**
     * 读取resources目录下的文件（支持子文件夹）
     */
    private static String readResourceFile(String resourcePath) {
        try (InputStream inputStream = PhotosController.class.getResourceAsStream("/" + resourcePath)) {
            if (inputStream == null) {
                throw new IllegalArgumentException("文件未找到：" + resourcePath);
            }
            return new Scanner(inputStream, "UTF-8").useDelimiter("\\A").next();
        } catch (IOException e) {
            throw new RuntimeException("读取文件失败：" + e.getMessage());
        }
    }
}
