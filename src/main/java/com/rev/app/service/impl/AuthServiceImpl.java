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

@Service
public class AuthServiceImpl implements IAuthService {

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
        if (IUserRepository.existsByEmail(dto.getEmail().toLowerCase())) {
            throw new IllegalArgumentException("Error: Email is already in use!");
        }

        User user = new User();
        user.setEmail(dto.getEmail().toLowerCase());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole("ROLE_SEEKER");
        User savedUser = IUserRepository.save(user);
        System.out.println("DEBUG [AuthService]: User saved with ID: " + savedUser.getId());

        JobSeeker jobSeeker = new JobSeeker();
        jobSeeker.setUser(savedUser);
        jobSeeker.setName(dto.getName());
        jobSeeker.setPhone(dto.getPhone());
        jobSeeker.setExperienceYears(dto.getExperienceYears());
        jobSeeker.setLocation(dto.getLocation());
        jobSeeker.setCurrentEmploymentStatus(dto.getCurrentEmploymentStatus());
        jobSeeker.setProfileCompletion(0);
        IJobSeekerRepository.save(jobSeeker);

        System.out.println("DEBUG [AuthService]: JobSeeker registration complete for Email: " + savedUser.getEmail());
        return userMapper.toDto(savedUser);
    }

    @Override
    @Transactional
    public UserDto registerEmployer(RegisterEmployerDto dto) {
        if (IUserRepository.existsByEmail(dto.getEmail().toLowerCase())) {
            throw new IllegalArgumentException("Error: Email is already in use!");
        }

        // 1. Save User first
        User user = new User();
        user.setEmail(dto.getEmail().toLowerCase());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole("ROLE_EMPLOYER");
        User savedUser = IUserRepository.save(user);
        System.out.println("DEBUG [AuthService]: User saved with ID: " + savedUser.getId());

        // 2. Save Company
        Company company = new Company();
        company.setName(dto.getCompanyName());
        company.setIndustry(dto.getIndustry());
        company.setSize(dto.getCompanySize());
        company.setDescription(dto.getDescription());
        company.setWebsite(dto.getWebsite());
        company.setLocation(dto.getLocation());
        Company savedCompany = ICompanyRepository.save(company);

        // 3. Save Employer
        Employer employer = new Employer();
        employer.setUser(savedUser);
        employer.setCompany(savedCompany);
        employer.setDesignation(dto.getDesignation());
        IEmployerRepository.save(employer);

        System.out.println("DEBUG [AuthService]: Employer registration complete for Email: " + savedUser.getEmail());
        return userMapper.toDto(savedUser);
    }

    @Override
    public JwtResponseDto login(LoginDto dto) {
        // Normalise the email to lowercase — passwords are stored against lowercase
        // emails
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getEmail().toLowerCase().trim(), dto.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtTokenProvider.generateJwtToken(authentication);

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String role = userDetails.getAuthorities().iterator().next().getAuthority();

        System.out.println("DEBUG: User " + userDetails.getUsername() + " successfully logged in. Role: " + role);
        return new JwtResponseDto(jwt, "Bearer", userDetails.getId(),
                userDetails.getUsername(), role);
    }
}
