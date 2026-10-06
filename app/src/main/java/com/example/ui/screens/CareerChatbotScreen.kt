package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.ChatMessage
import com.example.ai.GeminiChatService
import com.example.ui.theme.JobPulseAmber
import com.example.ui.theme.JobPulseBlue
import com.example.ui.theme.JobPulseEmerald
import com.example.ui.theme.JobPulsePurple
import com.example.ui.theme.JobPulseTeal
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Career Chatbot Screen:
 * Multi-turn AI chat interface powered by Gemini.
 * Features:
 * - Maintains full conversation history in a scrollable thread
 * - Role-specific system instructions (Executive career mentor & interview coach)
 * - Model selection: gemini-3.5-flash (General), gemini-3.1-pro-preview (Complex), gemini-3.1-flash-lite (Fast)
 * - Quick prompt suggestions for resume critique, interview prep, and salary negotiation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CareerChatbotScreen(
  onNavigateBack: () -> Unit
) {
  BackHandler { onNavigateBack() }

  val scope = rememberCoroutineScope()
  val listState = rememberLazyListState()

  var selectedModel by remember { mutableStateOf(GeminiChatService.MODEL_GENERAL) }
  var showModelMenu by remember { mutableStateOf(false) }

  var currentInputText by remember { mutableStateOf("") }
  var isSending by remember { mutableStateOf(false) }

  // Initial greeting
  val messages = remember {
    mutableStateListOf(
      ChatMessage(
        role = "model",
        text = "Hello! I am your JobPulse AI Career Coach. How can I assist you with your career goals, resume optimization, or interview preparation today?",
        modelUsed = GeminiChatService.MODEL_GENERAL
      )
    )
  }

  // Scroll to bottom when messages update
  LaunchedEffect(messages.size) {
    if (messages.isNotEmpty()) {
      listState.animateScrollToItem(messages.size - 1)
    }
  }

  val promptSuggestions = listOf(
    "💡 Review my resume for ATS keywords",
    "🎯 Practice clinical pharmacist interview questions",
    "💰 How can I negotiate salary for a senior role?",
    "📈 What high-demand skills should I learn in 2026?"
  )

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = JobPulsePurple, modifier = Modifier.size(20.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Career AI Coach", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Text(
              text = "Multi-Turn Gemini Career Advisor • $selectedModel",
              style = MaterialTheme.typography.bodySmall,
              fontSize = 11.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        },
        navigationIcon = {
          IconButton(onClick = onNavigateBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        actions = {
          // Model selection chip
          Box {
            FilterChip(
              selected = true,
              onClick = { showModelMenu = true },
              label = {
                Text(
                  when (selectedModel) {
                    GeminiChatService.MODEL_COMPLEX -> "3.1 Pro"
                    GeminiChatService.MODEL_FAST -> "3.1 Lite"
                    else -> "3.5 Flash"
                  },
                  fontSize = 11.sp
                )
              },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = JobPulsePurple.copy(alpha = 0.15f),
                selectedLabelColor = JobPulsePurple
              )
            )

            DropdownMenu(
              expanded = showModelMenu,
              onDismissRequest = { showModelMenu = false }
            ) {
              DropdownMenuItem(
                text = { Text("gemini-3.5-flash (General Tasks)") },
                onClick = {
                  selectedModel = GeminiChatService.MODEL_GENERAL
                  showModelMenu = false
                }
              )
              DropdownMenuItem(
                text = { Text("gemini-3.1-pro-preview (Complex Tasks)") },
                onClick = {
                  selectedModel = GeminiChatService.MODEL_COMPLEX
                  showModelMenu = false
                }
              )
              DropdownMenuItem(
                text = { Text("gemini-3.1-flash-lite-preview (Fast Tasks)") },
                onClick = {
                  selectedModel = GeminiChatService.MODEL_FAST
                  showModelMenu = false
                }
              )
            }
          }

          IconButton(
            onClick = {
              messages.clear()
              messages.add(
                ChatMessage(
                  role = "model",
                  text = "Chat cleared! What would you like to explore next?",
                  modelUsed = selectedModel
                )
              )
            }
          ) {
            Icon(Icons.Default.Refresh, contentDescription = "Restart Chat", tint = MaterialTheme.colorScheme.onSurfaceVariant)
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface
        )
      )
    },
    bottomBar = {
      Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
          // Quick suggestion chips
          LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 8.dp)
          ) {
            items(promptSuggestions) { suggestion ->
              AssistChip(
                onClick = {
                  currentInputText = suggestion
                },
                label = { Text(suggestion, fontSize = 11.sp) }
              )
            }
          }

          // Input Row
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            OutlinedTextField(
              value = currentInputText,
              onValueChange = { currentInputText = it },
              placeholder = { Text("Ask about resumes, interviews, or salaries...") },
              modifier = Modifier
                .weight(1f)
                .testTag("chat_input_textfield"),
              shape = RoundedCornerShape(24.dp),
              maxLines = 4
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
              onClick = {
                val textToSend = currentInputText.trim()
                if (textToSend.isNotBlank() && !isSending) {
                  currentInputText = ""
                  val userMsg = ChatMessage(role = "user", text = textToSend)
                  messages.add(userMsg)

                  scope.launch {
                    isSending = true
                    val reply = GeminiChatService.sendMessage(
                      history = messages.toList(),
                      newUserMessage = textToSend,
                      selectedModel = selectedModel
                    )
                    messages.add(reply)
                    isSending = false
                  }
                }
              },
              enabled = currentInputText.isNotBlank() && !isSending,
              modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(if (currentInputText.isNotBlank() && !isSending) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                .testTag("chat_send_button")
            ) {
              if (isSending) {
                CircularProgressIndicator(
                  color = Color.White,
                  modifier = Modifier.size(20.dp),
                  strokeWidth = 2.dp
                )
              } else {
                Icon(
                  imageVector = Icons.AutoMirrored.Filled.Send,
                  contentDescription = "Send",
                  tint = if (currentInputText.isNotBlank()) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }
        }
      }
    }
  ) { paddingValues ->
    LazyColumn(
      state = listState,
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .testTag("chat_messages_scroll_list"),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      items(messages, key = { it.id }) { message ->
        ChatMessageBubble(message = message)
      }

      if (isSending) {
        item {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(JobPulsePurple.copy(alpha = 0.15f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = JobPulsePurple, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "JobPulse AI is thinking...",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    }
  }
}

@Composable
private fun ChatMessageBubble(message: ChatMessage) {
  val isUser = message.role == "user"
  val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
  val timeStr = timeFormat.format(Date(message.timestamp))

  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
  ) {
    if (!isUser) {
      Box(
        modifier = Modifier
          .size(34.dp)
          .clip(CircleShape)
          .background(JobPulsePurple),
        contentAlignment = Alignment.Center
      ) {
        Icon(Icons.Default.Psychology, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
      }
      Spacer(modifier = Modifier.width(8.dp))
    }

    Column(
      horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
      modifier = Modifier.widthIn(max = 300.dp)
    ) {
      Box(
        modifier = Modifier
          .clip(
            RoundedCornerShape(
              topStart = 16.dp,
              topEnd = 16.dp,
              bottomStart = if (isUser) 16.dp else 4.dp,
              bottomEnd = if (isUser) 4.dp else 16.dp
            )
          )
          .background(
            if (isUser) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.surfaceVariant
          )
          .padding(horizontal = 14.dp, vertical = 10.dp)
      ) {
        Text(
          text = message.text,
          color = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
          fontSize = 14.sp,
          lineHeight = 20.sp
        )
      }

      Spacer(modifier = Modifier.height(4.dp))

      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          text = timeStr,
          fontSize = 10.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (!isUser) {
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "• ${message.modelUsed.replace("-preview", "").replace("gemini-", "")}",
            fontSize = 9.sp,
            color = JobPulsePurple,
            fontWeight = FontWeight.Bold
          )
        }
      }
    }

    if (isUser) {
      Spacer(modifier = Modifier.width(8.dp))
      Box(
        modifier = Modifier
          .size(34.dp)
          .clip(CircleShape)
          .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
      ) {
        Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(18.dp))
      }
    }
  }
}
