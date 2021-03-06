package duke.io;

import static duke.util.MagicStrings.ERROR_INVALID_COMMAND;
import static duke.util.StringCleaner.cleanAndLowerString;

import duke.command.Command;
import duke.exception.DuchessException;

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
        String formattedCommand = cleanAndLowerString(command.split("\\s", 2)[0]);
        // Solution below adapted from https://stackoverflow.com/a/4198066
        for (Command cmd : Command.values()) {
            if (cmd.hasCommand(formattedCommand)) {
                return cmd;
            }
        }
        throw new DuchessException(ERROR_INVALID_COMMAND);

    }
}
