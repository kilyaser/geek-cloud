package protocol.model;

import lombok.Getter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;

@Getter
public class FileMessage implements CloudMessage {

    private final String fileName;
    private final long size;
//    private final File file;
    private final byte[] bytes;

    public FileMessage(Path file) throws IOException {
//       this.file = Files.createFile(file).toFile();
//       try (FileChannel in = new FileInputStream(file.toFile()).getChannel();
//            FileChannel out = new FileOutputStream(this.file).getChannel()) {
//
//           ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
//           while (in.read(buffer) != -1) {
//               buffer.flip();
//               out.write(buffer);
//               buffer.clear();
//           }
//       } catch (IOException e) {
//           e.printStackTrace();
//       }
        fileName = file.getFileName().toString();
        bytes = Files.readAllBytes(file);
        size = bytes.length;

    }

    @Override
    public MessageType getType() {
        return MessageType.FILE;
    }
}
