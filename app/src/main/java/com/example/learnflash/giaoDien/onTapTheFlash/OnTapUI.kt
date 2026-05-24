package com.example.learnflash.giaoDien.onTapTheFlash

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

// Thành phần giao diện tổng thể của màn hình lật thẻ Flashcard
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnTapUI(
    viewModel: OnTapViewModel,
    quayLai: () -> Unit
) {
    // Kích hoạt hàm khởi tạo danh sách ôn tập ngay khi Composable hiển thị lần đầu
    LaunchedEffect(Unit) {
        viewModel.batDauPhienHoc()
    }

    // Đọc các trạng thái tương tác từ ViewModel
    val hoanThanhHoc by viewModel.hoanThanhHoc
    val dangLatThe by viewModel.dangLatThe
    val chiSoHienTai by viewModel.chiSoHienTai
    val tuVungHienTai = viewModel.layTuVungHienTai()

    // Tính góc xoay nội suy (Interpolation) từ 0° đến 180° cho hiệu ứng lật thẻ
    val gocXoay by animateFloatAsState(
        targetValue = if (dangLatThe) 180f else 0f,
        animationSpec = tween(
            durationMillis = 400,
            easing = FastOutSlowInEasing
        ),
        label = "gocXoayThe"
    )

    // Xác định thẻ đang ở nửa sau của vòng xoay (quá 90°) để chuyển nội dung mặt sau
    val dangHienMatSau = gocXoay > 90f

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Ôn tập thẻ") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (hoanThanhHoc) {
                // Khối giao diện thông báo hoàn thành phiên học
                Text(
                    text = "Tuyệt vời!\nBạn đã hoàn thành phiên ôn tập.",
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = quayLai) {
                    Text("Quay về màn hình chính")
                }
            } else if (tuVungHienTai != null) {

                // Nhãn hiển thị tiến độ thẻ hiện tại trong phiên học
                Text(
                    text = "Thẻ ${chiSoHienTai + 1}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Thẻ Flashcard với hiệu ứng lật 3D dùng graphicsLayer và rotationY
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .clickable { viewModel.latThe() }
                        // Áp dụng biến đổi ma trận đồ họa để tạo hiệu ứng xoay quanh trục Y
                        .graphicsLayer {
                            // Cấu hình chiều sâu phối cảnh (Camera Distance) tạo cảm giác 3D
                            cameraDistance = 12f * density
                            // Góc xoay trục Y — khi > 90° sẽ lật ngược hình ảnh
                            rotationY = gocXoay
                        },
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (dangHienMatSau)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surface
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!dangHienMatSau) {
                            // Nội dung mặt trước thẻ: Từ khóa tiếng Anh và phiên âm
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = tuVungHienTai.tuKhoa,
                                    style = MaterialTheme.typography.displaySmall,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                                if (tuVungHienTai.phienAm.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "/${tuVungHienTai.phienAm}/",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Chạm để lật thẻ",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            // Nội dung mặt sau thẻ — xoay ngược 180° để chữ hiển thị đúng chiều
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .padding(16.dp)
                                    // Đảo ngược trục Y để bù lại góc xoay đang ở mặt sau
                                    .graphicsLayer { rotationY = 180f }
                            ) {
                                Text(
                                    text = tuVungHienTai.nghiaTiengViet,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                                if (tuVungHienTai.loaiTu.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "(${tuVungHienTai.loaiTu})",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Hiển thị hai nút đánh giá mức độ ghi nhớ khi thẻ đã được lật sang mặt sau
                if (dangLatThe) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Nút xác nhận chưa nhớ — đưa cấp độ SRS về 0
                        Button(
                            onClick = { viewModel.danhGiaTuVung(false) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Chưa nhớ")
                        }
                        // Nút xác nhận đã nhớ — tăng cấp độ SRS thêm 1
                        Button(
                            onClick = { viewModel.danhGiaTuVung(true) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4CAF50)
                            )
                        ) {
                            Text("Đã nhớ")
                        }
                    }
                }
            }
        }
    }
}