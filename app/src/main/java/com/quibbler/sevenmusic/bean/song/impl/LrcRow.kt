package com.quibbler.sevenmusic.bean.song.impl

import android.util.Log

/**
 * 
 * Package:        com.quibbler.sevenmusic.bean.song
 * ClassName:      LrcRow
 * Description:    一行歌词的实体类
 * Author:         lishijun
 * CreateDate:     2019/9/28 14:47
 */
class LrcRow : Comparable<LrcRow?> {
    /** 该行歌词要开始播放的时间，格式如下：[02:34.14]  */
    var mString: String? = null

    /** 该行歌词要开始播放的时间，由[02:34.14]格式转换为long型，
     * 即将2分34秒14毫秒都转为毫秒后 得到的long型值：time=02*60*1000+34*1000+14
     */
    var mTime: Long = 0

    /** 该行歌词的内容  */
    var mContent: String? = null


    constructor()

    constructor(mString: String, time: Long, content: String?) {
        this.mString = mString
        this.mTime = time
        this.mContent = content
    }

    override fun toString(): String {
        return "[" + mString + " ]" + mContent
    }

    /**
     * 排序的时候，根据歌词的时间来排序
     */
    override fun compareTo(another: LrcRow): Int {
        return (this.mTime - another.mTime).toInt()
    }

    companion object {
        const val TAG: String = "LrcRow"

        //读取歌词的每一行内容，转换为LrcRow，加入到集合中
        fun createRows(standardLrcLine: String): MutableList<LrcRow?>? {
            /**
             * 一行歌词只有一个时间的  例如：徐佳莹   《我好想你》
             * [01:15.33]我好想你 好想你
             * 一行歌词有多个时间的  例如：草蜢 《失恋战线联盟》
             * [02:34.14][01:07.00]当你我不小心又想起她
             * [02:45.69][02:42.20][02:37.69][01:10.60]就在记忆里画一个叉
             */
            try {
                if (standardLrcLine.indexOf("[") != 0 || (standardLrcLine.indexOf("]") != 10 && standardLrcLine.indexOf(
                        "]"
                    ) != 9)
                ) {
                    return null
                }
                //[02:34.14][01:07.00]当你我不小心又想起她
                //找到最后一个 ‘]’ 的位置
                val lastIndexOfRightBracket = standardLrcLine.lastIndexOf("]")
                //歌词内容就是 ‘]’ 的位置之后的文本   eg:   当你我不小心又想起她
                val content =
                    standardLrcLine.substring(lastIndexOfRightBracket + 1, standardLrcLine.length)

                //歌词时间就是 ‘]’ 的位置之前的文本   eg:   [02:34.14][01:07.00]
                /**
                 * 将时间格式转换一下  [mm:ss.SS][mm:ss.SS] 转换为  -mm:ss.SS--mm:ss.SS-
                 * 即：[02:34.14][01:07.00]  转换为      -02:34.14--01:07.00-
                 */
                val times =
                    standardLrcLine.substring(0, lastIndexOfRightBracket + 1).replace("[", "-")
                        .replace("]", "-")
                //通过 ‘-’ 来拆分字符串
                val arrTimes: Array<String>? =
                    times.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val listTimes: MutableList<LrcRow?> = ArrayList<LrcRow?>()
                for (temp in arrTimes!!) {
                    if (temp.trim { it <= ' ' }.length == 0) {
                        continue
                    }
                    /** [02:34.14][01:07.00]当你我不小心又想起她
                     * 
                     * 上面的歌词的就可以拆分为下面两句歌词了
                     * [02:34.14]当你我不小心又想起她
                     * [01:07.00]当你我不小心又想起她
                     */
                    val lrcRow = LrcRow(temp, timeConvert(temp), content)
                    listTimes.add(lrcRow)
                }
                return listTimes
            } catch (e: Exception) {
                Log.e(TAG, "createRows exception:" + e.message)
                return null
            }
        }

        /**
         * 将解析得到的表示时间的字符转化为Long型
         */
        private fun timeConvert(timeString: String): Long {
            //因为给如的字符串的时间格式为XX:XX.XX,返回的long要求是以毫秒为单位
            //将字符串 XX:XX.XX 转换为 XX:XX:XX
            var timeString = timeString
            timeString = timeString.replace('.', ':')
            //将字符串 XX:XX:XX 拆分
            val times =
                timeString.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            // mm:ss:SS
            return (times[0].toInt() * 60 * 1000 +  //分
                    times[1].toInt() * 1000 + times[2].toInt()).toLong() //毫秒
        }
    }
}
