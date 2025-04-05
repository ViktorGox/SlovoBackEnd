package aad.message.app.refresh.token;

import aad.message.app.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByUser(User user);

    Optional<RefreshToken> findByUserId(Long userId);

    void deleteRefreshTokenByUser_Id(Long userId);
}
