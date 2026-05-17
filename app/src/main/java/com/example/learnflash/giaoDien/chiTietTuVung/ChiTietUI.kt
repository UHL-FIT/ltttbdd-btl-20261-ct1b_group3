package com.example.learnflash.giaoDien.chiTietTuVung

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.learnflash.giaoDien.thanhPhanChung.ManHinhChoLoading

// Thành phần giao diện cấu thành màn hình Thêm và Sửa từ vựng
@Composable
fun ChiTietUI(
    viewModel: ChiTietViewModel,
    quayLai: () -> Unit
) {
    // Đọc trạng thái dữ liệu (State) từ ViewModel
    val tuKhoa by viewModel.tuKhoa
    val nghiaTiengViet by viewModel.nghiaTiengViet
    val dangTai by viewModel.dangTai
    val loiNhapLieu by viewModel.loiNhapLieu

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Trường nhập liệu từ khóa
        OutlinedTextField(
            value = tuKhoa,
            onValueChange = { viewModel.capNhatTuKhoa(it) },
            label = { Text("Từ khóa (Tiếng Anh)") },
            modifier = Modifier.fillMaxWidth(),
            isError = loiNhapLieu.isNotEmpty()
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Nút kích hoạt tác vụ tra cứu dữ liệu (Gọi mạng)
        Button(onClick = { viewModel.traCuuApi() }, modifier = Modifier.fillMaxWidth()) {
            Text("Tra cứu ý nghĩa trực tuyến (API)")
        }
        Spacer(modifier = Modifier.height(8.dp))

        // Trường nhập liệu ý nghĩa
        OutlinedTextField(
            value = nghiaTiengViet,
            onValueChange = { viewModel.capNhatNghia(it) },
            label = { Text("Ý nghĩa (Tiếng Việt)") },
            modifier = Modifier.fillMaxWidth(),
            isError = loiNhapLieu.isNotEmpty()
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Hiển thị đoạn văn bản cảnh báo lỗi màu đỏ (Nếu có Validation lỗi)
        if (loiNhapLieu.isNotEmpty()) {
            Text(text = loiNhapLieu, color = Color.Red, modifier = Modifier.padding(bottom = 8.dp))
        }

        // Nút bấm thực thi lệnh lưu dữ liệu
        Button(
            onClick = { viewModel.luuTuVung(hoanThanh = quayLai) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Lưu từ vựng")
        }
    }

    // Hiển thị lớp phủ vòng xoay Loading khi mạng phản hồi trên 3 giây
    if (dangTai) {
        ManHinhChoLoading()
    }
}
