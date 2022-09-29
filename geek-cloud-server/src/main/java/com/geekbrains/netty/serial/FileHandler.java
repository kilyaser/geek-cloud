package com.geekbrains.netty.serial;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import protocol.model.*;

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
        }
    }
}
