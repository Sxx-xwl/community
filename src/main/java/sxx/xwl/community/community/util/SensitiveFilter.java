package sxx.xwl.community.community.util;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * @author sxx_27
 * @create 2022-04-26 11:05
 */
@Component
public class SensitiveFilter {
    //日志
    private static final Logger LOGGER = LoggerFactory.getLogger(SensitiveFilter.class);
    //替换词
    private static final String REPLACEMENT = "***";
    //根节点
    private TrieNode rootNode = new TrieNode();

    //初始化树
    @PostConstruct
    public void init() {
        try (
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        ) {
            String keyword;
            while ((keyword = reader.readLine()) != null) {
                this.addKeyword(keyword);
            }
        } catch (IOException e) {
            LOGGER.error("加载敏感词文件失败！" + e.getMessage());
        }
    }

    /**
     * 过滤敏感词
     *
     * @param text 待过滤文本
     * @return {@link String}  过滤后的文本
     * @author sxx
     * <br>CreateDate 2022-04-26 14:41
     */
    public String filter(String text) {
        if (StringUtils.isBlank(text)) {
            return null;
        }
        //指针一 指向树
        TrieNode tempNode = rootNode;
        //指针二 指向起始点
        int begin = 0;
        //指针三 指向结束点
        int end = 0;
        //记录结果
        StringBuilder res = new StringBuilder();
        while (begin < text.length()) {
            if (end < text.length()) {
                char c = text.charAt(end);
                //跳过符号
                if (isSymbol(c)) {
                    //若指针1 处于根节点，跳过
                    if (tempNode == rootNode) {
                        res.append(c);
                        begin++;
                    }
                    end++;
                    continue;
                }
                //检查下级节点
                tempNode = tempNode.getSubNode(c);
                if (tempNode == null) {
                    //没有这个字
                    res.append(c);
                    end = ++begin;
                    tempNode = rootNode;
                } else if (tempNode.isKeywordEnd()) {
                    //有且到头了 发现敏感词
                    res.append(REPLACEMENT);
                    begin = ++end;
                    tempNode = rootNode;
                } else {
                    //有但是没到头
                    end++;
                }
            } else {
                res.append(text.substring(begin));
            }
        }
        //把剩余的字符存入res
        return res.toString();
    }

    //判断是否为符号
    private boolean isSymbol(char c) {
        //判断是否为普通字符 abc等等   0x2E80---0x9FFF 东亚文字范围
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }

    //将敏感词添加到树中
    private void addKeyword(String keyword) {
        TrieNode tempNode = rootNode;
        for (int i = 0; i < keyword.length(); i++) {
            char c = keyword.charAt(i);
            TrieNode subNode = tempNode.getSubNode(c);
            if (subNode == null) {
                //初始化子节点
                subNode = new TrieNode();
                tempNode.addSubNode(c, subNode);
            }
            tempNode = subNode;
            if (i == keyword.length() - 1) {
                tempNode.setKeywordEnd(true);
            }
        }
    }

    //前缀树
    private class TrieNode {
        //关键词结束标识
        private boolean isKeywordEnd = false;

        //子节点
        private Map<Character, TrieNode> subNodes = new HashMap<>();

        public boolean isKeywordEnd() {
            return isKeywordEnd;
        }

        public void setKeywordEnd(boolean keywordEnd) {
            isKeywordEnd = keywordEnd;
        }

        //添加子节点
        public void addSubNode(Character c, TrieNode node) {
            subNodes.put(c, node);
        }

        //获取子节点
        public TrieNode getSubNode(Character c) {
            return subNodes.get(c);
        }
    }
}
