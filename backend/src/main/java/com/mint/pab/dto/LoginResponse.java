package com.mint.pab.dto;

import lombok.Data;

@Data
public class LoginResponse {

    private String token;

    private String expireTime;

}
