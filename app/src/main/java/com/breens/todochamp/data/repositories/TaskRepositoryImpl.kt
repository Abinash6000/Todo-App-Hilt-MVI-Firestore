package com.breens.todochamp.data.repositories

import com.breens.todochamp.common.COLLECTION_PATH_NAME
import com.breens.todochamp.common.PLEASE_CHECK_INTERNET_CONNECTION
import com.breens.todochamp.common.getCurrentTimeAsString
import com.breens.todochamp.data.model.Task
import com.breens.todochamp.di.IoDispatcher
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import com.breens.todochamp.common.Result
import com.breens.todochamp.common.convertDateFormat
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class TaskRepositoryImpl @Inject constructor(
    // @Inject is injecting these dependencies into the constructor
    private val todoAppDB: FirebaseFirestore,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : TaskRepository {
    override suspend fun addTask(title: String, body: String): Result<Unit> {
        return try {
            withContext(ioDispatcher) {
                // since we're going to store our task in key value pairs
                val task = hashMapOf(
                    "title" to title,
                    "body" to body,
                    "createdAt" to getCurrentTimeAsString()
                )

                // to create a timeout within with the task should complete or otherwise it returns null
                val addTaskTimeout = withTimeoutOrNull(10000L) {
                    todoAppDB.collection(COLLECTION_PATH_NAME)
                        .add(task)
                }

                if(addTaskTimeout == null) {
                    Result.Failure(
                        IllegalStateException(PLEASE_CHECK_INTERNET_CONNECTION)
                    )
                }

                Result.Success(Unit)
            }
        } catch (exception: Exception) {
            Result.Failure(exception = exception)
        }
    }

    override suspend fun getAllTasks(): Result<List<Task>> {
        return try {
            withContext(ioDispatcher) {
                val fetchingTasksTimeout = withTimeoutOrNull(10000L) {
                    todoAppDB.collection(COLLECTION_PATH_NAME)
                        .get()
                        .await()
                        .documents.map { document ->
                            Task(
                                taskId = document.id,
                                title = document.getString("title") ?: "",
                                body = document.getString("body") ?: "",
                                createdAt = convertDateFormat(
                                    dateString = document.getString("createdAt") ?: ""
                                )
                            )
                        }
                }

                if(fetchingTasksTimeout == null) {
                    Result.Failure(
                        IllegalStateException(PLEASE_CHECK_INTERNET_CONNECTION)
                    )
                }

                Result.Success(fetchingTasksTimeout?.toList() ?: emptyList())
            }
        } catch (exception: Exception) {
            Result.Failure(exception = exception)
        }
    }

    override suspend fun deleteTask(taskId: String): Result<Unit> {
        return try {
            withContext(ioDispatcher) {
                val deleteTaskTimeout = withTimeoutOrNull(10000L) {
                    todoAppDB.collection(COLLECTION_PATH_NAME)
                        .document(taskId)
                        .delete()
                }

                if(deleteTaskTimeout == null) {
                    Result.Failure(
                        IllegalStateException(PLEASE_CHECK_INTERNET_CONNECTION)
                    )
                }

                Result.Success(Unit)
            }
        } catch (exception: Exception) {
            Result.Failure(exception = exception)
        }
    }

    override suspend fun updateTask(taskId: String, title: String, body: String): Result<Unit> {
        return try {
            withContext(ioDispatcher) {
                val taskUpdate: Map<String, String> = hashMapOf(
                    "title" to title,
                    "body" to body
                )

                val updateTaskTimeout = withTimeoutOrNull(10000L) {
                    todoAppDB.collection(COLLECTION_PATH_NAME)
                        .document(taskId)
                        .update(taskUpdate)
                }

                if(updateTaskTimeout == null) {
                    Result.Failure(
                        IllegalStateException(PLEASE_CHECK_INTERNET_CONNECTION)
                    )
                }

                Result.Success(Unit)
            }
        } catch (exception: Exception) {
            Result.Failure(exception = exception)
        }
    }
}