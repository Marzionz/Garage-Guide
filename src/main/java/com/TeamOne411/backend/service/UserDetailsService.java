package com.TeamOne411.backend.service;

import com.TeamOne411.backend.entity.users.*;
import com.TeamOne411.backend.repository.CarOwnerRepository;
import com.TeamOne411.backend.repository.RoleRepository;
import com.TeamOne411.backend.repository.UserRepository;
import com.TeamOne411.backend.service.exceptions.EmailExistsException;
import com.TeamOne411.backend.service.exceptions.PhoneNumberExistsException;
import com.TeamOne411.backend.service.exceptions.UsernameExistsException;
import com.TeamOne411.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

public class UserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CarOwnerRepository carOwnerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username);

        if (user == null) {
            throw new UsernameNotFoundException("Could not find user");
        }

        return new GGUserDetails(user);
    }

    public GGUserDetails getLoggedInUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!SecurityUtils.isUserLoggedIn() || authentication instanceof AnonymousAuthenticationToken) return null;

        return (GGUserDetails) loadUserByUsername(authentication.getName());
    }

    public boolean isUsernameExisting(String username){
        //if user does not exist, return false
        return  userRepository.findByUsername(username) != null;
    }

    public boolean isEmailExisting(String email){
        //if email does not exist, return false
        return  userRepository.findByEmail(email) != null;
    }

    public boolean isCarOwnerPhoneExisting(String phone){
        //if phone does not exist, return false
        return carOwnerRepository.findByPhoneNumber(phone) != null;
    }

    private Collection<? extends GrantedAuthority> getAuthorities(
            Collection<Role> roles) {

        return getGrantedAuthorities(getPrivileges(roles));
    }

    private List<String> getPrivileges(Collection<Role> roles) {

        List<String> privileges = new ArrayList<>();
        List<Privilege> collection = new ArrayList<>();
        for (Role role : roles) {
            collection.addAll(role.getPrivileges());
        }
        for (Privilege item : collection) {
            privileges.add(item.getName());
        }
        return privileges;
    }

    private List<GrantedAuthority> getGrantedAuthorities(List<String> privileges) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        for (String privilege : privileges) {
            authorities.add(new SimpleGrantedAuthority(privilege));
        }
        return authorities;
    }

    public User registerNewUser(User user) throws EmailExistsException, UsernameExistsException, PhoneNumberExistsException {
        // check for unique email address
        if (isEmailExisting(user.getEmail())) {
            throw new EmailExistsException(
                    "There is already an account with that email address:" + user.getEmail());
        }

        // check for unique username
        if (isUsernameExisting(user.getUsername())) {
            throw new UsernameExistsException(
                    "There is already an account with that username:" + user.getUsername());
        }

        // rewrite the password as encoded
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        user.setRoles(new LinkedList<>());

        // set roles based on user type
        if (user instanceof CarOwner) {
            user.setRoles(new LinkedList<Role>(Arrays.asList(roleRepository.findByName("ROLE_CAR_OWNER"))));

            // check for unique car owner phone number
            if (isCarOwnerPhoneExisting(((CarOwner) user).getPhoneNumber())) {
                throw new PhoneNumberExistsException(
                        "There is already an account with that phone number:" + ((CarOwner) user).getPhoneNumber());
            }
        }

        if (user instanceof GarageEmployee) {
            GarageEmployee ge = (GarageEmployee) user;
            user.setRoles(new LinkedList<Role>(Arrays.asList(roleRepository.findByName("ROLE_GARAGE_EMPLOYEE"))));

            if (ge.getIsAdmin()) {
                Collection<Role> roles = user.getRoles();
                roles.addAll(user.getRoles());
                user.setRoles(roles);
            }
        }

        return userRepository.save(user);
    }

    public User updateUser(User user) {
        if (user instanceof GarageEmployee) {
            GarageEmployee ge = (GarageEmployee) user;
            user.setRoles(new LinkedList<Role>(Arrays.asList(roleRepository.findByName("ROLE_GARAGE_EMPLOYEE"))));

            if (ge.getIsAdmin()) {
                Collection<Role> roles = user.getRoles();
                roles.addAll(user.getRoles());
                user.setRoles(roles);
            }
        }

        return userRepository.save(user);
    }
}