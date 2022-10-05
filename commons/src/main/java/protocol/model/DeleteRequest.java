package protocol.model;

import lombok.Getter;

@Getter
public class DeleteRequest implements CloudMessage{

    private final String filename;

    public DeleteRequest(String filename) {
        this.filename = filename;
    }

    @Override
    public MessageType getType() {
        return MessageType.DELETE;
    }
}
