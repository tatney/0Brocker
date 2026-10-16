package com.homeapp.core.widgets
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.homeapp.data.model.ServiceCategory
import com.homeapp.core.icons.illustratedServiceIcon
@Composable fun ServicePicker(selected: ServiceCategory,onSelect: (ServiceCategory)->Unit,includeAll: Boolean=false) {
 var open by remember {mutableStateOf(false)}
 Box {
  OutlinedButton(onClick={open=true}) {Icon(illustratedServiceIcon(selected.name),null,Modifier.size(34.dp),tint=Color.Unspecified);Spacer(Modifier.width(10.dp));Text(selected.label+"  v")}
  DropdownMenu(modifier=Modifier.heightIn(max=360.dp),expanded=open,onDismissRequest={open=false}) {ServiceCategory.entries.filter{includeAll||it!=ServiceCategory.ALL}.forEach {trade->DropdownMenuItem(text={Text(trade.label)},leadingIcon={Icon(illustratedServiceIcon(trade.name),null,Modifier.size(36.dp),tint=Color.Unspecified)},onClick={onSelect(trade);open=false})}}
 }
}
