package com.example.learnflash.dieuHuong

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.learnflash.duLieu.khoDuLieu.KhoDuLieuDanhMuc
import com.example.learnflash.duLieu.khoDuLieu.KhoDuLieuTuVung
import com.example.learnflash.duLieu.local.dataStore.CaiDatDataStore
import com.example.learnflash.giaoDien.caiDat.CaiDatUI
import com.example.learnflash.giaoDien.caiDat.CaiDatViewModel
import com.example.learnflash.giaoDien.chiTietTuVung.ChiTietUI
import com.example.learnflash.giaoDien.chiTietTuVung.ChiTietViewModel
import com.example.learnflash.giaoDien.danhMuc.DanhMucUI
import com.example.learnflash.giaoDien.danhMuc.DanhMucViewModel
import com.example.learnflash.giaoDien.danhSachTuVung.DanhSachTuVungUI
import com.example.learnflash.giaoDien.danhSachTuVung.DanhSachTuVungViewModel
import com.example.learnflash.giaoDien.gioiThieu.GioiThieuUI
import com.example.learnflash.giaoDien.gioiThieu.GioiThieuViewModel
import com.example.learnflash.giaoDien.manHinhChinh.ManHinhChinhUI
import com.example.learnflash.giaoDien.manHinhChinh.ManHinhChinhViewModel
import com.example.learnflash.giaoDien.onTapTheFlash.OnTapUI
import com.example.learnflash.giaoDien.onTapTheFlash.OnTapViewModel
import com.example.learnflash.giaoDien.thongKe.ThongKeUI
import com.example.learnflash.giaoDien.thongKe.ThongKeViewModel
import java.net.URLDecoder
import java.net.URLEncoder

// Hàm Composable định nghĩa cấu trúc khung chuyển hướng màn hình toàn bộ ứng dụng
@Composable
fun DieuHuongApp(
    khoDuLieu: KhoDuLieuTuVung,
    khoDuLieuDanhMuc: KhoDuLieuDanhMuc,
    caiDatDataStore: CaiDatDataStore
) {
    // Khởi tạo bộ điều khiển trạng thái tuyến đường (NavController)
    val navController = rememberNavController()

    // Thu thập trạng thái BackStack để xác định Item nào trên Bottom Navigation Bar đang chọn
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val tuyenHienTai = navBackStackEntry?.destination?.route

    // Danh sách tuyến đường hiển thị Bottom Navigation Bar
    val cacTuyenCoBottomBar = listOf("manHinhChinh", "onTap", "thongKe", "gioiThieu")

    Scaffold(
        bottomBar = {
            // Chỉ hiển thị Bottom Navigation Bar khi ở các màn hình chính
            if (cacTuyenCoBottomBar.any { tuyenHienTai == it }) {
                NavigationBar {
                    // Mục điều hướng Trang chủ
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = "Trang chủ") },
                        label = { Text("Trang chủ") },
                        selected = tuyenHienTai == "manHinhChinh",
                        onClick = { navController.navigate("manHinhChinh") { popUpTo(0) } }
                    )
                    // Mục điều hướng Ôn tập
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.PlayArrow, contentDescription = "Ôn tập") },
                        label = { Text("Ôn tập") },
                        selected = tuyenHienTai == "onTap",
                        onClick = { navController.navigate("onTap") { launchSingleTop = true } }
                    )
                    // Mục điều hướng Thống kê
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.BarChart, contentDescription = "Thống kê") },
                        label = { Text("Thống kê") },
                        selected = tuyenHienTai == "thongKe",
                        onClick = { navController.navigate("thongKe") { launchSingleTop = true } }
                    )
                    // Mục điều hướng Giới thiệu
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Info, contentDescription = "Giới thiệu") },
                        label = { Text("Giới thiệu") },
                        selected = tuyenHienTai == "gioiThieu",
                        onClick = { navController.navigate("gioiThieu") { launchSingleTop = true } }
                    )
                }
            }
        }
    ) { paddingValues ->
        // Thành phần quản lý hệ thống chuyển trang (NavHost)
        NavHost(
            navController = navController,
            startDestination = "manHinhChinh",
            modifier = Modifier.padding(paddingValues)
        ) {
            // Tuyến đường Màn Hình Chính — lưới danh mục
            composable("manHinhChinh") {
                val viewModel: ManHinhChinhViewModel = viewModel {
                    ManHinhChinhViewModel(khoDuLieu, khoDuLieuDanhMuc)
                }
                ManHinhChinhUI(
                    viewModel = viewModel,
                    chuyenHuongDanhSachTu = { danhMucId, tenDanhMuc ->
                        val tenEncode = URLEncoder.encode(tenDanhMuc, "UTF-8")
                        navController.navigate("danhSachTuVung/$danhMucId/$tenEncode")
                    },
                    chuyenHuongCaiDat = { navController.navigate("caiDat") },
                    chuyenHuongDanhMuc = { navController.navigate("danhMuc") }
                )
            }

            // Tuyến đường Danh Sách Từ Vựng theo danh mục — nhận danhMucId và tenDanhMuc
            composable(
                route = "danhSachTuVung/{danhMucId}/{tenDanhMuc}",
                arguments = listOf(
                    navArgument("danhMucId") { type = NavType.StringType },
                    navArgument("tenDanhMuc") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val danhMucId = backStackEntry.arguments?.getString("danhMucId") ?: ""
                val tenDanhMucEncode = backStackEntry.arguments?.getString("tenDanhMuc") ?: ""
                val tenDanhMuc = URLDecoder.decode(tenDanhMucEncode, "UTF-8")
                val viewModel: DanhSachTuVungViewModel = viewModel {
                    DanhSachTuVungViewModel(khoDuLieu, danhMucId, tenDanhMuc)
                }
                DanhSachTuVungUI(
                    viewModel = viewModel,
                    chuyenHuongChiTiet = { id, danhMucIdMacDinh ->
                        navController.navigate("chiTiet/$id/$danhMucIdMacDinh")
                    },
                    // Điều hướng sang màn hình ôn tập với danhMucId cụ thể
                    chuyenHuongOnTap = { danhMucIdOnTap ->
                        val danhMucEncode = URLEncoder.encode(danhMucIdOnTap, "UTF-8")
                        navController.navigate("onTapTheoDanhMuc/$danhMucEncode")
                    },
                    quayLai = { navController.popBackStack() }
                )
            }

            // Tuyến đường Ôn Tập lọc theo danh mục cụ thể
            composable(
                route = "onTapTheoDanhMuc/{danhMucId}",
                arguments = listOf(navArgument("danhMucId") { type = NavType.StringType })
            ) { backStackEntry ->
                val danhMucIdEncode = backStackEntry.arguments?.getString("danhMucId") ?: ""
                val danhMucId = URLDecoder.decode(danhMucIdEncode, "UTF-8")
                val viewModel: OnTapViewModel = viewModel { OnTapViewModel(khoDuLieu, danhMucId) }
                OnTapUI(
                    viewModel = viewModel,
                    quayLai = { navController.popBackStack() }
                )
            }

            // Tuyến đường Màn Hình Chi Tiết — nhận id từ vựng và danhMucId mặc định
            composable(
                route = "chiTiet/{id}/{danhMucIdMacDinh}",
                arguments = listOf(
                    navArgument("id") { type = NavType.IntType },
                    navArgument("danhMucIdMacDinh") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val idTuVung = backStackEntry.arguments?.getInt("id") ?: 0
                val danhMucIdMacDinh = backStackEntry.arguments?.getString("danhMucIdMacDinh") ?: "mac_dinh"
                val viewModel: ChiTietViewModel = viewModel {
                    ChiTietViewModel(khoDuLieu, khoDuLieuDanhMuc, idTuVung, danhMucIdMacDinh)
                }
                ChiTietUI(
                    viewModel = viewModel,
                    quayLai = { navController.popBackStack() }
                )
            }

            // Tuyến đường Màn Hình Ôn Tập Thẻ
            composable("onTap") {
                val viewModel: OnTapViewModel = viewModel { OnTapViewModel(khoDuLieu) }
                OnTapUI(
                    viewModel = viewModel,
                    quayLai = { navController.popBackStack() }
                )
            }

            // Tuyến đường Màn Hình Thống Kê
            composable("thongKe") {
                val viewModel: ThongKeViewModel = viewModel {
                    ThongKeViewModel(khoDuLieu, khoDuLieuDanhMuc)
                }
                ThongKeUI(viewModel = viewModel)
            }

            // Tuyến đường Màn Hình Giới Thiệu
            composable("gioiThieu") {
                val viewModel: GioiThieuViewModel = viewModel { GioiThieuViewModel(khoDuLieu) }
                GioiThieuUI(viewModel = viewModel)
            }

            // Tuyến đường Màn Hình Cài Đặt (không có Bottom Bar)
            composable("caiDat") {
                val viewModel: CaiDatViewModel = viewModel { CaiDatViewModel(caiDatDataStore) }
                CaiDatUI(
                    viewModel = viewModel,
                    quayLai = { navController.popBackStack() }
                )
            }

            // Tuyến đường Màn Hình Quản Lý Danh Mục (không có Bottom Bar)
            composable("danhMuc") {
                val viewModel: DanhMucViewModel = viewModel { DanhMucViewModel(khoDuLieuDanhMuc) }
                DanhMucUI(
                    viewModel = viewModel,
                    quayLai = { navController.popBackStack() }
                )
            }
        }
    }
}