package com.example.firebase_test

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavController
import com.example.firebase_test.workspaces.ChecklistItem
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateChecklistDialog(
    onConfirm: (title: String, items: List<ChecklistItem>) -> Unit, onDismiss: () -> Unit
) {
    var title by rememberSaveable { mutableStateOf("") }
    var items by remember { mutableStateOf(listOf("")) }
    var currentItemText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(id = R.string.checklist_dialog_title)) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CharacterLimitedOutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    maxLength = 20,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(id = R.string.checklist_dialog_list_title_label)) },
                    singleLine = true,
                )

                HorizontalDivider()

                items.forEachIndexed { index, item ->
                    if (item.isNotBlank()) {
                        Row(
                            Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .weight(1f, fill = false)
                                    .padding(end = 8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(item)
                            }

                            IconButton(
                                onClick = {
                                    items = items.filterIndexed { i, _ -> i != index }
                                }, modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = stringResource(id = R.string.checklist_dialog_delete_item_desc),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                CharacterLimitedOutlinedTextField(
                    value = currentItemText,
                    onValueChange = { currentItemText = it },
                    maxLength = 20,
                    label = { Text(stringResource(id = R.string.checklist_dialog_new_item_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                if (currentItemText.isNotBlank()) {
                                    items = items + currentItemText
                                    currentItemText = ""
                                }
                            }, enabled = currentItemText.isNotBlank()
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = stringResource(id = R.string.checklist_dialog_add_item_desc)
                            )
                        }
                    })
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                val finalItems = (items + currentItemText).filter { it.isNotBlank() }
                onConfirm(
                    title, finalItems.map { ChecklistItem(text = it, isChecked = false) })
            },
                enabled = title.isNotBlank() && (items.any { it.isNotBlank() } || currentItemText.isNotBlank())) {
                Text(stringResource(id = R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(id = R.string.cancel)) }
        })
}


@Composable
fun MainBottomBar(navController: NavController) {

    val startDestination = Routes.Home
    var selectedDestination by rememberSaveable { mutableIntStateOf(startDestination.ordinal) }

    NavigationBar(windowInsets = NavigationBarDefaults.windowInsets) {
        Routes.entries.forEachIndexed { index, destination ->

            val label = stringResource(destination.labelResId)
            val contentDesc = stringResource(destination.contentDescResId)

            NavigationBarItem(selected = selectedDestination == index, onClick = {
                navController.navigate(route = destination.route)
                selectedDestination = index
            }, icon = {
                Icon(
                    destination.icon, contentDescription = contentDesc
                )
            }, label = {
                Text(label)
            })
        }
    }
}


@Composable
fun CharacterLimitedOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    maxLength: Int,
    modifier: Modifier = Modifier,
    label: @Composable () -> Unit,
    singleLine: Boolean = false,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    trailingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
) {
    OutlinedTextField(
        value = value,
        onValueChange = { newValue ->
            if (newValue.length <= maxLength) {
                onValueChange(newValue)
            }
        },
        label = label,
        singleLine = singleLine,
        minLines = minLines,
        maxLines = maxLines,
        modifier = modifier,
        trailingIcon = trailingIcon,
        supportingText = {
            Text(
                text = "${value.length} / $maxLength",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End
            )
        },
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryFilterRow(
    categories: List<String>, selectedCategory: String?, onCategorySelected: (String) -> Unit
) {
    if (categories.isNotEmpty()) {
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { category ->
                FilterChip(
                    selected = (selectedCategory == category),
                    onClick = { onCategorySelected(category) },
                    label = { Text(category) })
            }
        }
    }
}
