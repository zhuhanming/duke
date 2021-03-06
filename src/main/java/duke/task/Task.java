package duke.task;

import static duke.util.MagicStrings.ERROR_CANNOT_UNDO;

import java.time.LocalDateTime;

import duke.exception.DuchessException;

/**
 * The {@code Task} class creates a task with a description and isCompleted state.
 */
public class Task implements Cloneable {
    protected boolean isCompleted;
    protected String description;

    protected LocalDateTime creationTime;
    protected LocalDateTime completionTime;

    /**
     * Initialises the {@code Task} instance with its description.
     *
     * @param description Written description of the task.
     */
    public Task(String description) {
        this.description = description;
        this.creationTime = LocalDateTime.now();
        this.isCompleted = false;
    }

    /**
     * Initialises the {@code Task} instance with all of its information. Mainly used
     * by {@code Storage} to regenerate instances.
     *
     * @param description    Written description of the task.
     * @param isCompleted    {@code boolean} value indicating whether the task is completed.
     * @param creationTime   {@code LocalDateTime} object indicating the time of creation of
     *                       the task.
     * @param completionTime {@code LocalDateTime} object indicating the time of
     *                       completion of the task.
     */
    public Task(String description, boolean isCompleted, LocalDateTime creationTime, LocalDateTime completionTime) {
        this.description = description;
        this.isCompleted = isCompleted;
        this.creationTime = creationTime;
        this.completionTime = completionTime;
    }

    private String getStatusIcon() {
        return (this.isCompleted ? "\u2713" : "\u2718"); // tick or cross depending on isCompleted
    }

    @Override
    public String toString() {
        return "[" + this.getStatusIcon() + "] " + this.description;
    }

    @Override
    protected Object clone() throws DuchessException {
        Task clonedTask;
        try {
            clonedTask = (Task) super.clone();
            clonedTask.description = this.description; // Safe due to immutability of strings.
            clonedTask.isCompleted = this.isCompleted;
            clonedTask.creationTime = this.creationTime; // Safe due to immutability of LocalDateTime
            clonedTask.completionTime = this.completionTime;
            return clonedTask;
        } catch (CloneNotSupportedException e) {
            throw new DuchessException(ERROR_CANNOT_UNDO);
        }
    }

    /**
     * Toggles the completion status of the task.
     */
    public void completeTask() {
        this.isCompleted = true;
        this.completionTime = LocalDateTime.now();
    }

    /**
     * Returns a {@code boolean} value that indicates the completion status of the task.
     *
     * @return Completion status of the task.
     */
    public boolean isCompleted() {
        return isCompleted;
    }

    /**
     * Returns a description of the task.
     *
     * @return Description of the task.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the completion time of the task.
     *
     * @return Time of completion.
     */
    public LocalDateTime getCompletionTime() {
        return completionTime;
    }
}
