package protocol;

import java.io.*;

import static protocol.Constants.*;

public class FileUtils {

    public static void readFileFromStream(DataInputStream dis, String dstDirectory) throws IOException {
        byte[] batch = new byte[BATCH_SIZE];
        String fileName = dis.readUTF();
        long size = dis.readLong();
        System.out.printf("File name: %s, file size: %d", fileName, size);
        try (FileOutputStream fos = new FileOutputStream(dstDirectory + DELIMITER + fileName)) {
            for (int i = 0; i < (size + BATCH_SIZE - 1) / BATCH_SIZE; i++) {
                int read = dis.read(batch);
                fos.write(batch, 0, read);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void writeFileToStream(DataInputStream dis, DataOutputStream dos, String dstDirectory) throws IOException {
         String fileName = dis.readUTF();
         String filePath = dstDirectory + DELIMITER + fileName;
         File file = new File(filePath);
         if (file.isFile()) {
            dos.writeUTF(SEND_FILE_COMMAND);
            dos.writeUTF(fileName);
            dos.writeLong(file.length());
            try (FileInputStream fis = new FileInputStream(file)) {
               byte[] bytes = fis.readAllBytes();
               dos.write(bytes);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
         }
    }
    public static void writeFileToStream(DataOutputStream dos,String fileName,  String filePath) {
        try {
            File file = new File(filePath);
            if (file.isFile()) {
                dos.writeUTF(SEND_FILE_COMMAND);
                dos.writeUTF(fileName);
                dos.writeLong(file.length());
                try (FileInputStream fis = new FileInputStream(file)) {
                    byte[] bytes = fis.readAllBytes();
                    dos.write(bytes);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
