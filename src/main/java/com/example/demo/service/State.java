package com.example.demo.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;

import java.util.List;

public record State (List<Long> black, List<Long> white) {

    @SneakyThrows
    @Override
    public String toString() {
        ObjectMapper objectMapper = new ObjectMapper();
        var root = objectMapper.createObjectNode();
        var blackArray = objectMapper.createArrayNode();
        black.forEach(blackArray::add);
        var whiteArray = objectMapper.createArrayNode();
        white.forEach(whiteArray::add);
        root.set("black",blackArray);
        root.set("white",whiteArray);
        return objectMapper.writeValueAsString(root);
    }
}
