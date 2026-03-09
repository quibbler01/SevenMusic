package com.quibbler.sevenmusic.bean.song.impl

import android.util.Log
import com.quibbler.sevenmusic.bean.song.ILrcBuilder
import java.io.BufferedReader
import java.io.IOException
import java.io.StringReader
import java.util.Collections

/**
 * 
 * Package:        com.quibbler.sevenmusic.bean.song.impl
 * ClassName:      DefaultLrcBuilder
 * Description:    解析歌词的builder类
 * Author:         lishijun
 * CreateDate:     2019/9/28 19:23
 */
class DefaultLrcBuilder : ILrcBuilder {
    override fun getLrcRows(rawLrc: String?): MutableList<LrcRow?>? {
        if (rawLrc == null || rawLrc.length == 0) {
            Log.e(TAG, "getLrcRows rawLrc null or empty")
            return null
        }
        val reader = StringReader(rawLrc)
        val br = BufferedReader(reader)
        var line: String? = null
        val rows: MutableList<LrcRow?> = ArrayList<LrcRow?>()
        try {
            //循环地读取歌词的每一行
            do {
                line = br.readLine()
                /**
                 * 一行歌词只有一个时间的  例如：徐佳莹   《我好想你》
                 * [01:15.33]我好想你 好想你
                 * 一行歌词有多个时间的  例如：草蜢 《失恋战线联盟》
                 * [02:34.14][01:07.00]当你我不小心又想起她
                 * [02:45.69][02:42.20][02:37.69][01:10.60]就在记忆里画一个叉
                 */
                if (line != null && line.length > 0) {
                    //解析每一行歌词 得到每行歌词的集合，因为有些歌词重复有多个时间，就可以解析出多个歌词行来
                    val lrcRows: MutableList<LrcRow?>? = LrcRow.Companion.createRows(line)
                    if (lrcRows != null && lrcRows.size > 0) {
                        for (row in lrcRows) {
                            rows.add(row)
                        }
                    }
                }
            } while (line != null)

            if (rows.size > 0) {
                // 根据歌词行的时间排序
                Collections.sort<LrcRow?>(rows)
            }
        } catch (e: Exception) {
            Log.e(TAG, "parse exceptioned:" + e.message)
            return null
        } finally {
            try {
                br.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            reader.close()
        }
        return rows
    }

    companion object {
        const val TAG: String = "DefaultLrcBuilder"
    }
}
