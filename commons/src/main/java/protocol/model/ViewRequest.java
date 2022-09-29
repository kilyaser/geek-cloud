package protocol.model;

import lombok.Getter;

@Getter
public class ViewRequest implements CloudMessage {
    private final String directory;
    public ViewRequest(String directory) {
        this.directory = directory;
    }

    @Override
    public MessageType getType() {
        return MessageType.VIEW;
    }
}
