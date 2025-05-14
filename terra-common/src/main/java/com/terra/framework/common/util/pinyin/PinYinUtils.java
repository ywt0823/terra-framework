package com.terra.framework.common.util.pinyin;

import com.github.stuxuhai.jpinyin.ChineseHelper;
import com.github.stuxuhai.jpinyin.PinyinFormat;
import com.github.stuxuhai.jpinyin.PinyinHelper;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * @author ywt
 * @description
 * @date 2021年08月02日 16:15
 */
public class PinYinUtils {
    /**
     * 获取字符串拼音的第一个字母
     *
     * @param chinese
     * @return
     */
    public static String toFirstChar(String chinese) {
        Objects.requireNonNull(chinese);
        StringBuilder sb = new StringBuilder();
        loopPinyin(chinese, pinyin -> sb.append(pinyin.charAt(0)));
        return sb.toString().toUpperCase();
    }

    /**
     * 汉字转为拼音
     *
     * @param chinese
     * @return
     */
    public static String toPinyin(String chinese) {
        Objects.requireNonNull(chinese);
        StringBuilder sb = new StringBuilder();
        loopPinyin(chinese, sb::append);
        return sb.toString().toUpperCase();
    }

    private static void loopPinyin(String chinese, Consumer<String> action) {
        for (char c : chinese.toCharArray()) {
            boolean isChinese = ChineseHelper.isChinese(c);
            // 只要中文、字母、数字
            if (!isChinese && !Character.isAlphabetic(c) && !Character.isDigit(c)) {
                continue;
            }
            // 非中文，不用转
            if (!isChinese) {
                action.accept(String.valueOf(c));
                continue;
            }
            // 转拼音
            String[] pinyin = PinyinHelper.convertToPinyinArray(c, PinyinFormat.WITHOUT_TONE);
            // 多音字，默认第一个
            action.accept(pinyin[0]);
        }
    }
}
