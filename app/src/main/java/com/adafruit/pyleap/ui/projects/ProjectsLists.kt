package com.adafruit.pyleap.ui.projects

/**
 * Created by Antonio García (antonio@openroad.es)
 */

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.adafruit.pyleap.repository.ProjectsRepositoryFake
import com.adafruit.pyleap.model.PyLeapProject
import com.adafruit.pyleap.ui.theme.ProjectCardBackground
import com.adafruit.pyleap.ui.theme.PyLeapTheme

@Composable
fun ProjectsList(
    modifier: Modifier = Modifier,
    projects: List<PyLeapProject>,
    isLoading: Boolean,
    onSelectProjectId: (String) -> Unit,
    projectsListLazyListState: LazyListState = rememberLazyListState(),
) {
    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .wrapContentSize(Alignment.Center)
        ) {
            CircularProgressIndicator()
        }
    } else {
        LazyColumn(
            modifier = modifier,
            state = projectsListLazyListState
        ) {
            item {
                Text(
                    text = "Browse available PyLeap Projects",
                    style = MaterialTheme.typography.headlineSmall,
                    //color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .background(Color.White)
                        .padding(16.dp)
                )
            }

            items(items = projects, key = { it.data.id }) { project ->
                ProjectCard(project = project, onSelectProjectId = onSelectProjectId)
            }
        }
    }
}

@Composable
private fun ProjectCard(project: PyLeapProject, onSelectProjectId: (String) -> Unit) {

    Row(
        Modifier
            .clickable(onClick = { onSelectProjectId(project.data.id) })
            .padding(vertical = 4.dp)
            .background(ProjectCardBackground)
            .padding(16.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,

        ) {
        Text(
            project.data.title,
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
        )
        Icon(
            imageVector = Icons.Filled.NavigateNext,
            contentDescription = null,
            tint = Color.White
        )
    }
}


@Preview(showBackground = true)
@Composable
fun ProjectsListPreview() {
    // Use the ProjectsRepositoryFake for Preview
    val projects =
        ProjectsRepositoryFake(context = LocalContext.current).getAllPyLeapProjectsSynchronously()

    PyLeapTheme {
        ProjectsList(
            projects = projects,
            isLoading = false,
            onSelectProjectId = {},
        )
    }
}
// endregion