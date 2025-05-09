package com.breens.todochamp.feature_tasks.ui.state

import com.breens.todochamp.data.model.Task

// This defines all the states that our UI can be in
data class TasksScreenUiState(
    val isLoading: Boolean = false,
    val tasks: List<Task> = emptyList(),
    val errorMessage: String? = null,
    val isShowAddTaskDialog: Boolean = false,
    val isShowUpdateTaskDialog: Boolean = false,
    val taskToBeUpdated: Task? = null,
    val currentTextFieldTitle: String = "",
    val currentTextFieldBody: String = ""
)
