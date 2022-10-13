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

//    private File file;
    private String fileName;
    private int startPos;
//    private final long size;
    private byte[] body;
//    private int endPos;

//    public FileMessage(File file, String fileName, int startPos, byte[] body, int endPos) {
//        this.file = file;
//        this.fileName = fileName;
//        this.startPos = startPos;
//        this.body = body;
//        this.endPos = endPos;
//    }
//        public FileMessage(Path file) throws IOException {
//        this.fileName = file.getFileName().toString();
//        this.body = Files.readAllBytes(file);
//        this.file = new File(fileName);
//
//    }

    @Override
    public MessageType getType() {
        return MessageType.FILE;
    }
}
