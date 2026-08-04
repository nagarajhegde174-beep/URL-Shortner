package com.dsa.studio.dto.response;

import lombok.Data;
import java.util.List;

@Data
public class UserProfileResponse {
    private Long id;
    private String username;
    private String email;
    private String avatar;
    private String bio;
    private List<String> roles;
}
