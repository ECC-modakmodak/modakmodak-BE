package modak.modakmodak.util;

import java.util.regex.Pattern;

public class PasswordValidator {

    // 비밀번호 정규식: 영어, 숫자, 특수문자 모두 포함, 8-20자
    private static final String PASSWORD_PATTERN = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,20}$";

    private static final Pattern pattern = Pattern.compile(PASSWORD_PATTERN);

    /**
     * 비밀번호 유효성 검증
     * - 영문자 포함 필수
     * - 숫자 포함 필수
     * - 특수문자(@$!%*#?&) 포함 필수
     * - 8-20자 길이
     * 
     * @param password 검증할 비밀번호
     * @return 유효하면 true, 아니면 false
     */
    public static boolean isValid(String password) {
        if (password == null) {
            return false;
        }
        return pattern.matcher(password).matches();
    }

    /**
     * 비밀번호 유효성 검증 및 예외 발생
     * 
     * @param password 검증할 비밀번호
     * @throws IllegalArgumentException 비밀번호가 유효하지 않을 경우
     */
    public static void validate(String password) {
        if (!isValid(password)) {
            throw new IllegalArgumentException(
                    "비밀번호는 영문자, 숫자, 특수문자(@$!%*#?&)를 모두 포함하여 8-20자로 입력해주세요.");
        }
    }
}
