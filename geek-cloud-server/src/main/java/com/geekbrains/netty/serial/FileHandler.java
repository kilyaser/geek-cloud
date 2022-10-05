package com.geekbrains.netty.serial;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import lombok.extern.slf4j.Slf4j;
import protocol.AssistanceAlertUtils;
import protocol.model.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

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
                Files.write(serverDir.resolve(fileMessage.getFileName()), fileMessage.getBytes());
                ctx.writeAndFlush(new ListMessage(serverDir));
            }
            case FILE_REQUEST -> {
                FileRequest fileRequest = (FileRequest) cloudMessage;
                ctx.writeAndFlush(new FileMessage(serverDir.resolve(fileRequest.getFileName())));
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
//                Alert infoAlert = AssistanceAlertUtils.getInformationAlert(deleteRequest.getFilename(), true);
//                Alert alert = AssistanceAlertUtils.getWarningConfirm(deleteRequest.getFilename());
//                Optional<ButtonType> answer = alert.showAndWait();
//                if (answer.get() == ButtonType.OK) {
                    if (Files.exists(path)) {
                        Files.delete(path);
                        ctx.writeAndFlush(new ListMessage(serverDir));
//                        infoAlert.showAndWait();
                    } else {
                        log.debug("Received file {} didn't exist", deleteRequest.getFilename());
//                        infoAlert.setContentText("You should choose file");
//                        infoAlert.showAndWait();
                    }
//                } else {
//                    infoAlert = AssistanceAlertUtils.getInformationAlert(deleteRequest.getFilename(), false);
//                    infoAlert.showAndWait();
//                }
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
        }
    }
}
