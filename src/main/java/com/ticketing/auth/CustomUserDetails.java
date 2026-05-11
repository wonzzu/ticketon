package com.ticketing.auth;

import com.ticketing.member.domain.Member;
import com.ticketing.member.domain.MemberStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {

    private final Member member;

    public Long getMemberId() {
        return member.getId();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + member.getMemberType().name()));
    }

    @Override
    public String getPassword() {
        return member.getPassword();
    }

    @Override
    public String getUsername() {
        return member.getEmail();
    }

    @Override
    public boolean isAccountNonLocked() {
        return member.getMemberStatus() != MemberStatus.SUSPENDED;
    }

    @Override
    public boolean isEnabled() {
        MemberStatus s = member.getMemberStatus();
        return s == MemberStatus.ACTIVE || s == MemberStatus.PENDING;
    }
}
