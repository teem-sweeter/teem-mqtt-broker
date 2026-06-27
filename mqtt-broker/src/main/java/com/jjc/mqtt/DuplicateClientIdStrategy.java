package com.jjc.mqtt;

/**
 * 重复 Client ID 连接策略
 *
 * @author sweeter
 */
public enum DuplicateClientIdStrategy {

    /**
     * 拒绝新连接（默认策略）
     * 当已存在相同 Client ID 的活跃连接时，拒绝新客户端的连接请求
     */
    REJECT_NEW,

    /**
     * 断开旧连接
     * 当已存在相同 Client ID 的活跃连接时，断开旧连接，接受新连接
     */
    DISCONNECT_OLD
}
