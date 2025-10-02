package com.task.management.exception;

import lombok.AllArgsConstructor;

import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@AllArgsConstructor
public enum MessagePatternReplacement {

    BLANK_STRING("^(?!\\s*$).+",
            str -> replace(str, "^(?!\\s*$).+"," must not be blank" ));

    private final String pattern;
    private final Function<String, String> replacementLogic;

    public static String replace(String message) {
        var result =  Stream.of(MessagePatternReplacement.values())
                .filter(p -> message.contains(p.pattern))
                .map(p -> p.replacementLogic.apply(message))
                .collect(Collectors.joining(" "));

        return result.isEmpty() ? message : result;
    }

    private static String replace(String message, String pattern, String replacementText) {
        if(message != null && message.contains(pattern)) {
            return "%s:%s".formatted(message.substring(0, message.indexOf(':')), replacementText);
        }
        return message;
    }

}