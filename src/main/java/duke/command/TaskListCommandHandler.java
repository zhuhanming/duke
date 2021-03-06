package duke.command;

import static duke.util.MagicStrings.ERROR_COMMAND_MISSING_INDEX;
import static duke.util.MagicStrings.ERROR_COMMAND_TOO_MANY_INDICES;
import static duke.util.MagicStrings.ERROR_INDEX_OUT_OF_BOUNDS;
import static duke.util.MagicStrings.ERROR_INVALID_COMMAND;
import static duke.util.MagicStrings.ERROR_INVALID_SNOOZE_DURATION;
import static duke.util.MagicStrings.ERROR_SNOOZING_NON_DEADLINE;
import static duke.util.MagicStrings.ERROR_SORTING_EMPTY_LIST;
import static duke.util.StringCleaner.cleanAndLowerString;

import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import duke.exception.DuchessException;
import duke.io.DurationParser;
import duke.save.SaveStateStack;
import duke.storage.Storage;
import duke.task.Deadline;
import duke.task.Task;
import duke.task.TaskList;
import duke.ui.Ui;
import duke.util.Pair;

/**
 * The {@code CommandHandler} class contains all static methods to handle
 * commands given the same arguments of command, taskList, ui and storage.
 */
public class TaskListCommandHandler {
    /**
     * Prints out the given {@code TaskList} with the given {@code Ui} instance.
     *
     * @param command        Full user command string.
     * @param taskList       List of tasks.
     * @param ui             Ui instance.
     * @param storage        Storage instance.
     * @param saveStateStack Collection of save states.
     * @return String containing all {@code Task}s.
     */
    static String handleListCommand(String command, TaskList taskList, Ui ui, Storage storage,
                                    SaveStateStack saveStateStack) throws DuchessException {
        assert Command.LIST.hasCommand(cleanAndLowerString(command)); // pre-condition
        return ui.printTaskList(taskList);
    }

    /**
     * Completes a {@code Task} based on the command and given the entire command
     * and the supporting instances.
     *
     * @param command        Full user command string.
     * @param taskList       List of tasks.
     * @param ui             Ui instance.
     * @param storage        Storage instance.
     * @param saveStateStack Collection of save states.
     * @return Success message for completing the task.
     * @throws DuchessException If the list fails to be saved or the index is out of
     *                          bounds or the task is already completed.
     */
    static String handleDoneCommand(String command, TaskList taskList, Ui ui, Storage storage,
                                    SaveStateStack saveStateStack) throws DuchessException {
        int index = getIntegerFromCommand(command);
        checkBoundsOfIndex(index, taskList);

        saveStateStack.saveState(command, taskList); // An immutable copy is made of current state.
        Task taskCompleted = taskList.completeTask(index - 1);
        storage.save(taskList);
        return ui.printTaskCompleted(taskCompleted);
    }

    /**
     * Finds a list of {@code Task}s based on the command and given the entire
     * command and the supporting instances. The list of {@code Task}s are then
     * printed out with the given {@code Ui} instance.
     *
     * @param command        Full user command string.
     * @param taskList       List of tasks.
     * @param ui             Ui instance.
     * @param storage        Storage instance.
     * @param saveStateStack Collection of save states.
     * @return Message containing list of found tasks.
     */
    static String handleFindCommand(String command, TaskList taskList, Ui ui,
                                    Storage storage, SaveStateStack saveStateStack) {
        ArrayList<String> commands = new ArrayList<>(Arrays.asList(command.split("\\s", 2)));
        assert Command.FIND.hasCommand(cleanAndLowerString(commands.get(0))); // pre-condition
        ArrayList<Pair<Task, Integer>> filteredTaskList = taskList.find(cleanAndLowerString(commands.get(1)));
        return ui.printFilteredTaskList(filteredTaskList);
    }

    /**
     * Deletes a {@code Task} based on the command and given the entire command and
     * the supporting instances.
     *
     * @param command        Full user command string.
     * @param taskList       List of tasks.
     * @param ui             Ui instance.
     * @param storage        Storage instance.
     * @param saveStateStack Collection of save states.
     * @return Success message for deletion of task.
     * @throws DuchessException If the list fails to be saved or the index is out of
     *                          bounds.
     */
    static String handleDeleteCommand(String command, TaskList taskList, Ui ui, Storage storage,
                                      SaveStateStack saveStateStack) throws DuchessException {
        ArrayList<String> commands = new ArrayList<>(Arrays.asList(command.split("\\s", 2)));
        assert Command.DELETE.hasCommand(cleanAndLowerString(commands.get(0))); // pre-condition

        // Handle delete all case
        if (commands.size() == 2 && cleanAndLowerString(commands.get(1)).equals("all")) {
            saveStateStack.saveState(command, taskList);
            taskList.removeAllTasks();
            storage.save(taskList);
            return ui.printAllDeleted();
        }
        int index = getIntegerFromCommand(command);
        checkBoundsOfIndex(index, taskList);
        saveStateStack.saveState(command, taskList); // An immutable copy is made of current state.

        Task taskToDelete = taskList.getTask(index - 1);
        taskList.removeTask(index - 1);

        storage.save(taskList);
        return ui.printTaskDeleted(taskToDelete, taskList.size());
    }

    /**
     * Snoozes a {@code Task} based on the command and given the entire command and
     * the supporting instances.
     *
     * @param command        Full user command string.
     * @param taskList       List of tasks.
     * @param ui             Ui instance.
     * @param storage        Storage instance.
     * @param saveStateStack Collection of save states.
     * @return Success message for the snoozing of deadlines.
     * @throws DuchessException If the list fails to be saved or the index is out of
     *                          bounds or the task does not have a deadline to snooze.
     */
    static String handleSnoozeCommand(String command, TaskList taskList, Ui ui, Storage storage,
                                      SaveStateStack saveStateStack) throws DuchessException {
        ArrayList<String> commands = new ArrayList<>(Arrays.asList(command.split("/for", 2)));
        if (commands.size() < 2) {
            throw new DuchessException(ERROR_INVALID_SNOOZE_DURATION);
        }

        assert Command.SNOOZE.hasCommand(cleanAndLowerString(commands.get(0))); // pre-condition

        int index = getIntegerFromCommand(commands.get(0));
        checkBoundsOfIndex(index, taskList);

        Task taskToSnooze = taskList.getTask(index - 1);
        if (!(taskToSnooze instanceof Deadline)) {
            throw new DuchessException(ERROR_SNOOZING_NON_DEADLINE);
        }

        String duration = cleanAndLowerString(commands.get(1));
        TemporalAmount snoozePeriod = DurationParser.parseDuration(duration);
        saveStateStack.saveState(command, taskList); // An immutable copy is made of current state.
        ((Deadline) taskToSnooze).snooze(snoozePeriod);
        storage.save(taskList);
        return ui.printTaskSnoozed(taskToSnooze, DurationParser.parseDurationToString(duration));
    }

    /**
     * Sorts the {@code TaskList} given the entire command and the supporting instances.
     *
     * @param command        Full user command string.
     * @param taskList       List of tasks.
     * @param ui             Ui instance.
     * @param storage        Storage instance.
     * @param saveStateStack Collection of save states.
     * @return Success message after sorting the array.
     * @throws DuchessException If the list is empty and has nothing to sort.
     */
    static String handleSortCommand(String command, TaskList taskList, Ui ui, Storage storage,
                                    SaveStateStack saveStateStack) throws DuchessException {
        assert Command.SORT.hasCommand(cleanAndLowerString(command)); // pre-condition

        if (taskList.size() == 0) {
            throw new DuchessException(ERROR_SORTING_EMPTY_LIST);
        }

        saveStateStack.saveState(command, taskList); // An immutable copy is made of current state.
        taskList.sort();
        storage.save(taskList);
        return ui.printTaskListSorted();
    }

    /**
     * Archives the completed tasks in the current list.
     *
     * @param command        Full user command string.
     * @param taskList       List of tasks.
     * @param ui             Ui instance.
     * @param storage        Storage instance.
     * @param saveStateStack Collection of save states.
     * @return Archive success message.
     * @throws DuchessException Invalid command given.
     */
    static String handleArchiveCommand(String command, TaskList taskList, Ui ui, Storage storage,
                                       SaveStateStack saveStateStack) throws DuchessException {
        ArrayList<String> commands = new ArrayList<>(Arrays.asList(command.split("\\s", 2)));
        assert Command.ARCHIVE.hasCommand(cleanAndLowerString(commands.get(0))); // pre-condition
        if (commands.size() == 2) {
            return handleShowArchive(command, taskList, ui);
        }

        saveStateStack.saveState(command, taskList);
        taskList.archive();
        storage.save(taskList);
        return ui.printTaskListArchived();
    }

    private static Integer getIntegerFromCommand(String command) throws DuchessException {
        ArrayList<String> commands = new ArrayList<>(Arrays.asList(command.split("\\s")));
        checkSizeOfCommands(commands);
        try {
            return Integer.parseInt(commands.get(1).trim());
        } catch (NumberFormatException e) {
            throw new DuchessException(ERROR_INVALID_COMMAND);
        }
    }

    private static void checkSizeOfCommands(ArrayList<String> commands) throws DuchessException {
        if (commands.size() < 2) {
            throw new DuchessException(ERROR_COMMAND_MISSING_INDEX);
        }
        if (commands.size() > 2) {
            throw new DuchessException(ERROR_COMMAND_TOO_MANY_INDICES);
        }
    }

    private static void checkBoundsOfIndex(int index, TaskList taskList) throws DuchessException {
        boolean isIndexTooLow = index < 0;
        boolean isIndexTooHigh = index > taskList.size();
        if (isIndexTooLow || isIndexTooHigh) {
            throw new DuchessException(ERROR_INDEX_OUT_OF_BOUNDS);
        }
    }

    private static String handleShowArchive(String command, TaskList taskList, Ui ui) throws DuchessException {
        ArrayList<String> commands = new ArrayList<>(Arrays.asList(command.split("\\s")));
        String secondaryCommand = cleanAndLowerString(commands.get(1));
        ArrayList<String> validShowCommands = new ArrayList<>(List.of("show", "view", "list"));
        if (!validShowCommands.contains(secondaryCommand)) {
            throw new DuchessException(ERROR_INVALID_COMMAND);
        }
        return ui.printArchive(taskList);
    }
}
