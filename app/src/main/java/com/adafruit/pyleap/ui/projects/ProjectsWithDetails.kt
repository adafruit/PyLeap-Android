package com.adafruit.pyleap.ui.projects

/**
 * Created by Antonio García (antonio@openroad.es)
 */

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.adafruit.pyleap.model.Project

@Composable
fun ProjectsWithDetails(
    projects: List<Project>,
    selectedProject: Project?,
    onSelectProjectId: (String) -> Unit,
    projectsListLazyListState: LazyListState,
    projectsDetailLazyListStates: Map<String, LazyListState>,
) {
    Row(
        //modifier = Modifier.padding(20.dp),
        horizontalArrangement = Arrangement.spacedBy(30.dp)
    ) {
        ProjectsList(
            modifier = Modifier
                .fillMaxWidth(0.4f)
                //.notifyInput(onUnselectAll)
                .imePadding(), // add padding for the on-screen keyboard
            projects = projects,
            onSelectProjectId = onSelectProjectId,
            projectsListLazyListState = projectsListLazyListState,
        )

        // Crossfade between different project details
        Crossfade(targetState = selectedProject) { project ->

            if (project == null) {
                /* TODO empty state */
            } else {
                // Get the lazy list state for this detail view
                val detailLazyListState by derivedStateOf {
                    projectsDetailLazyListStates.getValue(project.id)
                }

                // Key against the project id to avoid sharing any state between different projects
                key(project.id) {
                    ProjectDetailsScreen(
                        project = project,
                        isExpandedScreen = true,
                        onBack = { /* Nothing to do */ },
                        onRunProjectId =  { /*TODO*/ }
                    )
                }
            }
        }
    }
}
