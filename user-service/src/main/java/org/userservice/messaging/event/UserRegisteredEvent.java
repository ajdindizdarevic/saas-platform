package org.userservice.messaging.event;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRegisteredEvent {
    private String userId;
    private String firstName;
    private String lastName;
    private String email;
    private String username;
    private String phoneNumber;
}