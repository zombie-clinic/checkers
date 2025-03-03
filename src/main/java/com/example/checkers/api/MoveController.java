package com.example.checkers.api;

import com.example.checkers.domain.Side;
import com.example.checkers.model.MoveRequest;
import com.example.checkers.model.MoveResponse;
import com.example.checkers.service.GameService;
import com.example.checkers.service.MoveService;
import com.example.checkers.service.MoveValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.ResponseEntity.badRequest;
import static org.springframework.http.ResponseEntity.ok;

@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
@RequiredArgsConstructor
@RestController
public class MoveController implements MoveApi {

    private final GameService gameService;

    private final MoveService moveService;

    private final MoveValidator validator;

    @Override
    public ResponseEntity<MoveResponse> getCurrentState(String gameId) {
        if (gameService.isGameValid(gameId)) {
            Side side = gameService.getCurrentSide(gameId);
            return ok(moveService.generateMoveResponse(gameId, side));
        }
        throw new IllegalArgumentException(String.format("Game %s deleted or not started", gameId));
    }

    @Override
    public ResponseEntity<MoveResponse> move(String gameId, MoveRequest moveRequest) {
        Errors errors = new BeanPropertyBindingResult(moveRequest, "move");
        validator.validate(moveRequest, errors);
        if (errors.hasErrors()) {
            return generateErrorResponse(errors);
        }
        MoveResponse moveResponse = moveService.saveMove(
                gameId, moveRequest
        );
        return ok(moveResponse);
    }










    private ResponseEntity<MoveResponse> generateErrorResponse(Errors errors) {
        MoveResponse moveResponse = new MoveResponse();
        moveResponse.setErrors(errors.getAllErrors());
        return badRequest().body(moveResponse);
    }
}
