package com.quibbler.sevenmusic.utils

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date

/**
 * 
 * Package:        com.quibbler.sevenmusic.utils
 * ClassName:      DateUtil
 * Description:    日期转换
 * Author:         lishijun
 * CreateDate:     2019/9/25 21:07
 */
object DateUtil {
    // date类型转换为String类型
    // formatType格式为yyyy-MM-dd HH:mm:ss//yyyy年MM月dd日 HH时mm分ss秒
    // data Date类型的时间
    fun dateToString(data: Date, formatType: String?): String {
        return SimpleDateFormat(formatType).format(data)
    }

    // long类型转换为String类型
    // currentTime要转换的long类型的时间
    // formatType要转换的string类型的时间格式
    @Throws(ParseException::class)
    fun longToString(currentTime: Long, formatType: String?): String {
        val date = longToDate(currentTime, formatType) // long类型转成Date类型
        val strTime = dateToString(date, formatType) // date类型转成String
        return strTime
    }

    // string类型转换为date类型
    // strTime要转换的string类型的时间，formatType要转换的格式yyyy-MM-dd HH:mm:ss//yyyy年MM月dd日
    // HH时mm分ss秒，
    // strTime的时间格式必须要与formatType的时间格式相同
    @Throws(ParseException::class)
    fun stringToDate(strTime: String, formatType: String?): Date? {
        val formatter = SimpleDateFormat(formatType)
        var date: Date? = null
        date = formatter.parse(strTime)
        return date
    }

    // long转换为Date类型
    // currentTime要转换的long类型的时间
    // formatType要转换的时间格式yyyy-MM-dd HH:mm:ss//yyyy年MM月dd日 HH时mm分ss秒
    @Throws(ParseException::class)
    fun longToDate(currentTime: Long, formatType: String?): Date {
        val dateOld = Date(currentTime) // 根据long类型的毫秒数生命一个date类型的时间
        val sDateTime = dateToString(dateOld, formatType) // 把date类型的时间转换为string
        val date = stringToDate(sDateTime, formatType) // 把String类型转换为Date类型
        return date!!
    }

    // string类型转换为long类型
    // strTime要转换的String类型的时间
    // formatType时间格式
    // strTime的时间格式和formatType的时间格式必须相同
    @Throws(ParseException::class)
    fun stringToLong(strTime: String, formatType: String?): Long {
        val date = stringToDate(strTime, formatType) // String类型转成date类型
        if (date == null) {
            return 0
        } else {
            val currentTime = dateToLong(date) // date类型转成long类型
            return currentTime
        }
    }

    // date类型转换为long类型
    // date要转换的date类型的时间
    fun dateToLong(date: Date): Long {
        return date.getTime()
    }
}
