package com.example.learnflash.giaoDien.thanhPhanChung

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

// Thành phần giao diện hiển thị vòng xoay tiến trình khi tải dữ liệu chờ trên 3 giây
@Composable
fun ManHinhChoLoading() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

// Thành phần hộp thoại thông báo lỗi hoặc cảnh báo chung cho toàn ứng dụng
@Composable
fun HopThoaiCanhBaoLoi(
    tieuDe: String,
    noiDung: String,
    onXacNhan: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onXacNhan,
        title = { Text(text = tieuDe) },
        text = { Text(text = noiDung) },
        confirmButton = {
            TextButton(onClick = onXacNhan) {
                Text("Đồng ý")
            }
        }
    )
}
