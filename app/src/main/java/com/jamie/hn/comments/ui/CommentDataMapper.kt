package com.jamie.hn.comments.ui

import com.jamie.hn.comments.domain.model.Comment
import com.jamie.hn.comments.ui.extensions.fixUrlSpans
import com.jamie.hn.comments.ui.extensions.italiciseQuotes
import com.jamie.hn.comments.ui.extensions.removeAppendedNewLines
import com.jamie.hn.comments.ui.repository.model.CommentCurrentState
import com.jamie.hn.comments.ui.repository.model.CurrentState.HEADER
import com.jamie.hn.core.ui.CoreDataMapper
import com.jamie.hn.stories.domain.model.Story
import com.jamie.hn.stories.ui.StoryResourceProvider

class CommentDataMapper(
    private val commentsResourceProvider: CommentsResourceProvider,
    private val coreDataMapper: CoreDataMapper,
    private val resourceProvider: StoryResourceProvider
) {

    fun toCommentViewItem(
        wrapper: CommentCurrentState,
        collapseCallback: (Int) -> Unit,
        urlClickedCallback: (String) -> Unit
    ): CommentViewItem {
        val comment = wrapper.comment.comment
        val depth = wrapper.comment.depth

        return CommentViewItem(
            id = wrapper.comment.id,
            state = wrapper.state,
            author = comment.author,
            time = coreDataMapper.time(comment.time),
            text = processText(comment.text, urlClickedCallback),
            depth = depth,
            authorAndHiddenChildren = authorAndHiddenChildren(comment),
            showTopDivider = depth == 0,
            longClickCommentListener = collapseCallback
        )
    }

    fun toStoryHeaderViewItem(
        story: Story,
        storyViewerCallback: (Int) -> Unit
    ) = HeaderViewItem(
        id = story.id,
        state = HEADER,
        author = story.author,
        time = coreDataMapper.time(story.time),
        comments = comments(story.commentCount),
        score = story.score.toString(),
        scoreText = scoreText(story.score),
        title = story.title,
        url = story.domain,
        storyViewerCallback = storyViewerCallback
    )

    fun processText(text: String, urlClickedCallback: (String) -> Unit) =
        text.fixUrlSpans(urlClickedCallback)
            .italiciseQuotes()
            .removeAppendedNewLines()

    private fun authorAndHiddenChildren(comment: Comment) =
        "${comment.author} [${comment.commentCount} ${commentsResourceProvider.children()}]"

    private fun comments(numComments: Int) = resourceProvider.comments(numComments)
    private fun scoreText(score: Int) = resourceProvider.score(score)
}
