package blok2.database.services;

import blok2.database.dao.IUserDao;
import blok2.database.repositories.UserRepository;
import blok2.database.repositories.UserSettingsRepository;
import blok2.extensions.exceptions.NoSuchDatabaseObjectException;
import blok2.model.users.User;
import blok2.model.users.UserSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService implements IUserDao {

    private final UserRepository userRepository;
    private final UserSettingsRepository userSettingsRepository;
    private final PenaltyService penaltyService;

    @Autowired
    public UserService(UserRepository userRepository, UserSettingsRepository userSettingsRepository, PenaltyService penaltyService) {
        this.userRepository = userRepository;
        this.userSettingsRepository = userSettingsRepository;
        this.penaltyService = penaltyService;
    }

    @Override
    public User getUserByEmail(String email) {
        User user = userRepository.findByMail(email)
                .orElseThrow(() -> new NoSuchDatabaseObjectException(
                        String.format("No user found with email '%s'", email)));
        user.setPenaltyPoints(penaltyService.getUserPenalty(user.getUserId()));
        return user;
    }

    @Override
    public User getUserByEmailAndInstitution(String email, String institution) {
        User user = userRepository.findByMailAndInstitution(email, institution)
                .orElseThrow(() -> new NoSuchDatabaseObjectException(
                        String.format("No user found with email '%s' and institution '%s'", email, institution)));
        user.setPenaltyPoints(penaltyService.getUserPenalty(user.getUserId()));
        return user;
    }

    @Override
    public User getUserById(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchDatabaseObjectException(
                        String.format("No user found with userId '%s'", userId)));
        user.setPenaltyPoints(penaltyService.getUserPenalty(user.getUserId()));
        return user;
    }

    @Override
    public User getUserByIdAndInstitution(String userId, String institution) {
        User user = userRepository.findByUserIdAndInstitution(userId, institution)
                .orElseThrow(() -> new NoSuchDatabaseObjectException(
                        String.format("No user found with userId '%s' and institution '%s'", userId, institution)));
        user.setPenaltyPoints(penaltyService.getUserPenalty(user.getUserId()));
        return user;
    }

    @Override
    public List<User> getUsersByLastName(String lastName) {
        List<User> users = userRepository.findAllByLastNameIgnoreCase(lastName);
        users.forEach(user -> user.setPenaltyPoints(penaltyService.getUserPenalty(user.getUserId())));
        return users;
    }

    @Override
    public List<User> getUsersByLastNameAndInstitution(String lastName, String institution) {
        List<User> users = userRepository.findAllByLastNameAndInstitution(lastName, institution);
        users.forEach(user -> user.setPenaltyPoints(penaltyService.getUserPenalty(user.getUserId())));
        return users;
    }

    @Override
    public List<User> getUsersByFirstName(String firstName) {
        List<User> users = userRepository.findAllByFirstNameIgnoreCase(firstName);
        users.forEach(user -> user.setPenaltyPoints(penaltyService.getUserPenalty(user.getUserId())));
        return users;
    }

    @Override
    public List<User> getUsersByFirstNameAndInstitution(String firstName, String institution) {
        List<User> users = userRepository.findAllByFirstNameIgnoreCaseAndInstitution(firstName, institution);
        users.forEach(user -> user.setPenaltyPoints(penaltyService.getUserPenalty(user.getUserId())));
        return users;
    }

    @Override
    public List<User> getUsersByFirstAndLastName(String firstName, String lastName) {
        List<User> users = userRepository.findAllByFirstNameIgnoreCaseAndLastNameIgnoreCase(firstName, lastName);
        users.forEach(user -> user.setPenaltyPoints(penaltyService.getUserPenalty(user.getUserId())));
        return users;
    }

    @Override
    public List<User> getUsersByFirstAndLastNameAndInstitution(String firstName, String lastName, String institution) {
        List<User> users = userRepository.findAllByFirstNameIgnoreCaseAndLastNameIgnoreCaseAndInstitution(firstName, lastName, institution);
        users.forEach(user -> user.setPenaltyPoints(penaltyService.getUserPenalty(user.getUserId())));
        return users;
    }

    @Override
    public List<User> getAdmins() {
        List<User> users = userRepository.findAllByAdminTrue();
        users.forEach(user -> user.setPenaltyPoints(penaltyService.getUserPenalty(user.getUserId())));
        return users;
    }

    @Override
    public User getUserFromBarcode(String barcode) {
        // Case 1: the student number and barcode match exactly.
        // For example, when scanning the barcode page, the student number is encoded as Code 128.
        Optional<User> optionalUser = userRepository.findById(barcode);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setPenaltyPoints(penaltyService.getUserPenalty(user.getUserId()));
            return user;
        }

        // Case 2: the barcode is a UPC-A encoded student number.
        // Example student number: 000140462060
        // Example barcode:        001404620603
        String alternative = "0" + barcode.substring(0, barcode.length() - 1);
        optionalUser = userRepository.findById(alternative);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setPenaltyPoints(penaltyService.getUserPenalty(user.getUserId()));
            return user;
        }

        // Case 3: the barcode is EAN13.
        // Example student number: 114637753611
        // Example barcode:        1146377536113
        alternative = barcode.substring(0, barcode.length() - 1);
        optionalUser = userRepository.findById(alternative);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setPenaltyPoints(penaltyService.getUserPenalty(user.getUserId()));
            return user;
        }

        // Case 4: last known option is that all but the first character match the student number
        // Example student number: 000140462060
        // Example barcode:        0000140462060
        alternative = barcode.substring(1);
        optionalUser = userRepository.findById(alternative);
        optionalUser.ifPresent(user -> user.setPenaltyPoints(penaltyService.getUserPenalty(user.getUserId())));

        return optionalUser.orElseThrow(
                () -> new NoSuchDatabaseObjectException(
                        String.format("No user found with barcode '%s'.", barcode)));
    }

    @Override
    public User getUserFromBarcodeAndInstitution(String barcode, String institution) {
        User user = getUserFromBarcode(barcode);
        if (!user.getInstitution().equals(institution)) {
            throw new NoSuchDatabaseObjectException(
                    String.format("No user found with barcode '%s' and institution '%s'.", barcode, institution));
        }
        return user;
    }

    @Override
    public User addUser(User user) {
        return userRepository.saveAndFlush(user);
    }

    @Override
    public void updateUser(User user) {
        userRepository.save(user);
    }

    @Override
    public void updateUserSettings(UserSettings settings) {
        userSettingsRepository.save(settings);
    }

    @Override
    public void deleteUser(String userId) {
        userRepository.deleteById(userId);
    }

    @Override
    public boolean accountExistsByEmail(String email) {
        return userRepository.existsByMail(email);
    }

}
