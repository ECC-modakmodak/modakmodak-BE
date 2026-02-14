package modak.modakmodak.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PasswordValidatorTest {

    @Test
    void testValidPasswords() {
        // 유효한 비밀번호들
        assertTrue(PasswordValidator.isValid("password1!"));
        assertTrue(PasswordValidator.isValid("Abcd1234!"));
        assertTrue(PasswordValidator.isValid("Test@123"));
        assertTrue(PasswordValidator.isValid("MyP@ssw0rd"));
        assertTrue(PasswordValidator.isValid("12345678aA!"));
    }

    @Test
    void testInvalidPasswords_NoSpecialChar() {
        // 특수문자 없음
        assertFalse(PasswordValidator.isValid("password123"));
        assertFalse(PasswordValidator.isValid("Abcd1234"));
    }

    @Test
    void testInvalidPasswords_NoNumber() {
        // 숫자 없음
        assertFalse(PasswordValidator.isValid("password!"));
        assertFalse(PasswordValidator.isValid("Abcdefg!"));
    }

    @Test
    void testInvalidPasswords_NoLetter() {
        // 영문자 없음
        assertFalse(PasswordValidator.isValid("12345678!"));
        assertFalse(PasswordValidator.isValid("!@#$%123"));
    }

    @Test
    void testInvalidPasswords_TooShort() {
        // 8자 미만
        assertFalse(PasswordValidator.isValid("Abc1!"));
        assertFalse(PasswordValidator.isValid("Test1!"));
    }

    @Test
    void testInvalidPasswords_TooLong() {
        // 20자 초과
        assertFalse(PasswordValidator.isValid("Abcd1234!Abcd1234!123"));
    }

    @Test
    void testInvalidPasswords_Null() {
        // null
        assertFalse(PasswordValidator.isValid(null));
    }

    @Test
    void testValidate_ThrowsException() {
        // 유효하지 않은 비밀번호는 예외 발생
        assertThrows(IllegalArgumentException.class, () -> {
            PasswordValidator.validate("password123");
        });
    }

    @Test
    void testValidate_NoException() {
        // 유효한 비밀번호는 예외 발생 안 함
        assertDoesNotThrow(() -> {
            PasswordValidator.validate("password123!");
        });
    }
}
