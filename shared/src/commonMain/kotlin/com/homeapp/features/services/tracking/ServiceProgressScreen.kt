package com.homeapp.features.services.tracking

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.homeapp.core.icons.IconBack
import com.homeapp.core.icons.IconBox
import com.homeapp.core.icons.IconCheck
import com.homeapp.core.icons.IconGauge
import com.homeapp.core.icons.IconHammer
import com.homeapp.core.icons.IconSliders
import com.homeapp.core.icons.IconWrench
import com.homeapp.core.theme.kAccentTeal
import com.homeapp.core.theme.kIconGrey
import com.homeapp.core.theme.kRadiusLG
import com.homeapp.core.theme.kRadiusSM
import com.homeapp.core.theme.kSpaceLG
import com.homeapp.core.theme.kSpaceMD
import com.homeapp.core.theme.kSpaceSM
import com.homeapp.core.theme.kSuccessLight
import com.homeapp.core.theme.kTextSecondary
import com.homeapp.core.widgets.CTAButton
import com.homeapp.core.widgets.TagBadge

private val statusSteps = listOf(
    "Inspection" to IconSliders,
    "Work Started" to IconHammer,
    "Materials Required" to IconBox,
    "Work in Progress" to IconGauge,
    "Completed" to IconCheck,
)

@Composable
fun ServiceProgressScreen(
    onBack: () -> Unit,
    onComplete: () -> Unit,
    viewModel: ServiceProgressViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val booking = state.booking
    Scaffold(containerColor = MaterialTheme.colorScheme.background) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = kSpaceMD),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(IconBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
                }
                Text("Service in Progress", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(kSpaceMD))

            val providerName = booking?.providerName?.ifBlank { "Provider" } ?: "Provider"
            val serviceType = booking?.serviceType ?: "Service"

            // Provider card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(kSuccessLight, kRadiusLG)
                    .padding(kSpaceMD),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier.size(48.dp).background(kAccentTeal.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = IconWrench,
                        contentDescription = null,
                        tint = kAccentTeal,
                        modifier = Modifier.size(24.dp),
                    )
                }
                Spacer(Modifier.width(kSpaceSM))
                Column(Modifier.weight(1f)) {
                    Text(providerName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(serviceType, style = MaterialTheme.typography.bodySmall, color = kTextSecondary)
                }
TagBadge(text = state.booking?.status?.label ?: "In Progress", foreground = kAccentTeal)
            }

            Spacer(Modifier.height(kSpaceLG))

            Text("Job Status", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(kSpaceSM))

            val activeIndex = state.activeIndex

            // Status pipeline
            statusSteps.forEachIndexed { index, (label, stepIcon) ->
                val isActive = index <= activeIndex
                val isCurrent = index == activeIndex
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (isCurrent) kAccentTeal.copy(alpha = 0.1f) else Color.Transparent,
                            kRadiusSM,
                        )
                        .padding(kSpaceSM),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                if (isActive) kAccentTeal else kIconGrey,
                                CircleShape,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = stepIcon,
                            contentDescription = null,
                            tint = if (isActive) Color.White else kTextSecondary,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                    Spacer(Modifier.width(kSpaceSM))
                    Text(
                        label,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                        color = if (isActive) MaterialTheme.colorScheme.onSurface else kTextSecondary,
                    )
                }
                if (index < statusSteps.lastIndex) {
                    Box(
                        modifier = Modifier
                            .padding(start = kSpaceMD)
                            .width(2.dp)
                            .height(16.dp)
                            .background(if (isActive) kAccentTeal else kIconGrey),
                    )
                }
            }

            Spacer(Modifier.height(kSpaceLG))

            LinearProgressIndicator(
                progress = { (activeIndex + 1).toFloat() / statusSteps.size },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = kAccentTeal,
                trackColor = kIconGrey,
            )

            Spacer(Modifier.height(kSpaceLG))

            CTAButton(
                text = "Mark as Completed",
                onClick = { viewModel.markCompleted(onComplete) },
                enabled = !state.isMarkingCompleted,
            )
            Spacer(Modifier.height(kSpaceMD))
        }
    }
}
