package com.quibbler.sevenmusic.utils

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.net.Uri
import android.util.DisplayMetrics
import okhttp3.Response.code
import java.io.File
import java.io.IOException
import java.lang.reflect.Constructor
import java.security.cert.Certificate
import java.util.jar.JarEntry
import java.util.jar.JarFile

/**
 * Package:        com.quibbler.sevenmusic.utils
 * ClassName:      APPUtil
 * Description:    有关APP的信息包括版本号，版本名，签名，安装路径等
 * Author:         11103876
 * CreateDate:     2019/10/7 15:39
 */
class APPUtil private constructor() {
    /**
     * 描述：打开指定包名的APP
     * 
     * @param context
     * @param packageName
     */
    fun openOtherApp(context: Context, packageName: String) {
        val manager = context.getPackageManager()
        val launchIntentForPackage = manager.getLaunchIntentForPackage(packageName)
        if (launchIntentForPackage != null) {
            context.startActivity(launchIntentForPackage)
        }
    }

    companion object {
        /**
         * 描述：获取应用包名
         * 
         * @param context 上下文信息
         * @return 包名
         */
        fun getPackageName(context: Context): String? {
            requireNotNull(context) { "Should not be null" }
            return context.getPackageName()
        }

        /**
         * 描述：获取包名信息
         * 
         * @param context 上下文信息
         * @return 获取包信息
         */
        fun getPackageInfo(context: Context): PackageInfo? {
            val packageManager = context.getPackageManager()
            /** getPackageName()是当前类的包名，0代表获取版本信息  */
            try {
                return packageManager.getPackageInfo(context.getPackageName(), 0)
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }

            return null
        }

        /**
         * 描述：判断应用是否已经启动
         * 
         * @param context 一个context
         * @return boolean
         */
        fun isAppAlive(context: Context): Boolean {
            val activityManager =
                context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val processInfos = activityManager.getRunningAppProcesses()
            for (i in processInfos.indices) {
                if (processInfos.get(i)!!.processName == getPackageName(context)) {
                    return true
                }
            }
            return false
        }

        /**
         * 描述：获取应用版本号
         * 
         * @param context
         * @return 成功返回版本号，失败返回-1
         */
        fun getVersionCode(context: Context): Int {
            if (getPackageInfo(context) != null) {
                return getPackageInfo(context)!!.versionCode
            }

            return -1
        }

        /**
         * 描述：获取应用版本名
         * 
         * @param context
         * @return 成功返回版本名， 失败返回null
         */
        fun getVersionName(context: Context): String? {
            if (getPackageInfo(context) != null) {
                return getPackageInfo(context)!!.versionName
            }

            return null
        }

        /**
         * 描述：获取APP名称
         * 
         * @param context
         * @return
         */
        fun getAppName(context: Context): String? {
            try {
                val packageManager = context.getPackageManager()
                val packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0)
                val labelRes = packageInfo.applicationInfo.labelRes
                return context.getResources().getString(labelRes)
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }
            return null
        }

        /**
         * 描述：判断当前ＡＰＰ处于前台还是后台；
         * 需添加<uses-permission android:name="android.permission.GET_TASKS"></uses-permission>
         * 并且必须是系统应用，该方法才有效
         * 
         * @param context
         * @return
         */
        fun isApplicationBackground(context: Context): Boolean {
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            @Suppress("deprecation") val tasks = am.getRunningTasks(1)
            if (tasks != null) {
                val topActivity = tasks.get(0)!!.topActivity
                if (topActivity!!.getPackageName() != context.getPackageName()) {
                    return true
                }
            }
            return false
        }


        /**
         * 描述：安装APK
         * 
         * @param context
         * @param apkPath 安装包的路径
         */
        fun installApk(context: Context, apkPath: Uri?) {
            val intent = Intent()
            intent.setAction(Intent.ACTION_VIEW)
            //        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setDataAndType(apkPath, "application/vnd.android.package-archive")
            context.startActivity(intent)
        }

        /**
         * 描述：卸载APP
         * 
         * @param context
         * @param packageName 包名
         */
        fun uninstallAPK(context: Context, packageName: String?) {
            val packageURI = Uri.parse("package:" + packageName)
            val uninstallIntent = Intent(Intent.ACTION_DELETE, packageURI)
            context.startActivity(uninstallIntent)
        }

        /**
         * 描述：获取已安装应用的签名
         * 
         * @param context
         * @return
         */
        fun getInstalledApkSign(context: Context): String? {
            val packageInfo: PackageInfo
            try {
                val packageManager = context.getPackageManager()
                packageInfo = packageManager.getPackageInfo(
                    context.getPackageName(),
                    PackageManager.GET_SIGNATURES
                )

                return packageInfo.signatures[0].toCharsString()
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }
            return null
        }

        /**
         * 描述：获取当前应用的 源Apk路径
         * 
         * @param context
         * @return
         */
        fun getOldApkSrcPath(context: Context): String? {
            try {
                val packageManager = context.getPackageManager()
                val applicationInfo = packageManager.getApplicationInfo(context.getPackageName(), 0)

                return applicationInfo.sourceDir
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }

            return null
        }

        /**
         * 描述：获取未安装APK的签名
         * 
         * @param file
         * @return
         * @throws IOException
         */
        fun getUninstallApkSignatures(file: File?): MutableList<String?> {
            val signatures: MutableList<String?> = ArrayList<String?>()
            try {
                val jarFile = JarFile(file)
                val je = jarFile.getJarEntry("AndroidManifest.xml")
                val readBuffer = ByteArray(8192)
                val certs: Array<Certificate>? = loadCertificates(jarFile, je, readBuffer)
                if (certs != null) {
                    for (c in certs) {
                        val sig: String = toCharsString(c.getEncoded())
                        signatures.add(sig)
                    }
                }
            } catch (ex: Exception) {
            }
            return signatures
        }

        /**
         * 描述：加载签名
         * 
         * @param jarFile
         * @param je
         * @param readBuffer
         * @return
         */
        private fun loadCertificates(
            jarFile: JarFile,
            je: JarEntry?,
            readBuffer: ByteArray
        ): Array<Certificate>? {
            try {
                val `is` = jarFile.getInputStream(je)
                while (`is`.read(readBuffer, 0, readBuffer.size) != -1) {
                }
                `is`.close()
                return if (je != null) je.getCertificates() else null
            } catch (e: IOException) {
            }
            return null
        }

        /**
         * 描述：将签名转成转成可见字符串
         * 
         * @param sigBytes
         * @return
         */
        private fun toCharsString(sigBytes: ByteArray): String {
            val sig = sigBytes
            val N = sig.size
            val N2 = N * 2
            val text = CharArray(N2)
            for (j in 0..<N) {
                val v = sig[j]
                var d = (v.toInt() shr 4) and 0xf
                text[j * 2] = (if (d >= 10) ('a'.code + d - 10) else ('0'.code + d)).toChar()
                d = v.toInt() and 0xf
                text[j * 2 + 1] = (if (d >= 10) ('a'.code + d - 10) else ('0'.code + d)).toChar()
            }
            return String(text)
        }

        /**
         * 描述：获取未安装APK的签名, 由于市面上的Android系统版本各一，不推荐使用这种方法获取应用签名
         * 
         * @param apkPath
         * @return
         */
        fun getUninstallAPKSignatures(apkPath: String): String? {
            //参数列表类型
            var typeArgs = arrayOfNulls<Class<*>>(1)
            //参数列表值
            var valueArgs = arrayOfNulls<Any>(1)
            try {
                //2.这是一个Package 解释器, 是隐藏的，获取PackageParser的类
                val pkgParserCls = Class.forName("android.content.pm.PackageParser")

                //2.创建PackageParser的实例
                typeArgs[0] = String::class.java
                val pkgParserCt: Constructor<*> = pkgParserCls.getConstructor(*typeArgs)
                valueArgs[0] = apkPath
                val pkgParser: Any = pkgParserCt.newInstance(*valueArgs)

                //3.获取PackageParser的类的  parsePackage方法；PackageParser.Package mPkgInfo = packageParser.parsePackage(new File(apkPath), apkPath, metrics, 0);   
                typeArgs = arrayOfNulls<Class<*>>(4)
                typeArgs[0] = File::class.java
                typeArgs[1] = String::class.java
                typeArgs[2] = DisplayMetrics::class.java
                typeArgs[3] = Int::class.javaPrimitiveType
                val pkgParser_parsePackageMtd =
                    pkgParserCls.getDeclaredMethod("parsePackage", *typeArgs)

                //4.执行parsePackage方法
                valueArgs = arrayOfNulls<Any>(4)
                valueArgs[0] = File(apkPath)
                valueArgs[1] = apkPath
                val metrics = DisplayMetrics()
                metrics.setToDefaults()
                valueArgs[2] = metrics
                valueArgs[3] = PackageManager.GET_SIGNATURES
                val pkgParserPkg = pkgParser_parsePackageMtd.invoke(pkgParser, *valueArgs)

                //5.获取PackageParser类的collectCertificates方法
                typeArgs = arrayOfNulls<Class<*>>(2)
                typeArgs[0] = pkgParserPkg!!.javaClass
                typeArgs[1] = Int::class.javaPrimitiveType
                val pkgParser_collectCertificatesMtd =
                    pkgParserCls.getDeclaredMethod("collectCertificates", *typeArgs)

                //6.执行collectCertificates方法
                valueArgs = arrayOfNulls<Any>(2)
                valueArgs[0] = pkgParserPkg
                valueArgs[1] = PackageManager.GET_SIGNATURES
                pkgParser_collectCertificatesMtd.invoke(pkgParser, *valueArgs)

                //7.获取PackageParser.Package的类的mSignatures属性
                val packageInfoFld = pkgParserPkg.javaClass.getDeclaredField("mSignatures")

                //8.获取PackageParser.Package的类的mSignatures属性的值
                val info = packageInfoFld.get(pkgParserPkg) as Array<Signature?>?
                return info!![0]!!.toCharsString()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }
    }
}
