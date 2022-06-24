package com.adafruit.pyleap.ui.projects

/**
 * Created by Antonio García (antonio@openroad.es)
 */

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.adafruit.pyleap.model.Project
import com.adafruit.pyleap.ui.theme.ProjectCardBackground

@Composable
fun ProjectsList(
    modifier: Modifier = Modifier,
    projects: List<Project>,
    onSelectProjectId: (String) -> Unit,
    projectsListLazyListState: LazyListState,
) {
    LazyColumn(
        modifier = modifier,
        state = projectsListLazyListState
    ) {
        item {
            Text(
                text = "Browse all of the available PyLeap Projects",
                style = MaterialTheme.typography.headlineSmall,
                //color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .background(Color.White)
                    .padding(16.dp)
            )
        }

        items(items = projects, key = { it.id }) { project ->
            ProjectCard(project = project, onSelectProjectId = onSelectProjectId)
        }
    }
}

@Composable
private fun ProjectCard(project: Project, onSelectProjectId: (String) -> Unit) {
    Row(
        Modifier
            .clickable(onClick = { onSelectProjectId(project.id) })
            .padding(vertical = 4.dp)
            .background(ProjectCardBackground)
            .padding(16.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,

        ) {
        Text(
            project.title,
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
