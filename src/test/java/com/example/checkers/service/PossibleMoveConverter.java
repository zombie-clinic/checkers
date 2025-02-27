package com.example.checkers.service;

import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.converter.ArgumentConversionException;
import org.junit.jupiter.params.converter.ArgumentConverter;

import java.lang.reflect.Parameter;
import java.util.List;

// TODO investigate
public class PossibleMoveConverter implements ArgumentConverter {
    @Override
    public Object convert(Object source, ParameterContext context) throws ArgumentConversionException {
        Parameter parameter = context.getDeclaringExecutable().getParameters()[1];
        return List.of();
    }
}