package com.urlshortener.subscription.repository;

import com.urlshortener.subscription.model.Subscription;
import com.urlshortener.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    Optional<Subscription> findByUser(User user);
    Optional<Subscription> findByUserId(Long userId);
}
