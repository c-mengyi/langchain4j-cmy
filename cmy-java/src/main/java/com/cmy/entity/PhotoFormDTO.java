package com.cmy.entity;

import java.util.List;

public class PhotoFormDTO {
    private String textInput; // 用户补充文字说明
    private List<String> imageUrls; // 多图片URL列表

    // Getter/Setter

    public String getTextInput() {
        return textInput;
    }

    public void setTextInput(String textInput) {
        this.textInput = textInput;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }
}