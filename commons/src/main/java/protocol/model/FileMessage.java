package protocol.model;

import lombok.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileMessage implements CloudMessage {

    private String fileName;
    private int startPos;
    private byte[] body;

    @Override
    public MessageType getType() {
        return MessageType.FILE;
    }
}
