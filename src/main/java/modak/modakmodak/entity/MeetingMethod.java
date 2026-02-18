package modak.modakmodak.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "선호 모임 방식")
public enum MeetingMethod {
    ONLINE("비대면"),
    OFFLINE("대면");

    private final String value;
    MeetingMethod(String value) { this.value = value; }

    // JSON 데이터를 자바 객체로 바꿀 때 한글을 인식하게 해주는 마법의 어노테이션
    @com.fasterxml.jackson.annotation.JsonCreator
    public static MeetingMethod from(String value) {
        for (MeetingMethod method : MeetingMethod.values()) {
            if (method.value.equals(value)) return method;
        }
        return null;
    }

    // DB나 API 응답 시 한글로 보여주고 싶을 때
    @com.fasterxml.jackson.annotation.JsonValue
    public String getValue() { return value; }
}