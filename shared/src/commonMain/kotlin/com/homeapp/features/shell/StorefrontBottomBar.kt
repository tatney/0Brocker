package com.homeapp.features.shell

import androidx.compose.foundation.background
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.homeapp.core.theme.DmSansFontFamily
import com.homeapp.core.widgets.AppBottomBarItem

@Composable
internal fun StorefrontBottomBar(items: List<AppBottomBarItem>) {
    Row(Modifier.fillMaxWidth().background(Color.White).navigationBarsPadding()) {
        items.forEachIndexed { index, item ->
            val accent = Color(0xFFEF3C64)
            Column(
                Modifier.weight(1f).height(62.dp)
                    .background(if (item.selected) Brush.verticalGradient(listOf(Color(0xFFFFEEEE), Color.White)) else Brush.verticalGradient(listOf(Color.White, Color.White)))
                    .selectable(selected = item.selected, role = Role.Tab, onClick = item.onClick),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(Modifier.fillMaxWidth().height(2.dp).background(if (item.selected) accent else Color(0xFFF8F8F8)))
                Spacer(Modifier.height(13.dp))
                if (index == 0) BrandMark(12) else Icon(item.icon, null, tint = if (item.selected) accent else Color(0xFF646464), modifier = Modifier.size(12.dp))
                Spacer(Modifier.height(5.dp))
                Text(item.label, fontFamily = DmSansFontFamily, fontSize = 9.sp, color = if (item.selected) accent else Color(0xFF454545))
            }
        }
    }
}


