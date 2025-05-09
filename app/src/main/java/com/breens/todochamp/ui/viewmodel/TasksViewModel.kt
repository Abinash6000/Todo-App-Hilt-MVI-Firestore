package com.breens.todochamp.feature_tasks.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.breens.todochamp.data.repositories.TaskRepository
import com.breens.todochamp.feature_tasks.ui.events.TaskScreenUiEvents
import com.breens.todochamp.feature_tasks.ui.side_effects.TaskScreenSideEffects
import com.breens.todochamp.feature_tasks.ui.state.TasksScreenUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import com.breens.todochamp.common.Result
import com.breens.todochamp.data.model.Task
import javax.inject.Inject

@HiltViewModel
class TasksViewModel @Inject constructor(
    private val repository: TaskRepository
) : ViewModel() {

    // In MVI it's recommended to have only one state object
    private val _state: MutableStateFlow<TasksScreenUiState> =
        MutableStateFlow(TasksScreenUiState())
    val state: StateFlow<TasksScreenUiState> = _state.asStateFlow()

    // because we want the side effects to be consumed only once so we make it a channel
    private val _effect: Channel<TaskScreenSideEffects> = Channel()
    val effect = _effect.receiveAsFlow()

    // function to send events to the ViewModel
    fun sendEvent(event: TaskScreenUiEvents) {
        reduce(event = event, oldState = state.value)
    }

    private fun setState(newState: TasksScreenUiState) {
        _state.value = newState
    }

    private fun setEffect(builder: () -> TaskScreenSideEffects) {
        val effectValue = builder()
        viewModelScope.launch {
            _effect.send(effectValue)
        }
    }

    init {
        sendEvent(event = TaskScreenUiEvents.GetTasks)
    }

    /* this reduce function will be called by the sendEvent function
     * and it will decide what to do with the event
     * and it will also update the old state with the new state
     */
    private fun reduce(event: TaskScreenUiEvents, oldState: TasksScreenUiState) {
        when(event) {
            is TaskScreenUiEvents.AddTask -> {
                addTask(
                    oldState = oldState,
                    title = event.title,
                    body = event.body
                )
            }
            is TaskScreenUiEvents.DeleteTask -> {
                deleteTask(
                    oldState = oldState,
                    taskId = event.taskId
                )
            }
            TaskScreenUiEvents.GetTasks -> {
                getAllTasks(oldState = oldState)
            }
            is TaskScreenUiEvents.OnChangeAddTaskDialogState -> {
                onChangesAddTaskDialogState(
                    oldState = oldState,
                    show = event.show
                )
            }
            is TaskScreenUiEvents.OnChangeTaskBody -> {
                onChangeTaskBody(oldState = oldState, body = event.body)
            }
            is TaskScreenUiEvents.OnChangeTaskTitle -> {
                onChangeTaskTitle(oldState = oldState, title = event.title)
            }
            is TaskScreenUiEvents.OnChangeUpdateDialogState -> {
                onChangeUpdateTaskDialogState(
                    oldState = oldState,
                    show = event.show
                )
            }
            is TaskScreenUiEvents.SetTaskToBeUpdated -> {
                setTaskToBeUpdated(
                    oldState = oldState,
                    taskToBeUpdated = event.taskToBeUpdated
                )
            }
            TaskScreenUiEvents.UpdateTask -> {
                updateTask(
                    oldState = oldState
                )
            }
        }
    }

    private fun updateTask(oldState: TasksScreenUiState) {
        viewModelScope.launch {
            setState(
                oldState.copy(
                    isLoading = true
                )
            )

            val title = oldState.currentTextFieldTitle
            val body = oldState.currentTextFieldBody
            val taskToBeUpdated = oldState.taskToBeUpdated

            when(val result = repository.updateTask(
                taskId = taskToBeUpdated?.taskId ?: "",
                title = title,
                body = body
            )) {
                is Result.Failure -> {
                    setState(
                        oldState.copy(
                            isLoading = false
                        )
                    )

                    val errorMessage = result.exception.message ?: "An error occurred while updating task"

                    setEffect { TaskScreenSideEffects.ShowSnackBarMessage(message = errorMessage) }
                }

                is Result.Success -> {
                    setState(
                        oldState.copy(
                            isLoading = false,
                            currentTextFieldTitle = "",
                            currentTextFieldBody = ""
                        )
                    )

                    sendEvent(event = TaskScreenUiEvents.OnChangeUpdateDialogState(show = false))

                    setEffect { TaskScreenSideEffects.ShowSnackBarMessage(message = "Task Updated Successfully") }

                    sendEvent(TaskScreenUiEvents.GetTasks)
                }
            }
        }
    }

    private fun setTaskToBeUpdated(oldState: TasksScreenUiState, taskToBeUpdated: Task) {
        setState(
            oldState.copy(
                taskToBeUpdated = taskToBeUpdated
            )
        )
    }

    private fun onChangeUpdateTaskDialogState(oldState: TasksScreenUiState, show: Boolean) {
        setState(
            oldState.copy(
                isShowUpdateTaskDialog = show
            )
        )
    }

    private fun onChangeTaskTitle(oldState: TasksScreenUiState, title: String) {
        setState(
            oldState.copy(
                currentTextFieldTitle = title
            )
        )
    }

    private fun onChangeTaskBody(oldState: TasksScreenUiState, body: String) {
        setState(
            oldState.copy(
                currentTextFieldBody = body
            )
        )
    }

    private fun onChangesAddTaskDialogState(oldState: TasksScreenUiState, show: Boolean) {
        setState(
            oldState.copy(
                isShowAddTaskDialog = show
            )
        )
    }

    private fun getAllTasks(oldState: TasksScreenUiState) {
        viewModelScope.launch {
            setState(
                oldState.copy(
                    isLoading = true
                )
            )

            when(val result = repository.getAllTasks()) {
                is Result.Failure -> {
                    setState(
                        oldState.copy(
                            isLoading = false
                        )
                    )

                    val errorMessage = result.exception.message ?: "An error occurred while fetching all tasks"

                    setEffect { TaskScreenSideEffects.ShowSnackBarMessage(message = errorMessage) }
                }

                is Result.Success -> {
                    setState(
                        oldState.copy(
                            isLoading = false,
                            tasks = result.data
                        )
                    )
                }
            }
        }
    }

    private fun deleteTask(oldState: TasksScreenUiState, taskId: String) {
        viewModelScope.launch {
            setState(
                oldState.copy(
                    isLoading = true
                )
            )

            when(val result = repository.deleteTask(taskId = taskId)) {
                is Result.Failure -> {
                    setState(
                        oldState.copy(
                            isLoading = false
                        )
                    )

                    val errorMessage = result.exception.message ?: "An error occurred while deleting task"

                    setEffect { TaskScreenSideEffects.ShowSnackBarMessage(message = errorMessage) }
                }

                is Result.Success -> {
                    setState(
                        oldState.copy(
                            isLoading = false
                        )
                    )

                    setEffect { TaskScreenSideEffects.ShowSnackBarMessage(message = "Task deleted successfully") }

                    sendEvent(event = TaskScreenUiEvents.GetTasks)
                }
            }
        }
    }

    private fun addTask(oldState: TasksScreenUiState, title: String, body: String) {
        viewModelScope.launch {
            setState(
                oldState.copy(
                    isLoading = true
                )
            )

            when(val result = repository.addTask(title = title, body = body)) {
                is Result.Failure -> {
                    setState(
                        oldState.copy(
                            isLoading = false
                        )
                    )

                    val errorMessage = result.exception.message ?: "An error occurred while adding task"

                    setEffect { TaskScreenSideEffects.ShowSnackBarMessage(message = errorMessage) }
                }

                is Result.Success -> {
                    setState(
                        oldState.copy(
                            isLoading = false,
                            currentTextFieldTitle = "",
                            currentTextFieldBody = ""
                        )
                    )

                    sendEvent(event = TaskScreenUiEvents.OnChangeAddTaskDialogState(show = false))

                    sendEvent(event = TaskScreenUiEvents.GetTasks)

                    setEffect { TaskScreenSideEffects.ShowSnackBarMessage(message = "Task added successfully") }
                }
            }
        }
    }
}