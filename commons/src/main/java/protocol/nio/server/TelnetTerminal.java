package protocol.nio.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;


import static protocol.Constants.*;
public class TelnetTerminal {
    private Path current;
    private ServerSocketChannel server;
    private Selector selector;
    private ByteBuffer buf;

    public TelnetTerminal() throws IOException {
        current = Path.of("common");
        buf = ByteBuffer.allocate(256);
        server = ServerSocketChannel.open();
        selector = Selector.open();
        server.bind(new InetSocketAddress(8189));
        server.configureBlocking(false);
        server.register(selector, SelectionKey.OP_ACCEPT);
        while (server.isOpen()) {
            selector.select();
            Set<SelectionKey> keys = selector.selectedKeys();
            Iterator<SelectionKey> keyIterator = keys.iterator();
            while (keyIterator.hasNext()) {
                SelectionKey key = keyIterator.next();
                if (key.isAcceptable()) {
                    handleAccept();
                }
                if (key.isReadable()) {
                    handleRead(key);
                }
                keyIterator.remove();
            }
        }
    }

    public static void main(String[] args) {
        try {
            new TelnetTerminal();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleRead(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        buf.clear();
        StringBuilder sb = new StringBuilder();
        while (true) {
            int read = channel.read(buf);
            if (read == 0) {
                break;
            }
            if (read == -1) {
                channel.close();
                return;
            }
            buf.flip();
            while (buf.hasRemaining()) {
                sb.append((char) buf.get());
            }
            buf.clear();
        }
        System.out.println("Received: " + sb);
        String[] command = sb.toString().trim().split("\s");
        if (command.length <= 2) {
            switch (command[0]) {
                case GET_LIST_FILES -> getListFiles(channel);
                case CD_DIR -> changeDirectory(channel, command[1]);
                case TOUCH_FILE -> creatFile(channel, command[1]);
                case MAKE_DIR -> makeDirectory(channel, command[1]);
                case CAT_FILENAME -> showFileNameBytes(channel, command[1]);
                default -> returnMessage(channel, command);
            }
        } else {
            String[] answer = {"incorrect command:"};
            returnMessage(channel, answer);
            returnMessage(channel, command);
        }
    }

    private void getListFiles(SocketChannel channel) throws IOException {
        String files = Files.list(current)
                .map(p -> p.getFileName().toString())
                .collect(Collectors.joining("\n\r"));
        channel.write(ByteBuffer.wrap(files.getBytes(StandardCharsets.UTF_8)));
    }

    private void changeDirectory(SocketChannel channel, String path) throws IOException {
        Path dirPath = current.resolve(path);
        if (Files.exists(dirPath)) {
            if (Files.isDirectory(dirPath)) {
                current = dirPath;
                channel.write(ByteBuffer.wrap(current.toString().getBytes(StandardCharsets.UTF_8)));
            } else {
                String answer = "the directory is specified incorrectly";
                channel.write(ByteBuffer.wrap(answer.getBytes(StandardCharsets.UTF_8)));
            }
            String answer = "the directory is specified incorrectly";
            channel.write(ByteBuffer.wrap(answer.getBytes(StandardCharsets.UTF_8)));
        }
    }
    private void creatFile(SocketChannel channel, String fileName) throws IOException {
        Path dir = Paths.get(String.valueOf(current), fileName);
        if (!Files.exists(dir)) {
            Files.createFile(dir);
            String answer = fileName + " created";
            channel.write(ByteBuffer.wrap(answer.getBytes(StandardCharsets.UTF_8)));
        } else {
            String answer = fileName + "is already exist";
            channel.write(ByteBuffer.wrap(answer.getBytes(StandardCharsets.UTF_8)));
        }
    }
    private void makeDirectory(SocketChannel channel, String newDir) throws IOException {
        Path path = Paths.get(String.valueOf(current), newDir);
        if (!Files.exists(path)) {
            Files.createDirectories(path);
            String[] answer = {"Directory " + newDir + "exists"};
            returnMessage(channel, answer);
        } else {
            String[] answer = {"Directory is already exists"};
            returnMessage(channel, answer);
        }
    }

    private void returnMessage(SocketChannel channel, String[] command) throws IOException {
        StringBuilder sb = new StringBuilder();
        Stream<String> stream = Arrays.stream(command);
        stream.forEach(sb::append);
        channel.write(ByteBuffer.wrap(sb.toString().getBytes()));
    }

    private void showFileNameBytes(SocketChannel channel, String fileName) throws IOException {
        Path file = Paths.get(String.valueOf(current), fileName);
        byte[] bytes;
        if (Files.exists(file)) {
            bytes = Files.readAllBytes(file);
            channel.write(ByteBuffer.wrap(bytes));
        } else {
            String answer = fileName + "doesn't exist";
            channel.write(ByteBuffer.wrap(answer.getBytes(StandardCharsets.UTF_8)));
        }
    }

    private void handleAccept() throws IOException {
        SocketChannel socketChannel = server.accept();
        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_READ);
        System.out.println("Client accepted");
    }


}