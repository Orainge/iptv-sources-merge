package com.orainge.tools.network.dns.exception;

/**
 * DNS 查询异常错误
 */
public class DnsQueryException extends RuntimeException {
    public DnsQueryException() {
        super("查询错误");
    }

    public DnsQueryException(String message) {
        super(message);
    }

    public DnsQueryException(Throwable t) {
        super("查询错误", t);
    }

    public DnsQueryException(String message, Throwable t) {
        super(message, t);
    }
}
