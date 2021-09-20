package cn.ryoii.baka.config;

public class BakaConfig {

    private long maxFileSize = 50 * 1024 * 1024;

    public long getMaxFileSize() {
        return maxFileSize;
    }

    public void setMaxFileSize(long maxFileSize) {
        this.maxFileSize = maxFileSize;
    }
}
