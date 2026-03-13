package com.claw.api.ui.drive

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.claw.api.data.AuthManager
import com.claw.api.data.drive.DriveFile
import com.claw.api.data.drive.DriveRepository
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import kotlinx.coroutines.launch

class DriveViewModel : ViewModel() {
    var files by mutableStateOf<List<DriveFile>>(emptyList())
        private set
    
    var isLoading by mutableStateOf(false)
        private set
    
    var errorMessage by mutableStateOf<String?>(null)
        private set
    
    var searchQuery by mutableStateOf("")
        private set
    
    private var driveRepository: DriveRepository? = null
    
    fun initialize(credential: GoogleAccountCredential) {
        val driveService = Drive.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        ).setApplicationName("Claw API").build()
        
        driveRepository = DriveRepository(driveService)
        loadFiles()
    }
    
    fun loadFiles() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            
            val result = driveRepository?.listFiles()
            result?.fold(
                onSuccess = { fileList ->
                    files = fileList
                    isLoading = false
                },
                onFailure = { error ->
                    errorMessage = error.message
                    isLoading = false
                }
            )
        }
    }
    
    fun searchFiles(query: String) {
        viewModelScope.launch {
            if (query.isBlank()) {
                loadFiles()
                return@launch
            }
            
            isLoading = true
            errorMessage = null
            
            val result = driveRepository?.searchFiles(query)
            result?.fold(
                onSuccess = { fileList ->
                    files = fileList
                    isLoading = false
                },
                onFailure = { error ->
                    errorMessage = error.message
                    isLoading = false
                }
            )
        }
    }
    
    fun updateSearchQuery(query: String) {
        searchQuery = query
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriveScreen(viewModel: DriveViewModel = viewModel()) {
    val context = LocalContext.current
    val authManager = remember { AuthManager(context) }
    
    LaunchedEffect(Unit) {
        authManager.getSignedInAccount()?.let { account ->
            viewModel.initialize(authManager.getDriveCredential(account))
        }
    }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Search Bar
        OutlinedTextField(
            value = viewModel.searchQuery,
            onValueChange = { query ->
                viewModel.updateSearchQuery(query)
                if (query.length >= 3) {
                    viewModel.searchFiles(query)
                } else if (query.isEmpty()) {
                    viewModel.loadFiles()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            placeholder = { Text("搜索文件...") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null)
            },
            trailingIcon = {
                if (viewModel.searchQuery.isNotEmpty()) {
                    IconButton(onClick = {
                        viewModel.updateSearchQuery("")
                        viewModel.loadFiles()
                    }) {
                        Icon(Icons.Default.Clear, contentDescription = "清除")
                    }
                }
            }
        )
        
        // Content
        when {
            viewModel.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            viewModel.errorMessage != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = viewModel.errorMessage ?: "未知错误",
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadFiles() }) {
                            Text("重试")
                        }
                    }
                }
            }
            
            viewModel.files.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Folder,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "没有找到文件",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(viewModel.files) { file ->
                        FileItem(file = file)
                    }
                }
            }
        }
    }
}

@Composable
fun FileItem(file: DriveFile) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable {
                // TODO: 打开文件或下载
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when {
                    file.mimeType.contains("image") -> Icons.Default.Image
                    file.mimeType.contains("video") -> Icons.Default.VideoFile
                    file.mimeType.contains("audio") -> Icons.Default.AudioFile
                    file.mimeType.contains("pdf") -> Icons.Default.PictureAsPdf
                    file.mimeType.contains("folder") -> Icons.Default.Folder
                    else -> Icons.Default.Description
                },
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = file.name,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    file.size?.let { size ->
                        Text(
                            text = formatFileSize(size),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    file.modifiedTime?.let { time ->
                        Text(
                            text = time,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            Icon(
                Icons.Default.MoreVert,
                contentDescription = "更多选项",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
        else -> "${bytes / (1024 * 1024 * 1024)} GB"
    }
}
