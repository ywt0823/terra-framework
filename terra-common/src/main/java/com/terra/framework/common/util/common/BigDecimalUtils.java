package com.terra.framework.common.util.common;

import java.math.BigDecimal;

import static java.math.RoundingMode.HALF_DOWN;
import static java.math.RoundingMode.HALF_UP;

/**
 * @author ywt
 * @description
 * @date 2021年08月03日 10:47
 */
public class BigDecimalUtils {

    /**
     * 计算除法运算时的精度。
     */
    public static final int DIVIDE_SCALE_2 = 2;
    public static final int DIVIDE_SCALE_4 = 4;
    public static final int DIVIDE_SCALE_8 = 8;

    public static BigDecimal getBigDecimal(BigDecimal data, int point) {
        if (data == null) {
            return null;
        }
        return data.setScale(point, HALF_UP);
    }

    public static BigDecimal getDivide(BigDecimal total, BigDecimal div, int point) {
        if (total == null || div == null || div.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return total.divide(div, point, HALF_UP);
    }

    /**
     * 除法：向下取数
     *
     * @param total
     * @param div
     * @param point
     * @return
     */
    public static BigDecimal getDivideRoundDown(BigDecimal total, BigDecimal div, int point) {
        if (total == null || div == null || div.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return total.divide(div, point, HALF_DOWN);
    }


    /**
     * 把int和long类型double类型转成BigDecimal方便精确计算
     *
     * @param num1
     * @return
     */
    public static BigDecimal getBigDecimal(Number num1) {
        BigDecimal b1 = null;
        if (num1 instanceof BigDecimal) {
            b1 = (BigDecimal) num1;
        } else {
            b1 = new BigDecimal(num1.toString());
        }
        return b1;
    }

    /**
     * 获得占比 num1/num2
     *
     * @param num1
     * @param num2
     * @param len
     * @return
     */
    public static BigDecimal getBigDecimalRate(Number num1, Number num2, Integer len) {
        if (num1 == null || num2 == null) {
            return null;
        }
        BigDecimal d1 = null;
        BigDecimal d2 = null;
        d1 = getBigDecimal(num1);
        d2 = getBigDecimal(num2);
        if (d1 != null && d2 != null && !d2.equals(BigDecimal.ZERO)) {
            try {
                return d1.divide(d2, len, HALF_UP);
            } catch (Exception e) {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * 获得占比 num1/num2
     *
     * @param num1
     * @param num2
     * @param len
     * @return
     */
    public static Double getRate(Number num1, Number num2, Integer len) {
        BigDecimal res = getBigDecimalRate(num1, num2, len);
        return res == null ? null : res.doubleValue();
    }

    /**
     * 两数相乘
     *
     * @param num1
     * @param num2
     * @return
     */
    public static BigDecimal multiply(Number num1, Number num2) {
        if (num1 == null || num2 == null) {
            return null;
        }
        BigDecimal d1 = null;
        BigDecimal d2 = null;
        d1 = getBigDecimal(num1);
        d2 = getBigDecimal(num2);
        if (d1 != null && d2 != null) {
            try {
                return d1.multiply(d2);
            } catch (Exception e) {
                return null;
            }
        } else {
            return null;
        }
    }

    public static BigDecimal multiply(Number num1, Number num2, int point) {
        BigDecimal multiply = multiply(num1, num2);
        if (multiply != null) {
            return multiply.setScale(point, HALF_UP);
        }
        return null;
    }

    /**
     * 获得两数相减 注意 两数可以都为空
     */
    public static Number getSubValue(Number num1, Number num2) {

        if (num1 == null && num2 == null) {
            return null;
        } else if (num1 == null) {
            if (num2.getClass().isAssignableFrom(BigDecimal.class)) {
                return (BigDecimal.ZERO).subtract((BigDecimal) num2);
            } else if (num2.getClass().isAssignableFrom(Long.class)) {
                return -(Long) num2;
            } else if (num2.getClass().isAssignableFrom(Double.class)) {
                return -(Double) num2;
            } else if (num2.getClass().isAssignableFrom(Integer.class)) {
                return -(Integer) num2;
            }
        } else if (num2 == null) {
            return num1;
        } else if (num1.getClass().isAssignableFrom(BigDecimal.class)) {
            return ((BigDecimal) num1).subtract((BigDecimal) num2);
        } else if (num1.getClass().isAssignableFrom(Long.class)) {
            return (Long) num1 - (Long) num2;
        } else if (num1.getClass().isAssignableFrom(Double.class)) {
            return getBigDecimal(num1).subtract(getBigDecimal(num2))
                    .doubleValue();
        } else if (num1.getClass().isAssignableFrom(Integer.class)) {
            return (Integer) num1 - (Integer) num2;
        }
        return 0;
    }

    /**
     * 获得两数相减 注意 两数可以都为空
     */
    public static Number getAddValue(Number num1, Number num2) {

        if (num1 == null && num2 == null) {
            return null;
        } else if (num1 == null) {
            if (num2.getClass().isAssignableFrom(BigDecimal.class)) {
                return (BigDecimal.ZERO).add((BigDecimal) num2);
            } else if (num2.getClass().isAssignableFrom(Long.class)) {
                return +(Long) num2;
            } else if (num2.getClass().isAssignableFrom(Double.class)) {
                return +(Double) num2;
            } else if (num2.getClass().isAssignableFrom(Integer.class)) {
                return +(Integer) num2;
            }
        } else if (num2 == null) {
            return num1;
        } else if (num1.getClass().isAssignableFrom(BigDecimal.class)) {
            return ((BigDecimal) num1).add((BigDecimal) num2);
        } else if (num1.getClass().isAssignableFrom(Long.class)) {
            return (Long) num1 + (Long) num2;
        } else if (num1.getClass().isAssignableFrom(Double.class)) {
            return getBigDecimal(num1).add(getBigDecimal(num2))
                    .doubleValue();
        } else if (num1.getClass().isAssignableFrom(Integer.class)) {
            return (Integer) num1 + (Integer) num2;
        }
        return null;
    }

    /**
     * 获得多数相加减 注意 两数可以都为空
     */
    public static Number addValue(Number... num) {
        Number result = new BigDecimal(0);
        for (Number number : num) {
            result = getAddValue(result, number);
        }
        return result;
    }

    /**
     * 获得两数环比 注意 两数可以都为空
     */
    public static Double getCompareValue(Number num1, Number num2) {
        return getRate(getSubValue(num1, num2), num2, 8);
    }

    /**
     * 装换double类型到%字符串
     * 如果为空 返回“-”
     */
    public static String getPercentage(Double percent) {
        if (percent != null) {
            return percent * 100 + "%";
        }
        return "-";
    }

    /**
     * 装换double类型到%字符串
     * 如果为空 返回“-”
     */
    public static String getPercentage2Point(BigDecimal percent) {
        if (percent != null) {
            String pointValue = String.format("%.2f", percent.multiply(BigDecimal.valueOf(100)));
            return pointValue + "%";
        }
        return "-";
    }

    /**
     * 获取百分比字符串，保留两位小数
     * 如果为空，返回-
     *
     * @param num1
     * @param num2
     * @return
     */
    public static String getPercentage(Number num1, Number num2) {
        return getPercentage(
                getRate(num1, num2, 4)
        );
    }
}
