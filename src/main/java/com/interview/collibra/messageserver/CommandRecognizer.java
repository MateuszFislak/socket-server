package com.interview.collibra.messageserver;

import com.interview.collibra.messageserver.model.Command;

public class CommandRecognizer {

    public static final String CLIENT_GREETING_MESSAGE = "HI, I AM ";
    public static final String TERMINATION_COMMAND = "BYE MATE!";


    public static Command recognize(String message) {
        if (message.startsWith(CLIENT_GREETING_MESSAGE)) {
            return Command.CLIENT_GREETING;
        }
        if (TERMINATION_COMMAND.equals(message)) {
            return Command.TERMINATION;
        }
        return Command.UNKNOWN;
    }

}
