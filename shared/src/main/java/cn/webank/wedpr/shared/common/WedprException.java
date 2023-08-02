package cn.webank.wedpr.shared.common;

@SuppressWarnings("serial")
public class WedprException extends Exception {
    /* 错误码类 */
    private final WedprStatusEnum status;

    public WedprException(WedprStatusEnum status) {
        super(status.getMessage());
        this.status = status;
    }

    public WedprStatusEnum getStatus() {
        return status;
    }
}
