package duke.command;

import java.util.ArrayList;
import java.util.Arrays;

import duke.save.SaveStateStack;
import duke.storage.Storage;
import duke.task.TaskList;
import duke.ui.Ui;
import duke.util.QuintFunction;

/**
 * The {@code Command} enum contains all of the different types of commands
 * recognised by {@code Duchess}.
 */
public enum Command {
    // Implementation below adapted from my CS2030 code, which was previously
    // adapted from https://www.youtube.com/watch?v=F02LKnWJWF4 at around 40:00
    TODO(TaskCreationHandler::handleTodoCommand, "todo", "t"),
    EVENT(TaskCreationHandler::handleEventCommand, "event", "e"),
    DEADLINE(TaskCreationHandler::handleDeadlineCommand, "deadline", "dl"),
    LIST(TaskListCommandHandler::handleListCommand, "list", "l", "li"),
    DONE(TaskListCommandHandler::handleDoneCommand, "done", "d", "complete"),
    FIND(TaskListCommandHandler::handleFindCommand, "find", "f", "search"),
    DELETE(TaskListCommandHandler::handleDeleteCommand, "delete", "del"),
    SNOOZE(TaskListCommandHandler::handleSnoozeCommand, "snooze"),
    SORT(TaskListCommandHandler::handleSortCommand, "sort", "s"),
    HELP(AdminCommandHandler::handleHelpCommand, "help", "h"),
    UNDO(AdminCommandHandler::handleUndoCommand, "undo"),
    ARCHIVE(TaskListCommandHandler::handleArchiveCommand, "archive", "arc", "a"),
    STATISTICS(AdminCommandHandler::handleStatsCommand, "statistics", "statistic", "stat", "stats"),
    BYE(AdminCommandHandler::handleByeCommand, "bye", "exit", "quit");

    /**
     * Executes the command. Use {@code execute.apply} to run the function.
     */
    public final QuintFunction<String, TaskList, Ui, Storage, SaveStateStack> execute;

    /**
     * Contains all valid user inputs that maps to this type of command.
     */
    public final ArrayList<String> commands;

    /**
     * Initialises the Command enum type with the appropriate {@code execute}
     * function.
     *
     * @param execute The {@code QuadFunction} for the Command type.
     */
    // Solution below adapted from https://stackoverflow.com/a/4198066
    Command(QuintFunction<String, TaskList, Ui, Storage, SaveStateStack> execute, String... commands) {
        this.execute = execute;
        this.commands = new ArrayList<>(Arrays.asList(commands));
    }

    public boolean hasCommand(String command) {
        return this.commands.contains(command);
    }
}
