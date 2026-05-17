package com.example.learnflash.giaoDien.gioiThieu

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

// Thành phần giao diện màn hình Giới thiệu ứng dụng
@Composable
fun GioiThieuUI(viewModel: GioiThieuViewModel) {
    // Trích xuất ngữ cảnh hệ thống (Context) để kích hoạt Intent và Toast
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "LearnFlash", style = MaterialTheme.typography.displayMedium)
        Text(text = "Phiên bản 1.0", style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Ứng dụng hỗ trợ học từ vựng bằng thuật toán lặp ngắt quãng.", style = MaterialTheme.typography.bodyMedium)

        Spacer(modifier = Modifier.height(32.dp))

        // Nút bấm kích hoạt Intent hệ thống để mở liên kết tệp PDF
        Button(
            onClick = {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("https://example.com/huong_dan_su_dung.pdf")
                }
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Xem Hướng Dẫn Sử Dụng (PDF)")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Nút kích hoạt chức năng xuất dữ liệu cấu trúc JSON
        Button(
            onClick = {
                viewModel.xuatDuLieuJson(context) { thongBao ->
                    Toast.makeText(context, thongBao, Toast.LENGTH_LONG).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Xuất dữ liệu hệ thống (Export JSON)")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Nút kích hoạt chức năng phục hồi dữ liệu từ file JSON
        Button(
            onClick = {
                viewModel.nhapDuLieuJson(context) { thongBao ->
                    Toast.makeText(context, thongBao, Toast.LENGTH_LONG).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Nhập dữ liệu hệ thống (Import JSON)")
        }
    }
}
