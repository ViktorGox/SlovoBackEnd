package aad.message.app.acess.token;

import aad.message.app.user.User;
import jakarta.persistence.*;

import java.util.Date;

@Entity
@Table(name = "access_token")
public class AccessToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false, unique = true)
    public String token;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    public User user;

    @Column(nullable = false)
    public Date expiryDate;

    public AccessToken() {
    }

    public AccessToken(String token, User user, Date expiryDate) {
        this.token = token;
        this.user = user;
        this.expiryDate = expiryDate;
    }
}
