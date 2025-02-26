package com.orainge.tools.network.dns.exception;

/**
 * 未找到 DNS 记录
 */
public class NoDnsRecordException extends RuntimeException {
    public NoDnsRecordException() {
        super("无法查询到记录");
    }

    public NoDnsRecordException(String message) {
        super(message);
    }

    public NoDnsRecordException(Throwable t) {
        super("无法查询到记录", t);
    }

    public NoDnsRecordException(String message, Throwable t) {
        super(message, t);
    }
}
