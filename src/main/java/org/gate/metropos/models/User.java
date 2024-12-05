package org.gate.metropos.models;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.gate.metropos.enums.UserRole;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@SuperBuilder
public class User {
    protected Long id;
    protected String username;
    protected String email;
    protected String password;
    protected UserRole role;
    protected LocalDateTime createdAt;
    protected LocalDateTime updatedAt;
}