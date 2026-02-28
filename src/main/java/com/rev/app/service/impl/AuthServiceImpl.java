package com.rev.app.service.impl;

import com.rev.app.dto.JwtResponseDto;
import com.rev.app.dto.LoginDto;
import com.rev.app.dto.RegisterEmployerDto;
import com.rev.app.dto.RegisterJobSeekerDto;
import com.rev.app.dto.UserDto;
import com.rev.app.entity.Company;
import com.rev.app.entity.Employer;
import com.rev.app.entity.JobSeeker;
import com.rev.app.entity.User;
import com.rev.app.exception.ResourceNotFoundException;
import com.rev.app.mapper.UserMapper;
import com.rev.app.repository.ICompanyRepository;
import com.rev.app.repository.IEmployerRepository;
import com.rev.app.repository.IJobSeekerRepository;
import com.rev.app.repository.IUserRepository;
import com.rev.app.security.CustomUserDetails;
import com.rev.app.security.JwtTokenProvider;
import com.rev.app.service.IAuthService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class AuthServiceImpl implements IAuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final IUserRepository IUserRepository;
    private final IJobSeekerRepository IJobSeekerRepository;
    private final IEmployerRepository IEmployerRepository;
    private final ICompanyRepository ICompanyRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public AuthServiceImpl(AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider,
            IUserRepository IUserRepository, IJobSeekerRepository IJobSeekerRepository,
            IEmployerRepository IEmployerRepository, ICompanyRepository ICompanyRepository,
            PasswordEncoder passwordEncoder, UserMapper userMapper) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.IUserRepository = IUserRepository;
        this.IJobSeekerRepository = IJobSeekerRepository;
        this.IEmployerRepository = IEmployerRepository;
        this.ICompanyRepository = ICompanyRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
    }

    @Override
    @Transactional
    public UserDto registerJobSeeker(RegisterJobSeekerDto dto) {
        if (IUserRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Error: Email is already in use!");
        }

        User user = new User();
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole("ROLE_JOB_SEEKER");
        // No manual id setting - handled by Oracle IDENTITY
        User savedUser = IUserRepository.save(user);

        JobSeeker jobSeeker = new JobSeeker();
        jobSeeker.setUser(savedUser);
        jobSeeker.setName(dto.getName());
        jobSeeker.setPhone(dto.getPhone());
        jobSeeker.setExperienceYears(dto.getExperienceYears());
        jobSeeker.setProfileCompletion(0);
        IJobSeekerRepository.save(jobSeeker);

        return userMapper.toDto(savedUser);
    }

    @Override
    @Transactional
    public UserDto registerEmployer(RegisterEmployerDto dto) {
        if (IUserRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Error: Email is already in use!");
        }

        // 1. Save User first to generate IDENTITY ID
        User user = new User();
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole("ROLE_EMPLOYER");
        User savedUser = IUserRepository.save(user);

        // 2. Save Company (uses SEQUENCE as requested previously or IDENTITY if
        // changed)
        Company company = new Company();
        company.setName(dto.getCompanyName());
        company.setIndustry(dto.getIndustry());
        company.setSize(dto.getCompanySize());
        company.setDescription(dto.getDescription());
        company.setWebsite(dto.getWebsite());
        company.setLocation(dto.getLocation());
        Company savedCompany = ICompanyRepository.save(company);

        // 3. Save Employer using saved User and Company
        Employer employer = new Employer();
        employer.setUser(savedUser);
        employer.setCompany(savedCompany);
        employer.setDesignation(dto.getDesignation());
        IEmployerRepository.save(employer);

        return userMapper.toDto(savedUser);
    }

    @Override
    public JwtResponseDto login(LoginDto dto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtTokenProvider.generateJwtToken(authentication);

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String role = userDetails.getAuthorities().iterator().next().getAuthority();

        logger.info("User {} successfully logged in.", userDetails.getUsername());
        return new JwtResponseDto(jwt, "Bearer", userDetails.getId(),
                userDetails.getUsername(), role);
    }
}
