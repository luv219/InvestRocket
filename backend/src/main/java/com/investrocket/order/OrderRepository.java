package com.investrocket.order;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.time.Instant;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.investrocket.user.User;

import jakarta.persistence.LockModeType;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    List<Order> findByUserOrderByCreatedAtDesc(User user);

    List<Order> findByUserAndStatusOrderByCreatedAtDesc(User user, OrderStatus status);

    long countByUserAndCreatedAtBetween(User user, Instant start, Instant end);

    long countByUser(User user);

    long countByStatus(OrderStatus status);

    long countBySide(OrderSide side);

    long countByOrderType(OrderType orderType);

    List<Order> findTop20ByOrderByCreatedAtDesc();

    @Query("select o.id from Order o where o.status = :status")
    List<UUID> findIdsByStatus(@Param("status") OrderStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from Order o where o.id = :id")
    Optional<Order> findForUpdateById(@Param("id") UUID id);
}
