package modak.modakmodak.entity;

import lombok.Getter;

@Getter
public enum ReviewTag {
    DONE("해냈어요"),
    SO_SO("무난했어요"),
    AS_PLANNED("계획대로"),
    UNTIL_END("끝까지 함"),
    SATISFIED("만족"),
    REGRETFUL("아쉬워요"),
    RECOVERED("집중 회복"),
    STEADY("꾸준하게"),
    WELL_DONE("잘했어요"),
    ENDURED("잘 버텼다"),
    GOAL_ACHIEVED("목표 달성"),
    NEXT_TIME("다음에 해요"),
    JUST_SAT("일단 앉음"),
    MEANINGFUL("의미 있었어요"),
    FOCUSED("집중했어요"),
    JUST_OK("그냥 그랬어요"),
    PROUD("뿌듯해요"),
    LACKING("부족했어요"),
    NEVER_GIVE_UP("포기 안 해"),
    SUCCESSFUL("성공적"),
    CANT_FOCUS("집중이 안 돼요");

    private final String description;

    ReviewTag(String description) {
        this.description = description;
    }
}