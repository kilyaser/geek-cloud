package com.geekbrains.netty.serial;

import com.geekbrains.netty.connectionDAO.DAOUtils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import protocol.model.*;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;


import static protocol.Constants.DELIMITER;


@Slf4j
public class FileHandler extends SimpleChannelInboundHandler<CloudMessage> {
    private Path serverDir;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        serverDir = Path.of("server_files");
        ctx.writeAndFlush(new ListMessage(serverDir));
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, CloudMessage cloudMessage) throws Exception {
        log.debug("received: {}", cloudMessage.getType());
        switch (cloudMessage.getType()) {
            case FILE -> {
                FileMessage fileMessage = (FileMessage) cloudMessage;
                getFileFromClient(ctx, fileMessage);
//                Files.write(serverDir.resolve(fileMessage.getFileName()), fileMessage.getBytes());
                ctx.writeAndFlush(new ListMessage(serverDir));
            }
            case FILE_REQUEST -> {
                FileRequest fileRequest = (FileRequest) cloudMessage;
                sendFileByRequest(fileRequest, ctx);
            }
            case VIEW -> {
                ViewRequest viewRequest = (ViewRequest) cloudMessage;
                Path path = Path.of(serverDir + DELIMITER + viewRequest.getDirectory());
                log.debug("Received directory {}", path);
                if (Files.isDirectory(path)) {
                    serverDir = path;
                    ctx.writeAndFlush(new ListMessage(serverDir));
                }
            }
            case DELETE -> {
                DeleteRequest deleteRequest = (DeleteRequest) cloudMessage;
                Path path = Path.of(serverDir + DELIMITER + deleteRequest.getFilename());
                log.debug("Received file for delete {}", path);

                if (deleteRequest.isConfirm()) {
                    if (Files.exists(path)) {
                        Files.delete(path);
                        ctx.writeAndFlush(new ListMessage(serverDir));
                        ctx.writeAndFlush(new AlertMessage(deleteRequest.getFilename(), true));
                    }
                } else {
                    ctx.writeAndFlush(new AlertMessage(deleteRequest.getFilename(), false));
                }
            }
            case RENAME -> {
                RenameRequest renameRequest = (RenameRequest) cloudMessage;
                File file = new File(serverDir + DELIMITER + renameRequest.getOldFileName());
                File newFileName = new File(serverDir + DELIMITER + renameRequest.getNewFileName());
                if (file.renameTo(newFileName)) {
                    ctx.writeAndFlush(new ListMessage(serverDir));
                    log.debug(file.getName() + "was rename to " + newFileName.getName());
                } else {
                    log.debug("rename file was failed");
                }
            }
            case AUTH -> {
                AuthRequest authRequest = (AuthRequest) cloudMessage;
                boolean checkUser = DAOUtils.logInUser(authRequest.getUsername(), authRequest.getPassword());
                if (checkUser) {
                    ctx.writeAndFlush(new AuthRequest(authRequest.getUsername(), authRequest.getPassword(), checkUser));
                } else {
                    ctx.writeAndFlush(new AuthRequest(authRequest.getUsername(), authRequest.getPassword(), checkUser));
                }
            }
            case SIGN -> {
                SignUpRequest signUpRequest = (SignUpRequest) cloudMessage;
                boolean resultSignUp = DAOUtils.connect(signUpRequest.getUsername(), signUpRequest.getPassword());
                if (resultSignUp) {
                    ctx.writeAndFlush(new SignUpRequest(signUpRequest.getUsername(), signUpRequest.getPassword(), resultSignUp));
                } else {
                    ctx.writeAndFlush(new SignUpRequest(signUpRequest.getUsername(), signUpRequest.getPassword(), resultSignUp));
                }
            }
        }
    }

    private void getFileFromClient(ChannelHandlerContext ctx, FileMessage fileMessage) {
        log.debug("FileMessage object receiving form client [{}]", fileMessage.getFileName());
        File file = new File(serverDir + DELIMITER + fileMessage.getFileName());
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw")){
            randomAccessFile.seek(fileMessage.getStartPos());
            randomAccessFile.write(fileMessage.getBody());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendFileByRequest(FileRequest fileRequest, ChannelHandlerContext ctx) {
        FileMessage fileMessage = new FileMessage();
        int startPos = 0;
        int byteRead;
        int bodyLength = 1024;
        byte[] body = new byte[bodyLength];
        long fileSize;
        int packages;
        try {
            RandomAccessFile randomAccessFile = new RandomAccessFile(serverDir + DELIMITER + fileRequest.getFileName(), "r");
            fileSize = randomAccessFile.length();
            log.debug("file size: " + fileSize);
            randomAccessFile.seek(fileRequest.getStartPosition());

            if ((randomAccessFile.length() % bodyLength) != 0) {
                packages = (int) (randomAccessFile.length() / bodyLength + 1);
            } else {
                packages = (int) (randomAccessFile.length() / bodyLength);
            }
            log.debug(packages + " packages will be sent to the client");

            while ((byteRead = randomAccessFile.read(body)) != -1) {
                log.debug(byteRead + " was read");
                fileMessage.setFileName(fileRequest.getFileName());
                fileMessage.setStartPos(startPos);
                fileMessage.setBody(body);
                ctx.writeAndFlush(fileMessage);
                startPos += byteRead;
                randomAccessFile.seek(startPos);
                packages--;

                log.debug("Document length: [{}], remaining length: [{}], send length: [{}]",
                        randomAccessFile.length(),
                        randomAccessFile.length() - byteRead,
                        byteRead);
                log.debug("number of remaining packets: [{}]",
                        packages);
            }



        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
