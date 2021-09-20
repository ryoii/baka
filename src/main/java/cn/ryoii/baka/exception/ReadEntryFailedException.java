package cn.ryoii.baka.exception;

import java.io.IOException;

public class ReadEntryFailedException extends IOException {

    public ReadEntryFailedException(int fileId, long position) {
        super(String.format("[%d] cannot read data at 0x%X", fileId, position));
    }
}
