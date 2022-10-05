package protocol.model;

import lombok.Getter;

@Getter
public class DeleteRequest implements CloudMessage{

    private final String filename;
    private final boolean confirm;

    public DeleteRequest(String filename, boolean confirm) {
        this.filename = filename;
        this.confirm = confirm;
    }

    @Override
    public MessageType getType() {
        return MessageType.DELETE;
    }
}
