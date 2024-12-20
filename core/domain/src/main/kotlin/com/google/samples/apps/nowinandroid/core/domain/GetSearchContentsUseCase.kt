/*
 * Copyright 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.samples.apps.nowinandroid.core.domain

import androidx.tracing.trace
import com.google.samples.apps.nowinandroid.core.data.repository.SearchContentsRepository
import com.google.samples.apps.nowinandroid.core.data.repository.UserDataRepository
import com.google.samples.apps.nowinandroid.core.model.data.FollowableTopic
import com.google.samples.apps.nowinandroid.core.model.data.SearchResult
import com.google.samples.apps.nowinandroid.core.model.data.UserData
import com.google.samples.apps.nowinandroid.core.model.data.UserNewsResource
import com.google.samples.apps.nowinandroid.core.model.data.UserSearchResult
import com.google.samples.apps.nowinandroid.core.network.Dispatcher
import com.google.samples.apps.nowinandroid.core.network.NiaDispatchers.Default
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

/**
 * A use case which returns the searched contents matched with the search query.
 */
class GetSearchContentsUseCase @Inject constructor(
    private val searchContentsRepository: SearchContentsRepository,
    private val userDataRepository: UserDataRepository,
    @Dispatcher(Default) private val defaultDispatcher: CoroutineDispatcher,
) {

    operator fun invoke(
        searchQuery: String,
    ): Flow<UserSearchResult> =
        searchContentsRepository.searchContents(searchQuery)
            .mapToUserSearchResult(userDataRepository.userData, defaultDispatcher)
}

private fun Flow<SearchResult>.mapToUserSearchResult(
    userDataStream: Flow<UserData>,
    defaultDispatcher: CoroutineDispatcher,
): Flow<UserSearchResult> =
    combine(userDataStream) { searchResult, userData ->
        trace("Flow<SearchResult>.mapToUserSearchResult") {
            UserSearchResult(
                topics = searchResult.topics.map { topic ->
                    FollowableTopic(
                        topic = topic,
                        isFollowed = topic.id in userData.followedTopics,
                    )
                },
                newsResources = searchResult.newsResources.map { news ->
                    UserNewsResource(
                        newsResource = news,
                        userData = userData,
                    )
                },
            )
        }
    }.flowOn(defaultDispatcher)
