package protocol.model;

import lombok.Getter;

@Getter
public class SignUpRequest implements CloudMessage{

    private final String username;
    private final String password;

    public SignUpRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }


    @Override
    public MessageType getType() {
        return MessageType.SIGN;
    }
}
