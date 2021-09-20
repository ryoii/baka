package cn.ryoii.baka.exception;

import java.io.IOException;

public class ReadHintFailedException extends IOException {

    public ReadHintFailedException(int fileId, long position) {
        super(String.format("[%d] cannot read hint at 0x%X", fileId, position));
    }
}
