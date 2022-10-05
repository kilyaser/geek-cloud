package protocol.model;

import javafx.scene.control.Alert;
import lombok.Getter;

@Getter
public class AlertMessage implements CloudMessage{
    private final String filename;
    private final boolean result;

    public AlertMessage(String filename, boolean result) {
        this.filename = filename;
        this.result = result;
    }

    @Override
    public MessageType getType() {
        return MessageType.AlERT;
    }
}
