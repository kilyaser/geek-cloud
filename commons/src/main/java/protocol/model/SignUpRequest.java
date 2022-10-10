package protocol.model;

import lombok.Getter;

@Getter
public class SignUpRequest implements CloudMessage{

    private final String username;
    private final String password;
    private final boolean resultSignUp;

    public SignUpRequest(String username, String password) {
        this.username = username;
        this.password = password;
        this.resultSignUp = false;
    }

    public SignUpRequest(String username, String password, boolean resultSignUp) {
        this.username = username;
        this.password = password;
        this.resultSignUp = resultSignUp;
    }

    @Override
    public MessageType getType() {
        return MessageType.SIGN;
    }
}
