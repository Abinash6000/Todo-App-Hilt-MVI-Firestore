package com.breens.todochamp.feature_tasks.ui.events

import com.breens.todochamp.data.model.Task

// This defines the events that will be sent from the UI to the ViewModel
sealed class TaskScreenUiEvents {
    object GetTasks : TaskScreenUiEvents()

    data class AddTask(val title: String, val body: String) : TaskScreenUiEvents()

    object UpdateTask : TaskScreenUiEvents()

    data class DeleteTask(val taskId: String) : TaskScreenUiEvents()

    data class OnChangeTaskTitle(val title: String) : TaskScreenUiEvents()

    data class OnChangeTaskBody(val body: String) : TaskScreenUiEvents()

    data class OnChangeAddTaskDialogState(val show: Boolean) : TaskScreenUiEvents()

    data class OnChangeUpdateDialogState(val show: Boolean) : TaskScreenUiEvents()

    data class SetTaskToBeUpdated(val taskToBeUpdated: Task) : TaskScreenUiEvents()
}