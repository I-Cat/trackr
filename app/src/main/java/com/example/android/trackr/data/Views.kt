/*
 * Copyright (C) 2020 The Android Open Source Project
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

package com.example.android.trackr.data

import androidx.room.*
import org.threeten.bp.Instant

@DatabaseView(
    """
        SELECT
            t.id, t.title, t.status, t.dueAt, t.orderInCategory,
            o.id AS owner_id, o.username AS owner_username, o.avatar AS owner_avatar
        FROM tasks AS t
        INNER JOIN users AS o ON o.id = t.ownerId
    """
)
data class TaskSummary(
    val id: Long,

    val title: String,

    val status: TaskStatus,

    val dueAt: Instant,

    val orderInCategory: Int,

    @Embedded(prefix = "owner_")
    val owner: User,

    @Relation(
        parentColumn = "id",
        entity = Tag::class,
        entityColumn = "id",
        associateBy = Junction(
            value = TaskTag::class,
            parentColumn = "taskId",
            entityColumn = "tagId"
        )
    )
    val tags: List<Tag>,

    @Relation(
        parentColumn = "id",
        entity = User::class,
        entityColumn = "id",
        associateBy = Junction(
            value = UserTask::class,
            parentColumn = "taskId",
            entityColumn = "userId"
        )
    )
    val starUsers: List<User>
)

@DatabaseView(
    """
        SELECT
            t.id, t.title, t.description, t.status, t.createdAt, t.dueAt,
            o.id AS owner_id, o.username AS owner_username, o.avatar AS owner_avatar,
            c.id AS creator_id, c.username AS creator_username, c.avatar as creator_avatar
        FROM tasks AS t
        INNER JOIN users AS o ON o.id = t.ownerId
        INNER JOIN users AS c ON c.id = t.creatorId
    """
)
data class TaskDetail(

    val id: Long,

    val title: String,

    val description: String,

    val status: TaskStatus,

    val createdAt: Instant,

    val dueAt: Instant,

    @Embedded(prefix = "owner_")
    val owner: User,

    @Embedded(prefix = "creator_")
    val creator: User,

    @Relation(
        parentColumn = "id",
        entity = Tag::class,
        entityColumn = "id",
        associateBy = Junction(
            value = TaskTag::class,
            parentColumn = "taskId",
            entityColumn = "tagId"
        )
    )
    val tags: List<Tag>,

    @Relation(
        parentColumn = "id",
        entity = User::class,
        entityColumn = "id",
        associateBy = Junction(
            value = UserTask::class,
            parentColumn = "taskId",
            entityColumn = "userId"
        )
    )
    val starUsers: List<User>
)