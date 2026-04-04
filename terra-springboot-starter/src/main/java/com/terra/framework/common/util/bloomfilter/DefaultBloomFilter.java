package com.terra.framework.common.util.bloomfilter;

import com.google.common.hash.Funnel;

public class DefaultBloomFilter extends BaseBloomFilter<String> {


    public DefaultBloomFilter() {
        super((Funnel<String>) (from, into) -> into.putUnencodedChars(from), 1000000, 0.01);
    }
}
