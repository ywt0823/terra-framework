package com.terra.framework.common.util.sequence;

import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.List;

/**
 * <p>雪花算法工具<p/>
 *
 * @author ywt
 */
public class SnowflakeUtils {

    private final SnowflakeSequence snowflakeSequence;

    public SnowflakeUtils(SnowflakeSequence snowflakeSequence) {
        this.snowflakeSequence = snowflakeSequence;
    }

    /**
     * 获取默认的工作ID
     */
    private static Long getWorkId() {
        try {
            String hostAddress = Inet4Address.getLocalHost().getHostAddress();
            int[] ints = StringUtils.toCodePoints(hostAddress);
            int sums = 0;
            for (int b : ints) {
                sums += b;
            }
            return (long) (sums % 32);
        } catch (UnknownHostException e) {
            // 如果获取失败，则使用随机数备用
            return RandomUtils.nextLong(0, 31);
        }
    }

    /**
     * 获取默认的数据中心
     */
    private static Long getDataCenterId() {
        int[] ints = StringUtils.toCodePoints(StringUtils.isNotBlank(SystemUtils.getHostName()) ? SystemUtils.getHostName() : "LOCALHOST");
        int sums = 0;
        for (int i : ints) {
            sums += i;
        }
        return (long) (sums % 32);
    }


    public Long getSnowflakeId() {
        return snowflakeSequence.getId();
    }

    public Long getSnowflakeId(List<String> ips) {
        return snowflakeSequence.getId();
    }

    /**
     * <p>雪花算法<p/>
     *
     * @author ywt
     */
    public static class SnowflakeSequence {

        /**
         * 机器id所占的位数
         */
        private final long workerIdBits = 5L;

        /**
         * 序列在id中占的位数
         */
        private final long sequenceBits = 12L;

        /**
         * 工作机器ID(0~31)
         */
        private final long workerId;

        /**
         * 上次生成ID的时间截
         */
        private long lastTimestamp = -1L;

        //12位的序列号
        private long sequence;

        //长度为5位
        private final long datacenterIdBits = 5L;

        //时间戳需要左移位数 12+5+5=22位
        private final long timestampLeftShift = sequenceBits + workerIdBits + datacenterIdBits;


        /**
         * 构造函数
         *
         * @param sequence 12位序列号
         */
        public SnowflakeSequence(long sequence) {

            // 检查工作ID和数据中心的合法性
            long maxWorkerId = ~(-1L << workerIdBits);
            if (getWorkId() > maxWorkerId || getWorkId() < 0) {
                throw new IllegalArgumentException(String.format("workerId can't be greater than %d or less than 0", maxWorkerId));
            }
            /**
             * 数据标识id所占的位数
             */
            long dataCenterIdBits = 5L;
            long maxDataCenterId = ~(-1L << dataCenterIdBits);
            if (getDataCenterId() > maxDataCenterId || getDataCenterId() < 0) {
                throw new IllegalArgumentException(String.format("dataCenterId can't be greater than %d or less than 0", maxDataCenterId));
            }
            System.out.printf("worker starting. timestamp left shift %d, datacenter id bits %d, worker id bits %d, sequence bits %d, workerid %d",
                    timestampLeftShift, datacenterIdBits, workerIdBits, sequenceBits, getWorkId());
            this.workerId = getWorkId();
            this.sequence = sequence;
        }

        /**
         * <p>获得下一个ID <p/>
         * <p>加了synchronized来保证线程安全<p/>
         *
         * @return SnowflakeId
         */
        public synchronized long getId() {
            long timestamp = timeGen();

            if (timestamp < lastTimestamp) {
                System.err.printf("clock is moving backwards.  Rejecting requests until %d.", lastTimestamp);
                throw new RuntimeException(String.format("Clock moved backwards.  Refusing to generate id for %d milliseconds",
                        lastTimestamp - timestamp));
            }
            //获取当前时间戳如果等于上次时间戳（同一毫秒内），则在序列号加一；否则序列号赋值为0，从0开始。
            if (lastTimestamp == timestamp) {
                /**
                 * 生成序列的掩码，这里为4095 (0b111111111111=0xfff=4095)
                 */
                long sequenceMask = ~(-1L << sequenceBits);
                sequence = (sequence + 1) & sequenceMask;
                if (sequence == 0) {
                    timestamp = tilNextMillis(lastTimestamp);
                }
            } else {
                sequence = 0;
            }

            //将上次时间戳值刷新
            lastTimestamp = timestamp;
            /**
             * 返回结果：
             * (timestamp - twepoch) << timestampLeftShift) 表示将时间戳减去初始时间戳，再左移相应位数
             * (datacenterId << datacenterIdShift) 表示将数据id左移相应位数
             * (workerId << workerIdShift) 表示将工作id左移相应位数
             * | 是按位或运算符，例如：x | y，只有当x，y都为0的时候结果才为0，其它情况结果都为1。
             * 因为个部分只有相应位上的值有意义，其它位上都是0，所以将各部分的值进行 | 运算就能得到最终拼接好的id
             */
            //初始时间戳
            long twepoch = 1288834974657L;
            //工作id需要左移的位数，12位
            //数据id需要左移位数 12+5=17位
            long datacenterIdShift = sequenceBits + workerIdBits;
            return ((timestamp - twepoch) << timestampLeftShift) |
                    (getDataCenterId() << datacenterIdShift) |
                    (workerId << sequenceBits) |
                    sequence;
        }

        /**
         * 阻塞到下一个毫秒，直到获得新的时间戳
         *
         * @param lastTimestamp 上次生成ID的时间截
         * @return 当前时间戳
         */
        protected long tilNextMillis(long lastTimestamp) {
            long timestamp = timeGen();
            while (timestamp <= lastTimestamp) {
                timestamp = timeGen();
            }
            return timestamp;
        }

        /**
         * 返回以毫秒为单位的当前时间
         *
         * @return 当前时间(毫秒)
         */
        protected long timeGen() {
            return System.currentTimeMillis();
        }

    }
}
