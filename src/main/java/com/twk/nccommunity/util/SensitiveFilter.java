package com.twk.nccommunity.util;

import com.duby.util.TrieFilter.TrieFilter;
import org.springframework.stereotype.Component;


/**
 * 调用别人的API
 */
@Component
public class SensitiveFilter {
    public String goToFilter(String text){
        TrieFilter trieFilter = new TrieFilter();
        trieFilter.batchAdd("sensitive-words.txt");
        return trieFilter.filter(text, '*');
    }
}
