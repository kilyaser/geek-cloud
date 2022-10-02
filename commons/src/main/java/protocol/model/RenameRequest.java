package protocol.model;

import lombok.Getter;

@Getter
public class RenameRequest implements CloudMessage{
    private final String oldFileName;
    private final String newFileName;


    public RenameRequest(String oldFileName, String newFileName) {
        this.oldFileName = oldFileName;
        this.newFileName = newFileName;
    }

    @Override
    public MessageType getType() {
        return MessageType.RENAME;
    }
}
