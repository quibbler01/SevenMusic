package com.quibbler.sevenmusic.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Package:        com.quibbler.mymusicdemo.utils
 * ClassName:      SPUtils
 * Description:    SharedPreferences工具类，用来存储设置参数
 * Author:         guojinliang
 * CreateDate:     2019/9/17 19:59
 */
public class SharedPreferencesUtils {
    /**
     * 保存参数的文件名
     */
    private static final String FILE_NAME = "app_setting_data";
    /**
     * SharedPreferences全局单例
     */
    private static SharedPreferences mSharedPreferences;
    /**
     * SharedPreferencesUtils类单例
     */
    private static SharedPreferencesUtils instance;


    /**
     * 描述：有参构造函数
     *
     * @param context
     */
    public SharedPreferencesUtils(Context context) {
        mSharedPreferences = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
    }

    /**
     * 描述：初始化SharedPreferencesUtils类的单例
     *
     * @param context
     */
    public static synchronized void init(Context context) {
        if (instance == null) {
            instance = new SharedPreferencesUtils(context);
        }
    }

    /**
     * 描述：获取SharedPreferencesUtils类的单例
     *
     * @return getInstance
     */
    public static SharedPreferencesUtils getInstance() {
        if (instance == null) {
            throw new RuntimeException("SharedPreferencesUtils class should init!");
        }
        return instance;
    }

    /**
     * 描述：保存参数数据
     *
     * @param key
     * @param data
     */
    public void saveData(String key, Object data) {
        String type = data.getClass().getSimpleName();    // 获取数据类型
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        if (type != null) {
            if ("Integer".equals(type)) {
                editor.putInt(key, (Integer) data);
            } else if ("Boolean".equals(type)) {
                editor.putBoolean(key, (Boolean) data);
            } else if ("String".equals(type)) {
                editor.putString(key, (String) data);
            } else if ("Float".equals(type)) {
                editor.putFloat(key, (Float) data);
            } else if ("Long".equals(type)) {
                editor.putLong(key, (Long) data);
            }
        }

//        editor.commit(); // 同步执行
        editor.apply(); // 异步执行
    }

    /**
     * 描述：获取参数数据
     *
     * @param key
     * @param data
     * @return
     */
    public Object getData(String key, Object data) {
        String type = data.getClass().getSimpleName();   // 获取数据类型
        if (type != null) {
            if ("Integer".equals(type)) {
                return mSharedPreferences.getInt(key, (Integer) data);
            } else if ("Boolean".equals(type)) {
                return mSharedPreferences.getBoolean(key, (Boolean) data);
            } else if ("String".equals(type)) {
                return mSharedPreferences.getString(key, (String) data);
            } else if ("Float".equals(type)) {
                return mSharedPreferences.getFloat(key, (Float) data);
            } else if ("Long".equals(type)) {
                return mSharedPreferences.getLong(key, (Long) data);
            }
        }
        return null;
    }

    /**
     * 描述：全部清除文件中的内容
     */
    public static void clearSharedPreferences() {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.clear().commit();
    }

    /**
     * 描述：清除指定key 的内容
     *
     * @param key
     */
    public static void remove(String key) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.remove(key).commit();
    }


}
