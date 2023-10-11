package com.example.demo.service;

import com.example.demo.domain.MoveRequest;
import com.example.demo.domain.State;
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

        List<Integer> blackList = nodeToList(black);
        List<Integer> whiteList = nodeToList(white);

        String[] moveSplit = move.split("-");

        if (side.equals("white")) {
            whiteList.remove(Integer.valueOf(moveSplit[0]));
            whiteList.add(Integer.valueOf(moveSplit[1]));
        }

        if (side.equals("black")) {
            blackList.remove(Integer.valueOf(moveSplit[0]));
            blackList.add(Integer.valueOf(moveSplit[1]));
        }

        return new State(blackList, whiteList);
    }

    private static List<Integer> nodeToList(ArrayNode black) {
        List<Integer> blackList = new ArrayList<>();
        black.forEach(b -> blackList.add(b.intValue()));
        return blackList;
    }

    public static State initialState() {
        return new State(
                List.of(1,2,3,4,5,6,7,8,9,10,11,12),
                List.of(21,22,23,24,25,26,27,28,29,30,31,32)
        );
    }
}
