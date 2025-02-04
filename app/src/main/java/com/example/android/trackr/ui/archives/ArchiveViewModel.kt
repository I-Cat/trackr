/*
 * Copyright (C) 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.trackr.ui.archives

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.example.android.trackr.data.TaskSummary
import com.example.android.trackr.repository.TrackrRepository
import com.example.android.trackr.ui.utils.timeout
import kotlinx.coroutines.launch

class ArchiveViewModel @ViewModelInject constructor(
    private val repository: TrackrRepository
) : ViewModel() {

    private val archivedTaskSummaries = repository.getArchivedTaskSummaries()
    private val selectedTaskIds = MutableLiveData(emptySet<Long>())

    /** A set of taskIds that were most recently unarchived. */
    private val undoableTaskIds = MutableLiveData(emptySet<Long>())

    /**
     * The number of recently unarchived tasks that can be archived back with the "undo" feature.
     * Undo is available during 5 seconds after unarchiving tasks.
     */
    val undoableCount = undoableTaskIds
        .map { it.size }
        .timeout(viewModelScope, 5000L, 0)

    val archivedTasks = MediatorLiveData<List<ArchivedTask>>().apply {
        fun update() {
            val selected = selectedTaskIds.value ?: return
            val tasks = archivedTaskSummaries.value ?: return
            value = tasks.map { task ->
                ArchivedTask(task, task.id in selected)
            }
        }
        addSource(archivedTaskSummaries) { update() }
        addSource(selectedTaskIds) { update() }
    }

    val selectedCount = selectedTaskIds.map { it.size }

    fun toggleTaskSelection(taskId: Long) {
        val selected = selectedTaskIds.value ?: emptySet()
        if (taskId in selected) {
            selectedTaskIds.value = selected - taskId
        } else {
            selectedTaskIds.value = selected + taskId
        }
    }

    fun toggleTaskStarState(taskId: Long) {
        viewModelScope.launch {
            repository.toggleTaskStarState(taskId)
        }
    }

    fun clearSelection() {
        selectedTaskIds.value = emptySet()
    }

    fun unarchiveSelectedTasks() {
        val ids = selectedTaskIds.value ?: return
        viewModelScope.launch {
            repository.unarchive(ids.toList())
            undoableTaskIds.value = ids
            selectedTaskIds.value = emptySet()
        }
    }

    fun undoUnarchiving() {
        val ids = undoableTaskIds.value ?: return
        viewModelScope.launch {
            repository.archive(ids.toList())
            undoableTaskIds.value = emptySet()
        }
    }
}

data class ArchivedTask(
    val taskSummary: TaskSummary,
    val selected: Boolean
)
