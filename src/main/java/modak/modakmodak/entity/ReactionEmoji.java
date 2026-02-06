package modak.modakmodak.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReactionEmoji {
    HELLO("👋 안녕하세요"),
    NICE_TO_MEET_YOU("🤝 반가워요"),
    FIGHTING("🔥 파이팅"),
    DO_MY_BEST("💪 열심히 할게요"),
    ON_MY_WAY("🏃 가고 있어요"),
    TIRED("😴 피곤해요"),
    HELP_ME("🆘 도와주세요"),
    LATE("⏰ 늦게 도착해요"),
    GOOD_JOB("👏 고생했어요");

    private final String description;
}