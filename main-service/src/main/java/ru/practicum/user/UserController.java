package ru.practicum.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.user.model.dto.NewUserRequest;
import ru.practicum.user.model.dto.UserDto;
import ru.practicum.user.service.UserService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers(@RequestParam(required = false) List<Long> ids,
                                                @RequestParam(defaultValue = "0") Integer from,
                                                @RequestParam(defaultValue = "10") Integer size) {
        log.info("Получен GET запрос: /admin/users ");
        return ResponseEntity.ok().body(userService.getAll(ids, from, size));
    }

    @PostMapping
    public ResponseEntity<UserDto> createUser(@RequestBody NewUserRequest newUserRequest) {
        log.info("Получен POST запрос: /admin/users endpoint с телом {}", newUserRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(newUserRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<UserDto> deleteUser(@PathVariable("id") Long userId) {
        log.info("Получен DELETE запрос: /admin/users/{} ", userId);
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

}