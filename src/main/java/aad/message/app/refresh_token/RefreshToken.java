package aad.message.app.refresh_token;

import aad.message.app.user.User;
import jakarta.persistence.*;
import java.util.Date;

@Entity
public class RefreshToken {

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

    public RefreshToken() {}

    public RefreshToken(String token, User user, Date expiryDate) {
        this.token = token;
        this.user = user;
        this.expiryDate = expiryDate;
    }

    public boolean isExpired() {
        return new Date().after(expiryDate);
    }
}
