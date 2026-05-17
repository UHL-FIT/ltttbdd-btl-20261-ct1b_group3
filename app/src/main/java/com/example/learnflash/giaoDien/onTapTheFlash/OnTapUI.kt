package com.example.learnflash.giaoDien.onTapTheFlash

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

// Thành phần giao diện tổng thể của màn hình lật thẻ Flashcard
@Composable
fun OnTapUI(
    viewModel: OnTapViewModel,
    quayLai: () -> Unit
) {
    // Kích hoạt hàm khởi tạo danh sách ôn tập ngay khi Compose hiển thị lần đầu
    LaunchedEffect(Unit) {
        viewModel.batDauPhienHoc()
    }

    // Đọc các trạng thái tương tác từ ViewModel
    val hoanThanhHoc by viewModel.hoanThanhHoc
    val dangLatThe by viewModel.dangLatThe
    val tuVungHienTai = viewModel.layTuVungHienTai()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (hoanThanhHoc) {
            // Khối giao diện hiển thị thông báo khi phiên học kết thúc
            Text(
                text = "Tuyệt vời!\nBạn đã hoàn thành phiên ôn tập.",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = quayLai) {
                Text("Quay về màn hình chính")
            }
        } else if (tuVungHienTai != null) {
            // Khối giao diện thẻ Flashcard
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clickable { viewModel.latThe() },
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (!dangLatThe) {
                        // Hiển thị mặt trước thẻ: Từ khóa tiếng Anh
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = tuVungHienTai.tuKhoa, style = MaterialTheme.typography.displaySmall)
                            if (tuVungHienTai.phienAm.isNotEmpty()) {
                                Text(text = "/${tuVungHienTai.phienAm}/", style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    } else {
                        // Hiển thị mặt sau thẻ: Ý nghĩa tiếng Việt
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = tuVungHienTai.nghiaTiengViet, style = MaterialTheme.typography.headlineMedium)
                            if (tuVungHienTai.loaiTu.isNotEmpty()) {
                                Text(text = "(${tuVungHienTai.loaiTu})", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Hiển thị hai nút chức năng đánh giá mức độ ghi nhớ khi thẻ đã được lật
            if (dangLatThe) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = { viewModel.danhGiaTuVung(false) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) {
                        Text("Chưa nhớ")
                    }
                    Button(
                        onClick = { viewModel.danhGiaTuVung(true) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Text("Đã nhớ")
                    }
                }
            } else {
                Text(text = "Chạm vào thẻ để lật", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
