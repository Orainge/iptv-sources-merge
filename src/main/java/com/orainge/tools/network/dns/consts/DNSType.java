package com.orainge.tools.network.dns.consts;

/**
 * DNS 类型<br>
 * 可自行增减<br>
 * https://www.iana.org/assignments/dns-parameters/dns-parameters.xhtml#dns-parameters-4
 */
public enum DNSType {
    ORIGINAL("ORIGINAL", "-1", "原始值"),
    A("A", "1", "a host address"),
    AAAA("AAAA", "28", "IP6 Address");

    private final String type;
    private final String value;
    private final String description;

    DNSType(String type, String value, String description) {
        this.type = type;
        this.value = value;
        this.description = description;
    }

    public static DNSType getByType(String type) {
        for (DNSType dnsType : DNSType.values()) {
            if (dnsType.type.equals(type)) {
                return dnsType;
            }
        }
        return null;
    }

    public String getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }
}
