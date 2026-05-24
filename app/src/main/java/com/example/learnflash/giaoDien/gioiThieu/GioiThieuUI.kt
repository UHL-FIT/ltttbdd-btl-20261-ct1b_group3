package com.example.learnflash.giaoDien.gioiThieu

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.learnflash.giaoDien.thanhPhanChung.ManHinhChoLoading

// Thành phần giao diện cấu thành màn hình Giới thiệu ứng dụng
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GioiThieuUI(viewModel: GioiThieuViewModel) {
    // Trích xuất ngữ cảnh hệ thống (Context) để kích hoạt Intent mở PDF
    val context = LocalContext.current

    // Đọc các trạng thái StateFlow từ ViewModel
    val dangXuLy by viewModel.dangXuLy.collectAsState()
    val thongBaoKetQua by viewModel.thongBaoKetQua.collectAsState()

    // Khởi tạo SnackbarHostState để hiển thị thông báo kết quả xuất/nhập
    val snackbarHostState = remember { SnackbarHostState() }

    // Theo dõi StateFlow thông báo và kích hoạt hiển thị SnackBar khi có nội dung mới
    LaunchedEffect(thongBaoKetQua) {
        if (thongBaoKetQua.isNotEmpty()) {
            snackbarHostState.showSnackbar(thongBaoKetQua)
            viewModel.daHienThiThongBao()
        }
    }

    // Khung giao diện Scaffold tích hợp TopAppBar và SnackbarHost
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Giới thiệu") })
        },
        snackbarHost = {
            // Vùng hiển thị thông báo SnackBar phía dưới màn hình
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Tiêu đề và thông tin phiên bản ứng dụng
            Text(
                text = "LearnFlash",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Phiên bản 1.0.0",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Ứng dụng học từ vựng bằng thuật toán lặp ngắt quãng (Spaced Repetition System).",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // Nhóm nút mở tài liệu hướng dẫn PDF
            Text(
                text = "Tài liệu hướng dẫn",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Nút bấm kích hoạt Intent hệ thống mở file PDF từ URL trực tuyến
            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        // URL trỏ đến file PDF hướng dẫn sử dụng thực tế — cập nhật khi deploy
                        data = Uri.parse("https://drive.google.com/file/d/huong_dan_learnflash/view")
                    }
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !dangXuLy
            ) {
                Text("Xem Hướng Dẫn Sử Dụng (PDF)")
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // Nhóm nút xuất/nhập dữ liệu định dạng JSON
            Text(
                text = "Quản lý dữ liệu — JSON",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Hàng nút Xuất và Nhập JSON đặt song song
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Nút kích hoạt tác vụ xuất dữ liệu ra file JSON
                Button(
                    onClick = { viewModel.xuatDuLieuJson(context) },
                    modifier = Modifier.weight(1f),
                    enabled = !dangXuLy
                ) {
                    Text("Xuất JSON")
                }
                // Nút kích hoạt tác vụ nhập dữ liệu từ file JSON
                OutlinedButton(
                    onClick = { viewModel.nhapDuLieuJson(context) },
                    modifier = Modifier.weight(1f),
                    enabled = !dangXuLy
                ) {
                    Text("Nhập JSON")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Nhóm nút xuất/nhập dữ liệu định dạng CSV
            Text(
                text = "Quản lý dữ liệu — CSV",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Hàng nút Xuất và Nhập CSV đặt song song
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Nút kích hoạt tác vụ xuất dữ liệu ra file CSV
                Button(
                    onClick = { viewModel.xuatDuLieuCsv(context) },
                    modifier = Modifier.weight(1f),
                    enabled = !dangXuLy
                ) {
                    Text("Xuất CSV")
                }
                // Nút kích hoạt tác vụ nhập dữ liệu từ file CSV
                OutlinedButton(
                    onClick = { viewModel.nhapDuLieuCsv(context) },
                    modifier = Modifier.weight(1f),
                    enabled = !dangXuLy
                ) {
                    Text("Nhập CSV")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Hiển thị lớp phủ vòng xoay Loading khi đang thực thi tác vụ IO
    if (dangXuLy) {
        ManHinhChoLoading()
    }
}