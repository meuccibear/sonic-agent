package org.cloud.sonic.agent.common.utils;

import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.text.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * 日期工具类
 *
 * @Description:
 * @Date: Created in 2018/5/30 16:19
 * @Author: Wangll
 */
public class DateUtils {

    /**
     * 日志对象
     */
    private static final Logger logger = LoggerFactory.getLogger(DateUtils.class);

    /**
     * 比较 当前时间 返回 XX秒前 .分钟.小时..  XX天前
     *
     * @param date
     * @return
     */
    public static String formationDate(Date date) {

        String dateString = "";
        // 获取系统当前时间
        Date now = new Date();

        if (date.before(now)) {
            try {
                long endTime = now.getTime();
                long currentTime = date.getTime();
                // 计算两个时间点相差的秒数
                long seconds = (endTime - currentTime);
                if (seconds < 10 * 1000) {
                    dateString = "刚刚";
                } else if (seconds < 60 * 1000) {
                    dateString = seconds / 1000 + "秒前";
                } else if (seconds < 60 * 60 * 1000) {
                    dateString = seconds / 1000 / 60 + "分钟前";
                } else if (seconds < 60 * 60 * 24 * 1000) {
                    dateString = seconds / 1000 / 60 / 60 + "小时前";
                } else if (seconds < 60 * 60 * 24 * 1000 * 30L) {
                    dateString = seconds / 1000 / 60 / 60 / 24 + "天前";
                } else if (date.getYear() == now.getYear()) {//今年并且大于30天显示具体月日
                    dateString = new SimpleDateFormat("MM-dd").format(date.getTime());
                } else if (date.getYear() != now.getYear()) {//大于今年显示年月日
                    dateString = new SimpleDateFormat("yyyy-MM-dd").format(date.getTime());
                } else {
                    dateString = new SimpleDateFormat("yyyy-MM-dd").format(date);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return dateString;

    }

    /**
     * 判断选择的日期是否是本月
     *
     * @param time    选择的时间
     * @param pattern 格式(yyyy-MM)
     * @return
     */
    public static boolean isThisTime(long time, String pattern) {
        Date date = new Date(time);
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        String param = sdf.format(date);//参数时间
        String now = sdf.format(new Date());//当前时间
        if (param.equals(now)) {
            return true;
        }
        return false;
    }

    /**
     * 获取过去第几天的日期
     *
     * @param past
     * @return
     */
    public static String getPastDate(int past, Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) - past);
        Date today = calendar.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String result = sdf.format(today);
        return result;
    }

    public static Date asDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    }

    public static Date asDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    public static LocalDateTime asLocalDateTime(Date date) {
        if (ObjectUtils.notIsEmpty(date)) {
            return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDateTime();
        }
        return null;
    }


    public static String asStr(LocalDateTime localDateTime) {
//        return asStr(0, localDateTime);
        return asStr(4, localDateTime);
    }

    /**
     * 格式
     *
     * @param typeId 类型
     * @param Date   时间
     * @return
     */
    public static String asStr(Integer typeId, Date time) {
        return asStr(typeId, asLocalDateTime(time));
    }

    /**
     * @param typeId        1. 2. 3. 4. 5.
     * @param localDateTime
     * @return
     */
    public static String asStr(Integer typeId, LocalDateTime localDateTime) {
        if (ObjectUtils.isEmpty(localDateTime)) {
            return "";
        }
        String pattern;
        switch (typeId) {
            case 1:
                pattern = "yyyy-MM-dd";
                break;
            case 2:
                pattern = "HH:mm:ss";
                break;
            case 3:
                pattern = "yyyyMMddHHmmss";
                break;
            case 4:
                pattern = "yyyy-MM-dd HH:mm:ss";
                break;
            case 5:
                pattern = "yyyy-MM-ddHH:mm-ss";
                break;
            case 6:
                pattern = "yyyyMMddHH";
                break;
            case 7:
                pattern = "MM-dd\nHH:mm:ss";
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + typeId);
        }
        return localDateTime.format(DateTimeFormatter.ofPattern(pattern));
    }

    public static String asStr(Integer typeId, LocalDate localDateTime) {
        if (ObjectUtils.isEmpty(localDateTime)) {
            return "";
        }
        String pattern;
        switch (typeId) {
            case 1:
                pattern = "yyyy-MM-dd";
                break;
            case 2:
                pattern = "HH:mm:ss";
                break;
            case 3:
                pattern = "yyyyMMddHHmmss";
                break;
            case 4:
                pattern = "yyyy-MM-dd HH:mm:ss";
                break;
            case 5:
                pattern = "yyyy-MM-ddHH:mm-ss";
                break;
            case 6:
                pattern = "yyyyMMddHH";
                break;
            case 7:
                pattern = "MM-dd\nHH:mm:ss";
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + typeId);
        }
        return localDateTime.format(DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * 传入Data类型日期，返回字符串类型时间（ISO8601标准时间）
     *
     * @param typeId
     * @param localDateTime
     * @param ID            时间区域 UTC、"Asia/Shanghai"、"Asia/Chongqing"
     * @return
     */
    public static String asStr(LocalDateTime localDateTime, String ID) {
//        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
//        df.setTimeZone(TimeZone.getTimeZone(ID));
//        localDateTime.atOffset(ZoneOffset.UTC);
        return localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"));
//        return df.formatKV(date);
    }

    /**
     * 无符号格式
     *
     * @param localDateTime
     * @return
     */
    public static String asNoSymbolString(LocalDateTime localDateTime) {
        return localDateTime.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

    ;

    /**
     * 计算耗时
     * eg:
     * //日志开始时间
     * LocalDateTime beginDate = LocalDateTime.now();
     * logger.debug(DateUtils.calculationTimeConsuming("微 信 支 付 开 始 回 调", beginDate));
     *
     * @param now 开始时间
     * @return 提示消息
     */
    public static String calculationTimeConsuming(LocalDateTime now) {
        if (null != now) {
            LocalDateTime end = LocalDateTime.now();
            Duration duration = Duration.between(now, end);
            long days = duration.toDays(); //相差的天数
            long hours = duration.toHours();//相差的小时数
            long minutes = duration.toMinutes();//相差的分钟数
            long millis = duration.toMillis();//相差毫秒数
            long nanos = duration.toNanos();//相差的纳秒数
            StringBuffer sb = new StringBuffer("耗时【 ");
            if (days > 0) {
                sb.append(days + " d");
            }
            if (hours > 0) {
                sb.append(hours + " h ");
            }
            if (minutes > 0) {
                sb.append(minutes + " m ");
            }
            if (millis > 0) {
                sb.append(millis + " ms ");
            }
            if (nanos > 0) {
                String millisStr = String.valueOf(millis);
                String nanosStr = String.valueOf(nanos);

                sb.append(nanosStr.substring(millisStr.length(), nanosStr.length()) + " ns");
            }
            return sb.append("】").toString();
        }
        return "";
    }

    /**
     * 判断当前时间距离第二天凌晨的秒数
     *
     * @return 返回值单位为[s:秒]
     */
    public static Long getSecondsNextEarlyMorning() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return (cal.getTimeInMillis() - System.currentTimeMillis()) / 1000;
    }


    public static String toAccurateDataStr() {
        long time = System.currentTimeMillis();
        Date d = new Date(time);
        Timestamp t = new Timestamp(time);
        t.setNanos(000000);
        System.out.println(d);
        System.out.println(t);
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'.'");
        NumberFormat nf = new DecimalFormat("000000");
        return df.format(t.getTime()) + nf.format(t.getNanos() + "Z");
    }

    /**
     * @param dateTime
     * @return
     */
    @SneakyThrows
    public static Date toDate(String dateTime) {
//        System.out.println(dateTime);
        int dateTimeIndex = dateTime.lastIndexOf(".");
        if (dateTimeIndex > -1) {
            dateTime = dateTime.substring(0, dateTimeIndex);
        }

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date;
        try {
            date = df.parse(dateTime);
        } catch (ParseException e) {
            System.out.println(dateTime + "  错误");
            return new Date();
        }
//        Timestamp t = new Timestamp(date.getTime());
//        t.setNanos(123121);
//        NumberFormat nf = new DecimalFormat("000000");
//        System.out.println(df.formatKV(t.getTime()) + nf.formatKV(t.getNanos()));

        return utcToLocal(date);
    }


    /**
     * <p>Description:UTC时间转化为本地时间 </p>
     *
     * @param utcTime
     * @return
     * @author wgs
     * @date 2018年10月19日 下午2:23:24
     */
    public static Date utcToLocal(Date utcDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getDefault());
        Date locatlDate = null;
        String localTime = sdf.format(utcDate.getTime());
        try {
            locatlDate = sdf.parse(localTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return locatlDate;
    }

    /**
     * @throws
     * @title 获取毫秒
     * @description
     * @author Mr.Lv lvzhuozhuang@foxmail.com
     * @updateTime 2022/7/28 17:26
     */
    public static Long localDateTimeToTimestamp(LocalDateTime localDateTime) {
        try {
            ZoneId zoneId = ZoneId.systemDefault();
            Instant instant = localDateTime.atZone(zoneId).toInstant();
            return instant.toEpochMilli();//毫秒时间戳
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}


//LocalDateTime localDateTime = LocalDateTime.now();
//        //时间运算，相加相减
//        System.out.println(localDateTime.plusYears(2)); //加2年
//        System.out.println(localDateTime.plusDays(2)); //加两天
//        System.out.println(localDateTime.minusYears(2)); //减两年
//        System.out.println(localDateTime.minusDays(2)); //减两天
//        System.out.println(localDateTime.toString());    // 转字符串 结果：2021-11-24T15:36:12.914
//        System.out.println(localDateTime.toLocalDate()); //获取日期(LocalDate) 结果：2021-11-24
//        System.out.println(localDateTime.toLocalTime()); //获取时间(LocalTime) 结果：15:36:12.914
//        System.out.println(localDateTime.getDayOfMonth()); //获取当前时间月份的第几天 结果：24
//        System.out.println(localDateTime.getDayOfWeek());  //获取当前周的第几天       结果：WEDNESDAY
//        System.out.println(localDateTime.getDayOfYear());  //获取当前时间在该年属于第几天 结果：328
//        System.out.println(localDateTime.getMonthValue()); // 获取当前时间的月份(阿拉伯文) 结果：11
//        System.out.println(localDateTime.getMonth());      // 获取当前时间的月份(英文) 结果：11
//        System.out.println(localDateTime.getHour());       // 获取当前时间的小时数 结果：15
//        System.out.println(localDateTime.getMinute());     // 获取当前时间的分钟数 结果：36
//        //格式化输出
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("YYYY/MM/dd HH:mm:ss");
//        System.out.println(localDateTime.format(formatter)); // 结果：2021/11/24 15:36:12
//        //构造时间
//        LocalDateTime startTime = LocalDateTime.of(2021, 1, 1, 20, 31, 20);
//        LocalDateTime endTime = LocalDateTime.of(2021, 1, 3, 20, 31, 20);
//        //比较时间
//        System.out.println(localDateTime.isAfter(startTime)); // 结果：true
//        System.out.println(localDateTime.isBefore(endTime));  // 结果：false
//        //获取毫秒数(使用Instant)
//        System.out.println(localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()); // 结果：1637739570129
//        //获取秒数(使用Instant)
//        System.out.println(localDateTime.atZone(ZoneId.systemDefault()).toInstant().getEpochSecond()); // 结果：1637739570
//        // 获取当前时间的0点~23点
//        LocalDateTime beginDay = localDateTime.withHour(0).withMinute(0).withSecond(0).withNano(0);
//        LocalDateTime endDay = localDateTime.withHour(23).withMinute(59).withSecond(59);
//        // 获取本月的第一天的0点0分0秒和最后一天的23点59分59秒
//        LocalDateTime beginMonth = localDateTime.with(TemporalAdjusters.firstDayOfMonth()).withHour(0).withMinute(0).withSecond(0);
//        LocalDateTime endMonth = localDateTime.with(TemporalAdjusters.lastDayOfMonth()).withHour(23).withMinute(59).withSecond(59);
//        // LocalDateTime转Date
//        Date date = Date.from(localDateTime.toInstant(ZoneOffset.of("+8")));
//        // Date转LocalDateTime
//        date.toInstant().atOffset(ZoneOffset.of("+8")).toLocalDateTime();
//        // LocalDateTime获取秒数
//        Long second = LocalDateTime.now().toEpochSecond(ZoneOffset.of("+8"));
//        // LocalDateTime获取毫秒数
//        Long milliSecond = LocalDateTime.now().toInstant(ZoneOffset.of("+8")).toEpochMilli();


// private static final String YYYY_MM_DD_HH_MM_SS =  "yyyy-MM-dd HH:mm:ss";
//    private static final ThreadLocal<SimpleDateFormat> threadLocalDateFormat = ThreadLocal.withInitial(() -> new SimpleDateFormat(YYYY_MM_DD_HH_MM_SS, Locale.US));
//    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(YYYY_MM_DD_HH_MM_SS);
//
//    /**
//     * SimpleDateFormat要结合ThreadLocal使用,避免并发问题
//     */
//    public static String formatBySimpleDateFormat(Date date, String... pattern){
//        SimpleDateFormat simpleDateFormat = threadLocalDateFormat.get();
//        if (pattern.length > 0) {
//            simpleDateFormat.applyPattern(pattern[0]);
//        }
//        return simpleDateFormat.format(date);
//    }
//
//
//    /**
//     * java8新增的方法
//     */
//    public static String formatByDateTimeFormatter(Date date, String... pattern){
//        LocalDateTime localDateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
//        if (pattern.length > 0) {
//            return DateTimeFormatter.ofPattern(pattern[0]).withZone(ZoneId.systemDefault()).format(localDateTime);
//        }
//        return dateTimeFormatter.format(localDateTime);
//    }
//
//    /**
//     * 阿帕奇的方法
//     */
//    public static String formatApacheDateFormatUtils(Date date, String... pattern){
//        if (pattern.length > 0) {
//            return DateFormatUtils.format(date, pattern[0]);
//        }
//        return DateFormatUtils.format(date, YYYY_MM_DD_HH_MM_SS);
//    }
