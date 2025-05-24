package com.programing.bookweb.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TopUserDTO {

    private Long id;
    private String fullName;
    private String email;
    private String phoneNumber;
    private double totalAmountPurchased;
}
