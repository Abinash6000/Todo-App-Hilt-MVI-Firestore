package com.breens.todochamp.data.repositories

import com.breens.todochamp.data.model.Task
import com.breens.todochamp.common.Result

interface TaskRepository {
    suspend fun addTask(title: String, body: String): Result<Unit>

    suspend fun getAllTasks(): Result<List<Task>>

    suspend fun deleteTask(taskId: String): Result<Unit>

    suspend fun updateTask(taskId: String, title: String, body: String): Result<Unit>
}