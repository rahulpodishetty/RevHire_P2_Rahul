package com.rev.app.service;

import com.rev.app.dto.JwtResponseDto;
import com.rev.app.dto.LoginDto;
import com.rev.app.dto.RegisterEmployerDto;
import com.rev.app.dto.RegisterJobSeekerDto;
import com.rev.app.dto.UserDto;

public interface IAuthService {
    UserDto registerJobSeeker(RegisterJobSeekerDto dto);

    UserDto registerEmployer(RegisterEmployerDto dto);

    JwtResponseDto login(LoginDto dto);
}

