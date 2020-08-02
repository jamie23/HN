package com.jamie.hn.stories.ui

import androidx.lifecycle.Observer
import com.jamie.hn.stories.domain.StoriesUseCase
import com.jamie.hn.core.BaseTest
import com.jamie.hn.core.Event
import com.jamie.hn.core.InstantExecutorExtension
import com.jamie.hn.stories.domain.model.Story
import com.jamie.hn.stories.ui.StoryListViewModel.StoryListViewState
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.just
import io.mockk.runs
import io.mockk.slot
import io.mockk.spyk
import io.mockk.verify
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(InstantExecutorExtension::class)
class StoryListViewModelTest : BaseTest() {
    @RelaxedMockK
    private lateinit var storyDataMapper: StoryDataMapper

    @MockK
    private lateinit var storiesUseCase: StoriesUseCase

    private lateinit var storyListViewModel: StoryListViewModel

    private val story = Story(
        id = 23,
        time = DateTime.parse(
            "23/08/2020 09:00:00",
            DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss")
        ),
        url = "url"
    )

    private val stories = listOf(story)
    private val storyViewItem = StoryViewItem(
        id = 0,
        author = "Jamie",
        comments = "1",
        score = "2",
        time = "3",
        title = "title",
        url = "url",
        commentsCallback = { },
        storyViewerCallback = { }
    )

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        coEvery { storiesUseCase.getStories(any()) } returns stories
        coEvery { storiesUseCase.getStory(1, true) } returns story
        every { storyDataMapper.toStoryViewItem(any(), any(), any()) } returns storyViewItem

        storyListViewModel = StoryListViewModel(storyDataMapper, storiesUseCase)
    }

    @Nested
    inner class RefreshList {

        @Nested
        inner class ManualRefresh {

            @Test
            fun `when refresh is called then we first emit empty list and is refreshing`() {
                val observer = spyk<Observer<StoryListViewState>>()

                storyListViewModel.storyListViewState().observeForever(observer)
                storyListViewModel.userManuallyRefreshed()

                verify { observer.onChanged(StoryListViewState(emptyList(), true)) }
            }

            @Test
            fun `when refresh is called then we call the usecase with false, map the results with the mapper and emit the view state`() {
                val observer = spyk<Observer<StoryListViewState>>()

                storyListViewModel.storyListViewState().observeForever(observer)
                storyListViewModel.userManuallyRefreshed()

                coVerify { storiesUseCase.getStories(false) }
                verify { storyDataMapper.toStoryViewItem(story, any(), any()) }
                verify { observer.onChanged(StoryListViewState(listOf(storyViewItem), false)) }
            }
        }

        @Nested
        inner class AutomaticRefresh {

            @Test
            fun `when refresh is called then we first emit empty list and is refreshing`() {
                val observer = spyk<Observer<StoryListViewState>>()

                storyListViewModel.storyListViewState().observeForever(observer)
                storyListViewModel.automaticallyRefreshed()

                verify { observer.onChanged(StoryListViewState(emptyList(), true)) }
            }

            @Test
            fun `when refresh is called then we call the usecase with true, map the results with the mapper and emit the view state`() {
                val observer = spyk<Observer<StoryListViewState>>()

                storyListViewModel.storyListViewState().observeForever(observer)
                storyListViewModel.automaticallyRefreshed()

                coVerify { storiesUseCase.getStories(true) }
                verify { storyDataMapper.toStoryViewItem(story, any(), any()) }
                verify { observer.onChanged(StoryListViewState(listOf(storyViewItem), false)) }
            }
        }

        @Test
        fun `when comments callback is called then we get the story using cache version and post the id to correct live data`() {
            val observer = spyk<Observer<Event<Long>>>()
            val commentsCallback = slot<(id: Long) -> Unit>()
            val idEmitted = slot<Event<Long>>()

            every { storyDataMapper.toStoryViewItem(any(), capture(commentsCallback), any()) } returns storyViewItem
            every { observer.onChanged(capture(idEmitted)) } just runs

            storyListViewModel.navigateToComments().observeForever(observer)
            storyListViewModel.automaticallyRefreshed()

            commentsCallback.captured.invoke(1)

            coVerify { storiesUseCase.getStory(1, true) }
            Assertions.assertEquals(23, idEmitted.captured.getContentIfNotHandled())
        }

        @Test
        fun `when article viewer callback is called then we get the story using cache and post the url to the correct live data`() {
            val observer = spyk<Observer<Event<String>>>()
            val articleViewerCallback = slot<(id: Long) -> Unit>()
            val urlEmitted = slot<Event<String>>()

            every { storyDataMapper.toStoryViewItem(any(), any(), capture(articleViewerCallback)) } returns storyViewItem
            every { observer.onChanged(capture(urlEmitted)) } just runs

            storyListViewModel.navigateToArticle().observeForever(observer)
            storyListViewModel.automaticallyRefreshed()

            articleViewerCallback.captured.invoke(1)

            coVerify { storiesUseCase.getStory(1, true) }
            Assertions.assertEquals("url", urlEmitted.captured.getContentIfNotHandled())
        }
    }
}