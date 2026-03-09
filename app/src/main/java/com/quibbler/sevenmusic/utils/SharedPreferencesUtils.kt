package com.quibbler.sevenmusic.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * Package:        com.quibbler.mymusicdemo.utils
 * ClassName:      SPUtils
 * Description:    SharedPreferences工具类，用来存储设置参数
 * Author:         guojinliang
 * CreateDate:     2019/9/17 19:59
 */
class SharedPreferencesUtils(context: Context) {
    /**
     * 描述：有参构造函数
     * 
     * @param context
     */
    init {
        mSharedPreferences = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
    }

    /**
     * 描述：保存参数数据
     * 
     * @param key
     * @param data
     */
    fun saveData(key: String?, data: Any) {
        val type = data.javaClass.getSimpleName() // 获取数据类型
        val editor: SharedPreferences.Editor = mSharedPreferences.edit()
        if (type != null) {
            if ("Integer" == type) {
                editor.putInt(key, data as Int)
            } else if ("Boolean" == type) {
                editor.putBoolean(key, data as Boolean)
            } else if ("String" == type) {
                editor.putString(key, data as String)
            } else if ("Float" == type) {
                editor.putFloat(key, data as Float)
            } else if ("Long" == type) {
                editor.putLong(key, data as Long)
            }
        }

        //        editor.commit(); // 同步执行
        editor.apply() // 异步执行
    }

    /**
     * 描述：获取参数数据
     * 
     * @param key
     * @param data
     * @return
     */
    fun getData(key: String?, data: Any): Any? {
        val type = data.javaClass.getSimpleName() // 获取数据类型
        if (type != null) {
            if ("Integer" == type) {
                return mSharedPreferences.getInt(key, data as Int)
            } else if ("Boolean" == type) {
                return mSharedPreferences.getBoolean(key, data as Boolean)
            } else if ("String" == type) {
                return mSharedPreferences.getString(key, data as String)
            } else if ("Float" == type) {
                return mSharedPreferences.getFloat(key, data as Float)
            } else if ("Long" == type) {
                return mSharedPreferences.getLong(key, data as Long)
            }
        }
        return null
    }

    companion object {
        /**
         * 保存参数的文件名
         */
        private const val FILE_NAME = "app_setting_data"

        /**
         * SharedPreferences全局单例
         */
        private val mSharedPreferences: SharedPreferences

        /**
         * SharedPreferencesUtils类单例
         */
        private var instance: SharedPreferencesUtils? = null


        /**
         * 描述：初始化SharedPreferencesUtils类的单例
         * 
         * @param context
         */
        @Synchronized
        fun init(context: Context) {
            if (instance == null) {
                instance = SharedPreferencesUtils(context)
            }
        }

        /**
         * 描述：获取SharedPreferencesUtils类的单例
         * 
         * @return getInstance
         */
        fun getInstance(): SharedPreferencesUtils {
            if (instance == null) {
                throw RuntimeException("SharedPreferencesUtils class should init!")
            }
            return instance!!
        }

        /**
         * 描述：全部清除文件中的内容
         */
        fun clearSharedPreferences() {
            val editor: SharedPreferences.Editor = mSharedPreferences.edit()
            editor.clear().commit()
        }

        /**
         * 描述：清除指定key 的内容
         * 
         * @param key
         */
        fun remove(key: String?) {
            val editor: SharedPreferences.Editor = mSharedPreferences.edit()
            editor.remove(key).commit()
        }
    }
}
