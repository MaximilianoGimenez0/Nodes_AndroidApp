package com.example.firebase_test.workspaces.entries

import com.example.firebase_test.workspaces.ChecklistItem
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CreateChecklistDialog(
    onConfirm: (title: String, items: List<ChecklistItem>) -> Unit, onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }

    var itemTexts by remember { mutableStateOf(listOf("")) }

    AlertDialog(onDismissRequest = onDismiss, title = { Text("Crear Checklist") }, text = {
        Column {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Título de la lista") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("Items:", style = MaterialTheme.typography.titleMedium)

            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                itemsIndexed(itemTexts) { index, text ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = text, onValueChange = { newText ->
                            val newList = itemTexts.toMutableList()
                            newList[index] = newText
                            itemTexts = newList
                        }, label = { Text("Item ${index + 1}") }, modifier = Modifier.weight(1f)
                        )

                        if (itemTexts.size > 1) {
                            IconButton(onClick = {
                                val newList = itemTexts.toMutableList()
                                newList.removeAt(index)
                                itemTexts = newList
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Eliminar item")
                            }
                        }
                    }
                }

                if (itemTexts.size < 10) {
                    item {
                        TextButton(
                            onClick = {
                                val newList = itemTexts.toMutableList()
                                newList.add("")
                                itemTexts = newList
                            }, modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Añadir")
                            Text("Añadir item")
                        }
                    }
                }
            }
        }
    }, confirmButton = {
        TextButton(
            onClick = {
                val validTexts = itemTexts.filter { it.isNotBlank() }

                val checklistItems = validTexts.map { text ->
                    ChecklistItem(text = text, isChecked = false)
                }

                if (title.isNotBlank() && checklistItems.isNotEmpty()) {
                    onConfirm(title, checklistItems)
                } else {
                    onDismiss()
                }
            }) {
            Text("Guardar")
        }
    }, dismissButton = {
        TextButton(onClick = onDismiss) {
            Text("Cancelar")
        }
    })

}


