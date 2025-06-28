package com.example.autopunchapp.utils

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.util.Log
import com.example.autopunchapp.model.AppInfo

object AppListUtil {
    private const val TAG = "AppListUtil"

    /**
     * 获取所有已安装的应用列表
     */
    fun getInstalledApps(context: Context): List<AppInfo> {
        return try {
            val packageManager = context.packageManager
            val installedApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            
            installedApps
                .filter { appInfo ->
                    // 过滤掉系统应用和当前应用
                    appInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0 ||
                    appInfo.packageName == context.packageName
                }
                .map { appInfo ->
                    AppInfo(
                        packageName = appInfo.packageName,
                        appName = appInfo.loadLabel(packageManager).toString(),
                        appIcon = appInfo.loadIcon(packageManager)
                    )
                }
                .sortedBy { it.appName }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting installed apps", e)
            emptyList()
        }
    }

    /**
     * 根据包名获取应用信息
     */
    fun getAppInfoByPackageName(context: Context, packageName: String): AppInfo? {
        return try {
            val packageManager = context.packageManager
            val appInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
            
            AppInfo(
                packageName = appInfo.packageName,
                appName = appInfo.loadLabel(packageManager).toString(),
                appIcon = appInfo.loadIcon(packageManager)
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error getting app info for $packageName", e)
            null
        }
    }

    /**
     * 检查是否有读取已安装应用的权限
     */
    fun hasQueryAllPackagesPermission(context: Context): Boolean {
        return try {
            context.checkSelfPermission(android.Manifest.permission.QUERY_ALL_PACKAGES) == 
                android.content.pm.PackageManager.PERMISSION_GRANTED
        } catch (e: Exception) {
            Log.e(TAG, "Error checking permission", e)
            false
        }
    }
} 