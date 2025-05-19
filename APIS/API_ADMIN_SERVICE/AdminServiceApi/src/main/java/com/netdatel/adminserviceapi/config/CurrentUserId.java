package com.netdatel.adminserviceapi.config;

import java.lang.annotation.*;

/**
 * Anotación para inyectar el ID del usuario actual en los métodos del controlador.
 *
 * Ejemplo de uso:
 * <pre>
 * @GetMapping("/profile")
 * public ResponseEntity<UserProfile> getProfile(@CurrentUserId Long userId) {
 *     return ResponseEntity.ok(userService.getProfileById(userId));
 * }
 * </pre>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CurrentUserId {
}