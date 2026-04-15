package com.minhthien.web.coach.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletBankAccountUpsertRequest {

    @NotBlank(message = "Bank code is required")
    @Size(max = 50, message = "Bank code must not exceed 50 characters")
    private String bankCode;

    @NotBlank(message = "Bank name is required")
    @Size(max = 150, message = "Bank name must not exceed 150 characters")
    private String bankName;

    @NotBlank(message = "Account number is required")
    @Size(max = 50, message = "Account number must not exceed 50 characters")
    @Pattern(regexp = "^[0-9]{6,50}$", message = "Account number must contain 6 to 50 digits")
    private String accountNumber;

    @NotBlank(message = "Account holder name is required")
    @Size(max = 150, message = "Account holder name must not exceed 150 characters")
    private String accountHolderName;

    @Size(max = 150, message = "Branch must not exceed 150 characters")
    private String branch;
}
