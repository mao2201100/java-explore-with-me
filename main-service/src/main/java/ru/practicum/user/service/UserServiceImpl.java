package ru.practicum.user.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.user.mapper.UserMapper;
import ru.practicum.user.model.User;
import ru.practicum.user.model.dto.NewUserRequest;
import ru.practicum.user.model.dto.UserDto;
import ru.practicum.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final UserRepository userRepository;

    public UserServiceImpl(UserMapper userMapper, UserRepository userRepository) {
        this.userMapper = userMapper;
        this.userRepository = userRepository;
    }

    public List<UserDto> getAll(List<Long> ids, Integer from, Integer size) {
        List<User> users;
        validateSearchParameters(from, size);
        PageRequest pageRequest = PageRequest.of(from, size, Sort.by("id"));
        if (ids != null) {
            users = userRepository.findAllByIdIn(ids, pageRequest);
        } else {
            users = userRepository.findAll(pageRequest).toList();
        }
        return users.stream()
                .map(userMapper::toUserDto)
                .collect(Collectors.toList());
    }

    private void validateSearchParameters(int from, int size) {
        if (from < 0) {
            log.info("Параметр запроса 'from' должен быть больше или равен 0, указано значение {}", from);
            throw new ValidationException();
        } else if (size <= 0) {
            log.info("Параметр запроса 'size' должен быть больше 0, указано значение {}", size);
            throw new ValidationException();
        }
    }

    @Override
    @Transactional
    public UserDto createUser(NewUserRequest newUserRequest) {
        User user = userMapper.toUser(newUserRequest);
        validateToCreate(user);
        findUserByEmail(newUserRequest.getEmail());
        User savedUser = userRepository.save(user);
        log.info("Пользователь добавлен: {}", savedUser);
        return userMapper.toUserDto(user);
    }

    private void validateToCreate(User user) {
        String name = user.getName();
        String email = user.getEmail();
        if (name == null) {
            log.info("Не задано имя пользователя {}", user);
            throw new ValidationException();
        }
        if (name.isBlank()) {
            log.info("Имя пользователя не может состоять из пробелов {}", user);
            throw new ValidationException();
        }
        if (name.length() < 2) {
            log.info("Имя пользователя должно быть более двух символов {}", user);
            throw new ValidationException();
        }
        if (name.length() > 250) {
            log.info("Имя пользователя должно быть менее 256 символов {}", user);
            throw new ValidationException();
        }
        if (email == null) {
            log.info("Не задана электронная почта пользователя {}", user);
            throw new ValidationException();
        }
        if (!email.contains("@")) {
            log.info("Не правильно указана электронная почта отсутствует знак @ {}", user);
            throw new ValidationException();
        }
        if (email.length() < 6) {
            log.info("Электронная почта должна быть более шести символов {}", user);
            throw new ValidationException();
        }
        if (email.length() > 254) {
            log.info("Электронная почта должна быть менее 254 символов {}", user);
            throw new ValidationException();
        }
        String[] emailParts = email.split("@");
        if (emailParts.length == 2) {
            if (emailParts[0].length() > 64) {
                log.info("Длина имени до знака @ должна быть менее 64 символов");
                throw new ValidationException();
            } else if (emailParts[1].length() > 63 && email.length() != 254) {
                log.info("Длина домена электронной почты должна быть меньше 63 символов");
                throw new ValidationException();
            }
        }
    }

    private void findUserByEmail(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isPresent()) {
            log.info("Пользователь с таким email уже существует {}", email);
            throw new ConflictException();
        }
    }

    public void deleteUser(long userId) {
        User user = findUser(userId);
        userRepository.delete(user);
        log.info("Пользователь {} удален", user);
    }

    private User findUser(long userId) {
        if (userId == 0) {
            throw new ValidationException();
        }
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            throw new NotFoundException();
        }
        return user.get();
    }

}