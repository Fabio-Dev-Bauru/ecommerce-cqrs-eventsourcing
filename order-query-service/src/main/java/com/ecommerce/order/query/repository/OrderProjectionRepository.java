package com.ecommerce.order.query.repository;

import com.ecommerce.order.query.projection.OrderProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface OrderProjectionRepository extends JpaRepository<OrderProjection, UUID> {

    Page<OrderProjection> findByCustomerId(String customerId, Pageable pageable);

    Page<OrderProjection> findByStatus(String status, Pageable pageable);

    Page<OrderProjection> findByCustomerIdAndStatus(String customerId, String status, Pageable pageable);

    @Query("SELECT o FROM OrderProjection o WHERE o.createdAt BETWEEN :startDate AND :endDate ORDER BY o.createdAt DESC")
    List<OrderProjection> findOrdersByDateRange(@Param("startDate") Instant startDate, 
                                                @Param("endDate") Instant endDate);

    @Query("SELECT COUNT(o) FROM OrderProjection o WHERE o.customerId = :customerId")
    Long countByCustomerId(@Param("customerId") String customerId);

    @Query("SELECT SUM(o.totalAmount) FROM OrderProjection o WHERE o.customerId = :customerId AND o.status = 'CONFIRMED'")
    BigDecimal getTotalSpentByCustomer(@Param("customerId") String customerId);
}

