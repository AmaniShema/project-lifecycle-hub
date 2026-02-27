package com.amani.projecthub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateNoteRequest {

    @NotBlank(message = "Note content is required")
    @Size(max = 2000, message = "Note must not exceed 2000 characters")
    private String content;

    public CreateNoteRequest() {}

    public String getContent() { return content; }

    public void setContent(String content) { this.content = content; }
}