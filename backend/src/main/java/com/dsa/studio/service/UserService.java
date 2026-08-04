package com.dsa.studio.service;

import com.dsa.studio.dto.request.UpdateProfileRequest;
import com.dsa.studio.dto.response.UserProfileResponse;
import com.dsa.studio.model.User;
import com.dsa.studio.repository.UserRepository;
import com.dsa.studio.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserProfileResponse getProfile(UserDetailsImpl currentUser) {
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return mapToProfileResponse(user, currentUser);
    }

    @Transactional
    public UserProfileResponse updateProfile(UserDetailsImpl currentUser, UpdateProfileRequest request) {
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (request.getAvatar() != null) user.setAvatar(request.getAvatar());
        if (request.getBio() != null) user.setBio(request.getBio());

        userRepository.save(user);
        return mapToProfileResponse(user, currentUser);
    }

    private UserProfileResponse mapToProfileResponse(User user, UserDetailsImpl currentUser) {
        UserProfileResponse response = new UserProfileResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setAvatar(user.getAvatar());
        response.setBio(user.getBio());
        List<String> roles = currentUser.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .collect(Collectors.toList());
        response.setRoles(roles);
        return response;
    }
}
