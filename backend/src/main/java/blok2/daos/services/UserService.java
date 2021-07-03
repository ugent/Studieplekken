package blok2.daos.services;

import blok2.daos.IUserDao;
import blok2.daos.repositories.UserRepository;
import blok2.helpers.exceptions.NoSuchDatabaseObjectException;
import blok2.model.users.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService implements IUserDao {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByMail(email)
                .orElseThrow(() -> new NoSuchDatabaseObjectException(
                        String.format("No user found with email '%s'", email)));
    }

    @Override
    public User getUserById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchDatabaseObjectException(
                        String.format("No user found with userId '%s'", userId)));
    }

    @Override
    public List<User> getUsersByLastName(String lastName) {
        return userRepository.findAllByLastName(lastName);
    }

    @Override
    public List<User> getUsersByFirstName(String firstName) {
        return userRepository.findAllByFirstName(firstName);
    }

    @Override
    public List<User> getUsersByFirstAndLastName(String firstName, String lastName) {
        return userRepository.findAllByFirstNameAndLastName(firstName, lastName);
    }

    @Override
    public List<User> getAdmins() {
        return userRepository.findAllByAdminTrue();
    }

    @Override
    public User getUserFromBarcode(String barcode) {
        // Case 1: the student number and barcode match exactly.
        // For example, when scanning the barcode page, the student number is encoded as Code 128.
        Optional<User> optionalUser = userRepository.findById(barcode);
        if (optionalUser.isPresent())
            return optionalUser.get();

        // Case 2: the barcode is a UPC-A encoded student number.
        // Example student number: 000140462060
        // Example barcode:        001404620603
        String alternative = "0" + barcode.substring(0, barcode.length() - 1);
        optionalUser = userRepository.findById(alternative);
        if (optionalUser.isPresent())
            return optionalUser.get();

        // Case 3: the barcode is EAN13.
        // Example student number: 114637753611
        // Example barcode:        1146377536113
        alternative = barcode.substring(0, barcode.length() - 1);
        optionalUser = userRepository.findById(alternative);
        if (optionalUser.isPresent())
            return optionalUser.get();

        // Case 4: last known option is that all but the first character match the student number
        // Example student number: 000140462060
        // Example barcode:        0000140462060
        alternative = barcode.substring(1);
        optionalUser = userRepository.findById(alternative);

        return optionalUser.orElseThrow(
                () -> new NoSuchDatabaseObjectException(
                        String.format("No user found with barcode '%s'.", barcode)));
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
    public void deleteUser(String userId) {
        userRepository.deleteById(userId);
    }

    @Override
    public boolean accountExistsByEmail(String email) {
        return userRepository.existsByMail(email);
    }

}
