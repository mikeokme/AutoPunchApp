package com.example.autopunchapp.model

import android.graphics.drawable.Drawable
import java.io.Serializable

/**
 * 应用信息数据类
 * @param packageName 应用包名
 * @param appName 应用名称
 * @param appIcon 应用图标
 * @param isSelected 是否被选中
 */
data class AppInfo(
    val packageName: String,
    val appName: String,
    val appIcon: Drawable,
    var isSelected: Boolean = false
) : Serializable {
    // 由于Drawable不可序列化，重写equals和hashCode方法
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AppInfo

        if (packageName != other.packageName) return false

        return true
    }

    override fun hashCode(): Int {
        return packageName.hashCode()
    }
}