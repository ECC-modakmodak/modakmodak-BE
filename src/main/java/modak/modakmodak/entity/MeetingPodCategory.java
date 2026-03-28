package modak.modakmodak.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Schema(description = "팟 상세 카테고리")
public enum MeetingPodCategory {
    EXAMS("시험대비"),
    PROJECTS("과제팀플"),
    CODING("프로그래밍"),
    LANGUAGES("어학"),
    CERTS("자격증"),
    JOBS("취업준비"),
    READING("독서"),
    GROWTH("자기계발");

    private final String value;

    @JsonCreator
    public static MeetingPodCategory from(String value) {
        for (MeetingPodCategory category : MeetingPodCategory.values()) {
            // 한글 값("시험대비")이나 Enum 이름("EXAMS") 모두 인식하도록 설정
            if (category.value.equals(value) || category.name().equalsIgnoreCase(value)) {
                return category;
            }
        }
        return null;
    }

    @JsonValue
    public String getValue() { return value; }
}
