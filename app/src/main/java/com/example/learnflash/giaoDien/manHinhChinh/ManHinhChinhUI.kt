package com.example.learnflash.giaoDien.manHinhChinh

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.learnflash.duLieu.local.thucThe.DanhMuc

// Thành phần giao diện màn hình chính hiển thị danh mục dạng lưới 2 cột
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManHinhChinhUI(
    viewModel: ManHinhChinhViewModel,
    chuyenHuongDanhSachTu: (String, String) -> Unit,
    chuyenHuongCaiDat: () -> Unit,
    chuyenHuongDanhMuc: () -> Unit
) {
    // Thu thập State từ ViewModel
    val danhSachDanhMuc by viewModel.danhSachDanhMucHienThi.collectAsState()
    val kieuSapXep by viewModel.kieuSapXep.collectAsState()
    val boLocTrangThai by viewModel.boLocTrangThai.collectAsState()
    val thongKe by viewModel.thongKeTuVung.collectAsState()
    val dangKhoiTao by viewModel.dangKhoiTao.collectAsState()
    val loiKhoiTao by viewModel.loiKhoiTao.collectAsState()

    // Khởi tạo SnackbarHostState để hiển thị thông báo lỗi kết nối Firebase
    val snackbarHostState = remember { SnackbarHostState() }

    // Hiển thị SnackBar khi có thông báo lỗi từ Firebase
    LaunchedEffect(loiKhoiTao) {
        if (loiKhoiTao.isNotEmpty()) {
            snackbarHostState.showSnackbar(loiKhoiTao)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    // Tên ứng dụng — hiển thị nổi bật ở dòng chính
                    Text(
                        text = "LearnFlash",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    // Nút điều hướng sang màn hình Quản lý Danh mục kèm label
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { chuyenHuongDanhMuc() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Category,
                            contentDescription = "Quản lý danh mục",
                            modifier = Modifier.size(22.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Danh mục",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    // Nút điều hướng sang màn hình Cài đặt kèm label
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { chuyenHuongCaiDat() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Cài đặt",
                            modifier = Modifier.size(22.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Cài đặt",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        when {
            // Trạng thái đang tải dữ liệu Firebase lần đầu — hiển thị Loading indicator
            dangKhoiTao -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Vòng xoay tiến trình trong khi tải dữ liệu từ Firestore
                        CircularProgressIndicator(
                            modifier = Modifier.size(56.dp),
                            strokeWidth = 4.dp
                        )
                        Text(
                            text = "Đang tải dữ liệu...",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Vui lòng đợi trong giây lát",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            // Trạng thái tải xong — hiển thị lưới danh mục
            else -> {
                Column(
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize()
                ) {
                    // Thanh điều khiển chứa bộ lọc trạng thái và cách sắp xếp danh mục dạng dropdown
                    ThanhDieuKhienDropdown(
                        sapXepHienTai = kieuSapXep,
                        boLocHienTai = boLocTrangThai,
                        onDoiSapXep = { viewModel.doiKieuSapXep(it) },
                        onDoiBoLoc = { viewModel.doiBoLocTrangThai(it) }
                    )

                    if (danhSachDanhMuc.isEmpty()) {
                        // Trạng thái trống — không có danh mục phù hợp bộ lọc hoặc lỗi tải dữ liệu ban đầu
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Văn bản thông báo trạng thái trống hoặc thông báo lỗi hệ thống
                                Text(
                                    text = if (loiKhoiTao.isNotEmpty()) loiKhoiTao else "Không tìm thấy danh mục nào phù hợp",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 24.dp)
                                )
                                // Chỉ hiển thị nút thử lại khi phát hiện có lỗi khởi tạo dữ liệu xảy ra
                                if (loiKhoiTao.isNotEmpty()) {
                                    Button(
                                        onClick = {
                                            viewModel.taiLai()
                                        }
                                    ) {
                                        Text("Thử lại")
                                    }
                                }
                            }
                        }
                    } else {
                        // Lưới 2 cột LazyVerticalGrid hiển thị các ô danh mục đã được lọc
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(danhSachDanhMuc) { mucHienThi ->
                                // Ô danh mục — nhấn để vào danh sách từ của danh mục đó
                                ODanhMuc(
                                    mucHienThi = mucHienThi,
                                    onClick = { chuyenHuongDanhSachTu(mucHienThi.danhMuc.id, mucHienThi.danhMuc.ten) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Thành quan Composable hiển thị một ô danh mục trong lưới kèm trạng thái màu sắc viền và badge
@Composable
fun ODanhMuc(mucHienThi: DanhMucHienThi, onClick: () -> Unit) {
    val daThuoc = mucHienThi.soTuDaThuoc
    val tongSo = mucHienThi.tongSoTu
    val canOn = mucHienThi.soTuCanOnTap

    // Cấu hình viền và nhãn chỉ thị trạng thái học tập của danh mục
    val (mauVien, nhanTrangThai, mauNhan) = when (mucHienThi.trangThai) {
        TrangThaiDanhMuc.TRONG -> Triple(
            MaterialTheme.colorScheme.outlineVariant,
            "Chưa có từ",
            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
        TrangThaiDanhMuc.CHUA_ON -> Triple(
            MaterialTheme.colorScheme.outlineVariant,
            "Chưa ôn tập",
            MaterialTheme.colorScheme.primary
        )
        TrangThaiDanhMuc.DANG_ON -> Triple(
            Color(0xFFFF9800).copy(alpha = 0.5f),
            "Đang ôn tập",
            Color(0xFFE65100)
        )
        TrangThaiDanhMuc.DA_ON -> Triple(
            Color(0xFF4CAF50).copy(alpha = 0.5f),
            "Đã học thuộc",
            Color(0xFF2E7D32)
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.15f)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, mauVien)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            // Hiển thị nhãn số từ cần ôn tập ở góc trên bên phải dạng tròn xinh xắn
            if (canOn > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFC62828))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "$canOn từ",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Cột hiển thị nội dung tên danh mục và tiến trình
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Tên danh mục chủ đề
                Text(
                    text = mucHienThi.danhMuc.ten,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(end = if (canOn > 0) 36.dp else 0.dp)
                )

                // Cột hiển thị các thông tin thống kê tiến độ phụ
                Column {
                    // Badge trạng thái nhỏ gọn, xinh xắn
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(mauNhan.copy(alpha = 0.08f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = nhanTrangThai,
                            style = MaterialTheme.typography.labelSmall,
                            color = mauNhan,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    if (tongSo > 0) {
                        Text(
                            text = "Tiến độ: $daThuoc / $tongSo từ",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = "Chưa có từ vựng",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

// Thành phần thanh điều khiển dạng dropdown gọn gàng chứa bộ lọc trạng thái và sắp xếp
@Composable
fun ThanhDieuKhienDropdown(
    sapXepHienTai: String,
    boLocHienTai: String,
    onDoiSapXep: (String) -> Unit,
    onDoiBoLoc: (String) -> Unit
) {
    // Trạng thái kiểm soát đóng mở của dropdown menu sắp xếp
    var moSapXep by remember { mutableStateOf(false) }
    // Trạng thái kiểm soát đóng mở của dropdown menu bộ lọc trạng thái
    var moBoLoc by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Dropdown lựa chọn sắp xếp tên danh mục
        Box(modifier = Modifier.weight(1f)) {
            val nhanSapXep = if (sapXepHienTai == "A_Z") "Tên A-Z" else "Tên Z-A"
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { moSapXep = true },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Sắp xếp: $nhanSapXep",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null
                    )
                }
            }

            DropdownMenu(
                expanded = moSapXep,
                onDismissRequest = { moSapXep = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Tên A-Z") },
                    onClick = {
                        onDoiSapXep("A_Z")
                        moSapXep = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Tên Z-A") },
                    onClick = {
                        onDoiSapXep("Z_A")
                        moSapXep = false
                    }
                )
            }
        }

        // Dropdown lựa chọn lọc danh mục theo tiến trình học tập
        Box(modifier = Modifier.weight(1f)) {
            val nhanBoLoc = when (boLocHienTai) {
                "CHUA_ON" -> "Chưa ôn"
                "DANG_ON" -> "Đang ôn"
                "DA_ON" -> "Đã ôn"
                else -> "Tất cả"
            }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { moBoLoc = true },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Trạng thái: $nhanBoLoc",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null
                    )
                }
            }

            DropdownMenu(
                expanded = moBoLoc,
                onDismissRequest = { moBoLoc = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Tất cả") },
                    onClick = {
                        onDoiBoLoc("TAT_CA")
                        moBoLoc = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Chưa ôn") },
                    onClick = {
                        onDoiBoLoc("CHUA_ON")
                        moBoLoc = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Đang ôn") },
                    onClick = {
                        onDoiBoLoc("DANG_ON")
                        moBoLoc = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Đã ôn") },
                    onClick = {
                        onDoiBoLoc("DA_ON")
                        moBoLoc = false
                    }
                )
            }
        }
    }
}