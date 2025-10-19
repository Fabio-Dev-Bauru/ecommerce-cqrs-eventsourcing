package com.ecommerce.order.command.controller;

import com.ecommerce.order.command.dto.OrderRequest;
import com.ecommerce.order.command.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    @Test
    @WithMockUser(roles = "USER")
    void shouldCreateOrderSuccessfully() throws Exception {
        // Arrange
        UUID orderId = UUID.randomUUID();
        when(orderService.createOrder(any(OrderRequest.class))).thenReturn(orderId);

        OrderRequest request = createValidOrderRequest();

        // Act & Assert
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Order created successfully"))
                .andExpect(jsonPath("$.data.orderId").value(orderId.toString()));
    }

    @Test
    void shouldReturn401WhenNotAuthenticated() throws Exception {
        // Arrange
        OrderRequest request = createValidOrderRequest();

        // Act & Assert
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturn400ForInvalidRequest() throws Exception {
        // Arrange - request sem customerId
        OrderRequest request = OrderRequest.builder()
                .items(List.of())
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    private OrderRequest createValidOrderRequest() {
        return OrderRequest.builder()
                .customerId("CUST-123")
                .items(List.of(
                        OrderRequest.OrderItemRequest.builder()
                                .productId("PROD-001")
                                .productName("Laptop")
                                .quantity(1)
                                .unitPrice(new BigDecimal("1500.00"))
                                .build()
                ))
                .build();
    }
}

