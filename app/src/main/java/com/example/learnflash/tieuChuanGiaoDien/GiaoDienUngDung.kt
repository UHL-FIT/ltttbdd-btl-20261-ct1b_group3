package com.example.learnflash.tieuChuanGiaoDien

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Cấu hình bảng màu giao diện tối
private val BangMauToi = darkColorScheme(
    primary = MauChinhToi,
    background = MauNenToi,
    surface = MauBeMatToi,
    onPrimary = MauChuToi
)

// Cấu hình bảng màu giao diện sáng
private val BangMauSang = lightColorScheme(
    primary = MauChinhSang,
    background = MauNenSang,
    surface = MauBeMatSang,
    onPrimary = MauChuSang
)

// Hàm Composable định nghĩa Theme chung cho ứng dụng
@Composable
fun GiaoDienLearnFlash(
    toanGiaoDienToi: Boolean = isSystemInDarkTheme(),
    mauDong: Boolean = true,
    noiDung: @Composable () -> Unit
) {
    // Xác định bảng màu dựa trên trạng thái Dark Theme và Hỗ trợ màu động
    val bangMau = when {
        mauDong && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (toanGiaoDienToi) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        toanGiaoDienToi -> BangMauToi
        else -> BangMauSang
    }

    val view = LocalView.current

    // Cập nhật màu thanh trạng thái (StatusBar)
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = bangMau.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !toanGiaoDienToi
        }
    }

    // Gắn bảng màu và kiểu chữ vào MaterialTheme
    MaterialTheme(
        colorScheme = bangMau,
        typography = KieuChuUngDung,
        content = noiDung
    )
}
