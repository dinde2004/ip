package duke.command;

import duke.data.Storage;
import duke.exception.DukeException;
import duke.task.Deadline;
import duke.task.Event;
import duke.task.Task;
import duke.task.TaskList;
import duke.task.Todo;
import duke.ui.Ui;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Deals with the user command.
 */
public class Parser {
    /**
     * Processes the user's command.
     *
     * @param command The command user types.
     * @param ui The chatbot's ui.
     * @param taskList The list of tasks.
     * @param storage The storage of tasks.
     * @throws DukeException If the command is invalid.
     */
    public void processCommand(String command, Ui ui, TaskList taskList, Storage storage) throws DukeException {
        if (command.equals("bye")) {
            ui.bye();
        } else if (command.equals("list")) {
            ui.printList(taskList);
        } else if (command.startsWith("mark")) {
            int index = 0;
            for (int i = 5; i < command.length(); ++i) {
                index *= 10;
                index += command.charAt(i) - 48;
            }
            taskList.getTask(index - 1).markAsDone();
            ui.markTaskDone(taskList.getTask(index - 1));
        } else if (command.startsWith("unmark")) {
            int index = 0;
            for (int i = 7; i < command.length(); ++i) {
                index *= 10;
                index += command.charAt(i) - 48;
            }
            taskList.getTask(index - 1).markAsNotDone();
            ui.markTaskNotDone(taskList.getTask(index - 1));
        } else if (command.startsWith("delete")) {
            int index = 0;
            for (int i = 7; i < command.length(); ++i) {
                index *= 10;
                index += command.charAt(i) - 48;
            }
            Task task = taskList.deleteTask(index - 1);
            ui.deleteTask(task, taskList.size());
        } else if (command.startsWith("find")) {
            String str = command.substring(5);
            ArrayList<Task> tasks = taskList.contains(str);
            ui.printMatchingTasks(tasks);
        } else {
            addTask(command, ui, taskList);
        }
        try {
            if (!command.equals("list")) {
                storage.writeFile(taskList);
            }
        } catch (IOException ioe) {
            ui.printError(ioe);
        }
    }

    /**
     * Processes command to add task to the task list.
     * @param task Task to be added to list.
     * @param ui The chatbot's ui.
     * @param taskList The list of tasks.
     * @throws DukeException If the task is invalid.
     */
    public void addTask(String task, Ui ui, TaskList taskList) throws DukeException {
        if (task.startsWith("todo")) {
            if (task.length() < 6) {
                throw new DukeException("☹ OOPS!!! The description of a todo cannot be empty.");
            }
            Todo todo = new Todo(task.substring(5));
            taskList.addTask(todo);
            ui.addTask(todo, taskList.size());
        } else if (task.startsWith("deadline")) {
            if (task.length() < 10) {
                throw new DukeException("cannot be empty." + "☹ OOPS!!! The description of a deadline ");
            }
            String description = "";
            String by = "";
            for (int i = 9; i < task.length(); ++i) {
                if (task.charAt(i) == '/') {
                    description = task.substring(9, i - 1);
                    by = task.substring(i + 4);
                    break;
                }
            }
            Deadline deadline = new Deadline(description, by);
            taskList.addTask(deadline);
            ui.addTask(deadline, taskList.size());
        } else if (task.startsWith("event")) {
            if (task.length() < 7) {
                throw new DukeException("☹ OOPS!!! The description of an event cannot be empty.");
            }
            int slash1 = -1;
            int slash2 = -1;
            for (int i = 0; i < task.length(); ++i) {
                if (task.charAt(i) == '/') {
                    if (slash1 == -1) {
                        slash1 = i;
                    } else {
                        slash2 = i;
                    }
                }
            }
            String description = task.substring(6, slash1 - 1);
            String from = task.substring(slash1 + 6, slash2 - 1);
            String to = task.substring(slash2 + 4);
            Event event = new Event(description, from, to);
            taskList.addTask(event);
            ui.addTask(event, taskList.size());
        } else {
            throw new DukeException("☹ OOPS!!! I'm sorry, but I don't know what that means :-(");
        }
    }
}
