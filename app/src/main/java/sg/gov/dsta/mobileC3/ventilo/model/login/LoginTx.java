package sg.gov.dsta.mobileC3.ventilo.model.login;

import lombok.Data;

@Data
public class LoginTx {

    private String email;
    private String password;

    public LoginTx(String username, String password) {
        this.email = username;
        this.password = password;
    }

}
