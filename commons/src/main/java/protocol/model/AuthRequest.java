package protocol.model;

import lombok.Getter;

@Getter
public class AuthRequest implements CloudMessage{
    private final String username;
    private final String password;
    private final boolean checkUser;

    public AuthRequest(String username, String password) {
        this.username = username;
        this.password = password;
        this.checkUser = false;
    }
    public AuthRequest(String username, String password, boolean checkUser) {
        this.username = username;
        this.password = password;
        this.checkUser = checkUser;
    }

    @Override
    public MessageType getType() {
        return MessageType.AUTH;
    }
}
