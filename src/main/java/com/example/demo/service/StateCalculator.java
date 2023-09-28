package com.example.demo.service;

import com.example.demo.domain.MoveRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.util.ArrayList;
import java.util.List;

public class StateCalculator {

    static State calculateNextState(MoveRequest request) {

        if (request == null) {
            throw new IllegalArgumentException("MoveRequest should not be null");
        }

        try {
            return getState(request);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Couldn't parse state.");
        }
    }

    private static State getState(MoveRequest request) throws JsonProcessingException {

        var objectMapper = new ObjectMapper();

        String move = request.getMove();
        String previousState = request.getState();
        String side = request.getSide();

        JsonNode jsonNode = objectMapper.readTree(previousState);
        ArrayNode black = (ArrayNode) jsonNode.get("black");
        ArrayNode white = (ArrayNode) jsonNode.get("white");

        List<Long> blackList = nodeToList(black);
        List<Long> whiteList = nodeToList(white);

        String[] moveSplit = move.split("-");

        if (side.equals("white")) {
            whiteList.remove(Long.valueOf(moveSplit[0]));
            whiteList.add(Long.valueOf(moveSplit[1]));
        }

        if (side.equals("black")) {
            blackList.remove(Long.valueOf(moveSplit[0]));
            blackList.add(Long.valueOf(moveSplit[1]));
        }

        return new State(blackList, whiteList);
    }

    private static List<Long> nodeToList(ArrayNode black) {
        List<Long> blackList = new ArrayList<>();
        black.forEach(b -> blackList.add(b.longValue()));
        return blackList;
    }
}
