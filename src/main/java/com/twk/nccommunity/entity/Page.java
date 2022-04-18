package com.twk.nccommunity.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 封装分页组件
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Page {
    //当前页数
    private int current = 1;
    //限制显示条数
    private int limit = 10;
    //总共有多少条
    private int rows;
    //查询路径
    private String path;

    public void setCurrent(int current) {
        if (current >= 1) {
            this.current = current;
        }
    }

    public void setLimit(int limit) {
        if (limit >= 1 && limit <= 100) {
            this.limit = limit;
        }
    }

    public void setRows(int rows) {
        if (rows >= 0) {
            this.rows = rows;
        }
    }

    //起始行
    public int getOffset() {
        return (current - 1) * limit;
    }

    //总页数
    public int getTotal() {
        if (rows % limit == 0) {
            return rows / limit;
        } else {
            return rows / limit + 1;
        }
    }

    public int getFrom() {
        int from = current - 2;
        return from < 1 ? 1 : from;
    }

    public int getTo() {
        int to = current + 2;
        int total = getTotal();
        return to > total ? total : to;
    }

}
