package aad.message.app.acess.token;

import aad.message.app.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccessTokenRepository extends JpaRepository<AccessToken, Long> {
    Optional<AccessToken> findByUser(User user);

    Optional<AccessToken> findByUserId(Long userId);

    void deleteAccessTokenByUser_Id(Long userId);
}
