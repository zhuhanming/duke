package duke.io;

import duke.command.Command;
import duke.exception.DuchessException;

import static duke.util.MagicStrings.ERROR_INVALID_COMMAND;

/**
 * The {@code Parser} class helps to parse given user inputs into a
 * {@code Command} of the appropriate type.
 */
public class Parser {
    /**
     * Parses a given {@code String} into a {@code Command} of
     * the appropriate type.
     *
     * @param command Entire command to be processed.
     * @return Command type of the given command.
     * @throws DuchessException If the command is not recognised.
     */
    public static Command parse(String command) throws DuchessException {
        try {
            return Command.valueOf(command.split("\\s", 2)[0].toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new DuchessException(ERROR_INVALID_COMMAND);
        }
    }
}