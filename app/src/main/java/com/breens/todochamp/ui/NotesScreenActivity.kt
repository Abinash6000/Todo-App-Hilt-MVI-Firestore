package com.breens.todochamp.feature_tasks.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.breens.todochamp.common.SIDE_EFFECTS_KEY
import com.breens.todochamp.feature_tasks.ui.components.AddTaskDialogComponent
import com.breens.todochamp.feature_tasks.ui.components.EmptyComponent
import com.breens.todochamp.feature_tasks.ui.components.LoadingComponent
import com.breens.todochamp.feature_tasks.ui.components.TaskCardComponent
import com.breens.todochamp.feature_tasks.ui.components.UpdateTaskDialogComponent
import com.breens.todochamp.feature_tasks.ui.components.WelcomeMessageComponent
import com.breens.todochamp.feature_tasks.ui.events.TaskScreenUiEvents
import com.breens.todochamp.feature_tasks.ui.side_effects.TaskScreenSideEffects
import com.breens.todochamp.feature_tasks.ui.viewmodel.TasksViewModel
import com.breens.todochamp.ui.theme.TodoChampTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
class NotesScreenActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val tasksViewModel: TasksViewModel = viewModel()

            val uiState = tasksViewModel.state.collectAsState().value

            val effect = tasksViewModel.effect

            val snackBarHostState = remember { SnackbarHostState() }

            LaunchedEffect(key1 = SIDE_EFFECTS_KEY) {
                effect.onEach { effect ->
                    when(effect) {
                        is TaskScreenSideEffects.ShowSnackBarMessage -> {
                            snackBarHostState.showSnackbar(
                                message = effect.message,
                                duration = SnackbarDuration.Short,
                                actionLabel = "DISMISS"
                            )
                        }
                    }
                }.collect()
            }

            TodoChampTheme {
                if(uiState.isShowAddTaskDialog) {
                    AddTaskDialogComponent(
                        uiState = uiState,
                        setTaskTitle = { title ->
                            tasksViewModel.sendEvent(event = TaskScreenUiEvents.OnChangeTaskTitle(title = title))
                        },
                        setTaskBody = { body ->
                            tasksViewModel.sendEvent(event = TaskScreenUiEvents.OnChangeTaskBody(body = body))
                        },
                        saveTask = {
                            tasksViewModel.sendEvent(
                                event = TaskScreenUiEvents.AddTask(
                                    title = uiState.currentTextFieldTitle,
                                    body = uiState.currentTextFieldBody
                                )
                            )
                        },
                        closeDialog = {
                            tasksViewModel.sendEvent(
                                event = TaskScreenUiEvents.OnChangeAddTaskDialogState(show = false)
                            )
                        }
                    )
                }

                if(uiState.isShowUpdateTaskDialog) {
                    UpdateTaskDialogComponent(
                        uiState = uiState,
                        setTaskTitle = { title ->
                            tasksViewModel.sendEvent(
                                event = TaskScreenUiEvents.OnChangeTaskTitle(
                                    title = title
                                )
                            )
                        },
                        setTaskBody = { body ->
                            tasksViewModel.sendEvent(
                                event = TaskScreenUiEvents.OnChangeTaskBody(
                                    body = body
                                )
                            )
                        },
                        saveTask = {
                            tasksViewModel.sendEvent(event = TaskScreenUiEvents.UpdateTask)
                        },
                        closeDialog = {
                            tasksViewModel.sendEvent(
                                event = TaskScreenUiEvents.OnChangeUpdateDialogState(show = false)
                            )
                        },
                        task = uiState.taskToBeUpdated
                    )
                }

                Scaffold(
                    snackbarHost = {
                        SnackbarHost (snackBarHostState)
                    },
                    floatingActionButton = {
                        ExtendedFloatingActionButton(
                            icon = {
                                Icon(
                                    imageVector = Icons.Rounded.AddCircle,
                                    contentDescription = "Add Task",
                                    tint = Color.White
                                )
                            },
                            text = {
                                Text(
                                    text = "Add Task",
                                    color = Color.White
                                )
                            },
                            onClick = {
                                tasksViewModel.sendEvent(
                                    event = TaskScreenUiEvents.OnChangeAddTaskDialogState(show = true)
                                )
                            },
                            modifier = Modifier.padding(horizontal = 12.dp),
                            containerColor = Color.Black,
                            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp)
                        )
                    },
                    containerColor = Color(0xfffafafa)
                ) { paddingValues ->
                    Box(modifier = Modifier.padding(paddingValues = paddingValues)) {
                        when {
                            uiState.isLoading -> {
                                LoadingComponent()
                            }

                            !uiState.isLoading && uiState.tasks.isNotEmpty() -> {
                                LazyColumn(
                                    contentPadding = PaddingValues(14.dp)
                                ) {
                                    item {
                                        WelcomeMessageComponent()

                                        Spacer(modifier = Modifier.height(30.dp))
                                    }

                                    items(uiState.tasks) { task ->
                                        TaskCardComponent(
                                            deleteTask = { taskId ->
                                                tasksViewModel.sendEvent(
                                                    event = TaskScreenUiEvents.DeleteTask(taskId)
                                                )
                                            },
                                            updateTask = { task ->
                                                tasksViewModel.sendEvent(
                                                    event = TaskScreenUiEvents.OnChangeUpdateDialogState(
                                                        show = true
                                                    )
                                                )

                                                tasksViewModel.sendEvent(
                                                    event = TaskScreenUiEvents.SetTaskToBeUpdated(
                                                        taskToBeUpdated = task
                                                    )
                                                )
                                            },
                                            task = task
                                        )
                                    }
                                }
                            }

                            !uiState.isLoading && uiState.tasks.isEmpty() -> {
                                EmptyComponent()
                            }
                        }
                    }
                }
            }
        }
    }
}
