package com.interview.collibra.messageserver;

import com.interview.collibra.messageserver.model.Command;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CommandRecognizer {

    private final Map<Command, Pattern> patterns = Map.of(
            Command.CLIENT_GREETING, Pattern.compile("^HI, I AM ([a-zA-Z0-9-]+)$"),
            Command.TERMINATION, Pattern.compile("^BYE MATE!$"),
            Command.ADD_NODE, Pattern.compile("^ADD NODE ([a-zA-Z0-9-]+)$"),
            Command.ADD_EDGE, Pattern.compile("^ADD EDGE ([a-zA-Z0-9-]+) ([a-zA-Z0-9-]+) ([0-9]+)$"),
            Command.REMOVE_NODE, Pattern.compile("^REMOVE NODE ([a-zA-Z0-9-]+)$"),
            Command.REMOVE_EDGE, Pattern.compile("^REMOVE EDGE ([a-zA-Z0-9-]+) ([a-zA-Z0-9-]+)$"),
            Command.SHORTEST_PATH, Pattern.compile("^SHORTEST PATH ([a-zA-Z0-9-]+) ([a-zA-Z0-9-]+)$")
    );


    public Pair<Command, List<String>> recognize(String message) {
        return findMatchingPattern(message)
                .map(commandMatcher -> Pair.of(commandMatcher.getLeft(), findMatches(commandMatcher.getRight())))
                .orElse(Pair.of(Command.UNKNOWN, List.of()));
    }

    private List<String> findMatches(Matcher matcher) {
        List<String> matches = new ArrayList<>();
        for (int i = 1; i <= matcher.groupCount(); i++) {
            matches.add(matcher.group(i));
        }
        return matches;
    }

    private Optional<Pair<Command, Matcher>> findMatchingPattern(String message) {
        return patterns.entrySet().stream()
                .map(commandPattern -> Pair.of(commandPattern.getKey(), commandPattern.getValue().matcher(message)))
                .filter(commandMatcher -> commandMatcher.getValue().matches())
                .findFirst();
    }

}
