package modak.modakmodak.dto;

public record UserJoinRequest(
        String username,
        String password,
        String email,
        String nickname
) {}
