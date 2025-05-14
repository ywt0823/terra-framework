package com.terra.framework.common.util.common;

import org.apache.commons.lang3.StringUtils;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.TimeZone;

import static com.terra.framework.common.constant.LocalDateParseConstant.NORM_DATETIME_PATTERN;
import static com.terra.framework.common.constant.LocalDateParseConstant.NORM_DATE_PATTERN;


/**
 * @author ywt
 * @description
 * @date 2021年08月02日 15:54
 */
public final class LocalDateTimeUtils {

    /**
     * 当前时间（默认市区）
     *
     * @return java.time.LocalDateTime
     **/
    public static LocalDateTime now() {
        return LocalDateTime.now();
    }

    /**
     * instant转LocalDateTime（默认时区）
     *
     * @param instant Instant对象
     * @return java.time.LocalDateTime
     **/
    public static LocalDateTime of(Instant instant) {
        return of(instant, ZoneId.systemDefault());
    }

    /**
     * instant转LocalDateTime（UTC时区）
     *
     * @param instant Instant对象
     * @return java.time.LocalDateTime
     **/
    public static LocalDateTime ofUTC(Instant instant) {
        return of(instant, ZoneId.of("UTC"));
    }

    /**
     * ZonedDateTime转LocalDateTime
     *
     * @param zonedDateTime ZonedDateTime对象
     * @return java.time.LocalDateTime
     **/
    public static LocalDateTime of(ZonedDateTime zonedDateTime) {
        if (null == zonedDateTime) {
            return null;
        }
        return zonedDateTime.toLocalDateTime();
    }

    /**
     * Instant转LocalDateTime
     *
     * @param instant Instant对象
     * @param zoneId  时区
     **/
    public static LocalDateTime of(Instant instant, ZoneId zoneId) {
        if (null == instant) {
            return null;
        }
        if (zoneId == null) {
            zoneId = ZoneId.systemDefault();
        }
        return LocalDateTime.ofInstant(instant, zoneId);
    }

    /**
     * Instant转LocalDateTime
     *
     * @param instant  Instant对象
     * @param timeZone 时区
     * @return java.time.LocalDateTime
     **/
    public static LocalDateTime of(Instant instant, TimeZone timeZone) {
        if (null == instant) {
            return null;
        }
        if (timeZone == null) {
            timeZone = TimeZone.getDefault();
        }
        return of(instant, timeZone.toZoneId());
    }

    /**
     * 毫秒转LocalDateTime（使用默认时区）
     *
     * @param epochMilli 从1970-01-01T00:00:00Z开始计数的毫秒数
     * @return java.time.LocalDateTime
     **/
    public static LocalDateTime of(long epochMilli) {
        return of(Instant.ofEpochMilli(epochMilli));
    }

    /**
     * 毫秒转LocalDateTime（使用UTC时区）
     *
     * @param epochMilli 从1970-01-01T00:00:00Z开始计数的毫秒数
     * @return java.time.LocalDateTime
     **/
    public static LocalDateTime ofUTC(long epochMilli) {
        return ofUTC(Instant.ofEpochMilli(epochMilli));
    }

    /**
     * 毫秒转LocalDateTime
     *
     * @param epochMilli 从1970-01-01T00:00:00Z开始计数的毫秒数
     * @param zoneId     时区
     * @return java.time.LocalDateTime
     **/
    public static LocalDateTime of(long epochMilli, ZoneId zoneId) {
        return of(Instant.ofEpochMilli(epochMilli), zoneId);
    }

    /**
     * 毫秒转LocalDateTime
     *
     * @param epochMilli 从1970-01-01T00:00:00Z开始计数的毫秒数
     * @param timeZone   时区
     * @return java.time.LocalDateTime
     */
    public static LocalDateTime of(long epochMilli, TimeZone timeZone) {
        return of(Instant.ofEpochMilli(epochMilli), timeZone);
    }

    /**
     * Date转LocalDateTime（使用默认时区）
     *
     * @param date Date对象
     * @return java.time.LocalDateTime
     */
    public static LocalDateTime of(Date date) {
        if (null == date) {
            return null;
        }
        return of(date.toInstant());
    }

    /**
     * 解析日期时间字符串（仅支持yyyy-MM-dd'T'HH:mm:ss格式，例如：2007-12-03T10:15:30）
     *
     * @param text 日期时间字符串
     * @return java.time.LocalDateTime
     */
    public static LocalDateTime parse(CharSequence text) {
        return parse(text, (DateTimeFormatter) null);
    }

    /**
     * 解析日期时间字符串为LocalDateTime
     *
     * @param text      日期时间字符串
     * @param formatter 日期格式化器
     * @return java.time.LocalDateTime
     */
    public static LocalDateTime parse(CharSequence text, DateTimeFormatter formatter) {
        if (null == text) {
            return null;
        }
        if (null == formatter) {
            return LocalDateTime.parse(text);
        }
        return LocalDateTime.parse(text, formatter);
    }

    /**
     * 解析日期时间字符串为LocalDateTime
     *
     * @param text   日期时间字符串
     * @param format 日期格式，类似于yyyy-MM-dd HH:mm:ss,SSS
     * @return java.time.LocalDateTime
     */
    public static LocalDateTime parse(CharSequence text, String format) {
        if (null == text) {
            return null;
        }
        if (StringUtils.isBlank(format)) {
            return null;
        }
        return parse(text, DateTimeFormatter.ofPattern(format));
    }

    /**
     * 解析日期时间字符串为LocalDate（仅支持yyyy-MM-dd'T'HH:mm:ss格式，例如：2007-12-03T10:15:30）
     *
     * @param text 日期时间字符串
     * @return java.time.LocalDate
     **/
    public static LocalDate parseDate(CharSequence text) {
        return parseDate(text, (DateTimeFormatter) null);
    }

    /**
     * 解析日期时间字符串为LocalDate（格式支持日期）
     *
     * @param text      日期时间字符串
     * @param formatter 日期格式化器
     * @return java.time.LocalDate
     */
    public static LocalDate parseDate(CharSequence text, DateTimeFormatter formatter) {
        if (null == text) {
            return null;
        }
        if (null == formatter) {
            return LocalDate.parse(text);
        }
        return LocalDate.parse(text, formatter);
    }

    /**
     * 解析日期字符串为LocalDate
     *
     * @param text   日期字符串
     * @param format 日期格式，类似于yyyy-MM-dd
     * @return java.time.LocalDate
     */
    public static LocalDate parseDate(CharSequence text, String format) {
        if (null == text) {
            return null;
        }
        if (StringUtils.isBlank(format)) {
            return null;
        }
        return parseDate(text, DateTimeFormatter.ofPattern(format));
    }

    /**
     * 格式化日期时间为yyyy-MM-dd HH:mm:ss格式
     *
     * @param time 时间
     * @return java.lang.String
     **/
    public static String formatNormal(LocalDateTime time) {
        if (time == null) {
            return null;
        }
        return format(time, DateTimeFormatter.ofPattern(NORM_DATETIME_PATTERN));
    }

    /**
     * 格式化日期时间为指定格式
     *
     * @param time      时间
     * @param formatter 日期格式化器
     * @return java.lang.String
     **/
    public static String format(LocalDateTime time, DateTimeFormatter formatter) {
        if (null == time) {
            return null;
        }
        if (null == formatter) {
            formatter = DateTimeFormatter.ofPattern(NORM_DATETIME_PATTERN);
        }
        return formatter.format(time);
    }

    /**
     * 格式化日期时间为指定格式
     *
     * @param time   时间
     * @param format 日期格式，类似于yyyy-MM-dd HH:mm:ss,SSS
     * @return java.lang.String
     */
    public static String format(LocalDateTime time, String format) {
        if (null == time) {
            return null;
        }
        if (StringUtils.isBlank(format)) {
            format = NORM_DATETIME_PATTERN;
        }
        return format(time, DateTimeFormatter.ofPattern(format));
    }

    /**
     * 格式化日期时间为yyyy-MM-dd格式
     *
     * @param date LocalDate对象
     * @return java.lang.String
     **/
    public static String formatNormal(LocalDate date) {
        if (date == null) {
            return null;
        }
        return format(date, DateTimeFormatter.ofPattern(NORM_DATE_PATTERN));
    }

    /**
     * 格式化日期时间为指定格式
     *
     * @param date      LocalDate对象
     * @param formatter 日期格式化器
     * @return java.lang.String
     **/
    public static String format(LocalDate date, DateTimeFormatter formatter) {
        if (date == null) {
            return null;
        }
        if (formatter == null) {
            formatter = DateTimeFormatter.ofPattern(NORM_DATE_PATTERN);
        }
        return formatter.format(date);
    }

    /**
     * 格式化日期时间为指定格式
     *
     * @param date   LocalDate对象
     * @param format 日期格式化器
     * @return java.lang.String
     **/
    public static String format(LocalDate date, String format) {
        if (null == date) {
            return null;
        }
        if (StringUtils.isBlank(format)) {
            format = NORM_DATE_PATTERN;
        }
        return format(date, DateTimeFormatter.ofPattern(format));
    }

    /**
     * 日期偏移（不会修改传入的对象）
     *
     * @param time   LocalDateTime对象
     * @param number 偏移量，正数为向后偏移，负数为向前偏移
     * @param unit   偏移单位，如下
     *               ChronoUnit.MONTHS：月
     *               ChronoUnit.DAYS：天
     *               ChronoUnit.HOURS：小时
     *               ChronoUnit.MINUTES：分
     *               ChronoUnit.SECONDS：秒
     * @return java.time.LocalDateTime
     */
    public static LocalDateTime offset(LocalDateTime time, long number, ChronoUnit unit) {
        if (null == time) {
            return null;
        }
        return time.plus(number, unit);
    }

    /**
     * 计算当前时间偏移指定年数的日期
     *
     * @param time   LocalDateTime对象
     * @param number 偏移量，正数为向后偏移，负数为向前偏移
     * @return java.time.LocalDateTime
     **/
    public static LocalDateTime offsetYears(LocalDateTime time, int number) {
        return offset(time, number, ChronoUnit.YEARS);
    }

    /**
     * 计算当前时间偏移指定月数的日期
     *
     * @param time   LocalDateTime对象
     * @param number 偏移量，正数为向后偏移，负数为向前偏移
     * @return java.time.LocalDateTime
     **/
    public static LocalDateTime offsetMonths(LocalDateTime time, int number) {
        return offset(time, number, ChronoUnit.MONTHS);
    }

    /**
     * 计算当前时间偏移指定天数的日期
     *
     * @param time   LocalDateTime对象
     * @param number 偏移量，正数为向后偏移，负数为向前偏移
     * @return java.time.LocalDateTime
     **/
    public static LocalDateTime offsetDays(LocalDateTime time, int number) {
        return offset(time, number, ChronoUnit.DAYS);
    }


    /**
     * 计算当前时间偏移指定小时的日期
     *
     * @param time   LocalDateTime对象
     * @param number 偏移量，正数为向后偏移，负数为向前偏移
     * @return java.time.LocalDateTime
     **/
    public static LocalDateTime offsetHours(LocalDateTime time, int number) {
        return offset(time, number, ChronoUnit.HOURS);
    }

    /**
     * 计算当前时间偏移指定分钟的日期
     *
     * @param time   LocalDateTime对象
     * @param number 偏移量，正数为向后偏移，负数为向前偏移
     * @return java.time.LocalDateTime
     **/
    public static LocalDateTime offMinutes(LocalDateTime time, int number) {
        return offset(time, number, ChronoUnit.MINUTES);
    }

    /**
     * 计算当前时间偏移指定分钟的日期
     *
     * @param time   LocalDateTime对象
     * @param number 偏移量，正数为向后偏移，负数为向前偏移
     * @return java.time.LocalDateTime
     **/
    public static LocalDateTime offSeconds(LocalDateTime time, int number) {
        return offset(time, number, ChronoUnit.SECONDS);
    }

    /**
     * 修改为一天的开始时间，例如：2020-02-02 00:00:00,000
     *
     * @param time LocalDateTime对象
     * @return java.time.LocalDateTime
     **/
    public static LocalDateTime beginOfDay(LocalDateTime time) {
        return time.with(LocalTime.MIN);
    }

    /**
     * 修改为一天的结束时间，例如：2020-02-02 23:59:59,999
     *
     * @param time 1
     * @return java.time.LocalDateTime
     **/
    public static LocalDateTime endOfDay(LocalDateTime time) {
        return time.with(LocalTime.MAX);
    }

    /**
     * localDateTime转Date（默认时区）
     *
     * @param localDateTime LocalDateTime对象
     * @return java.util.Date
     **/
    public static Date toDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }
}
