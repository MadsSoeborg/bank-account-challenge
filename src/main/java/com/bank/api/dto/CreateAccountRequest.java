package com.bank.api.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateAccountRequest(
        @NotBlank(message = "First name must not be blank")
        String firstName,

        @NotBlank(message = "Last name must not be blank")
        String lastName
) {}