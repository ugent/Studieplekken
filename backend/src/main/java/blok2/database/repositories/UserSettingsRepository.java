package blok2.database.repositories;

import blok2.model.users.UserSettings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSettingsRepository extends JpaRepository<UserSettings, String> {
}
