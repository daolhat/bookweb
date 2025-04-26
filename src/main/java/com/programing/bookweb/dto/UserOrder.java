package com.programing.bookweb.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserOrder {

    private String fullName;
    private String email;
    private String phoneNumber;
    private String address;
}
