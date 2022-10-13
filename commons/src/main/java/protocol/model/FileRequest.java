package protocol.model;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileRequest implements CloudMessage {

    private String fileName;
    private int startPosition;
    @Override
    public MessageType getType() {
        return MessageType.FILE_REQUEST;
    }
}
