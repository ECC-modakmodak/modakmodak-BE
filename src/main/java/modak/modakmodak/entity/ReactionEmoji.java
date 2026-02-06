package modak.modakmodak.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReactionEmoji {
    hi("👋 안녕하세요"),
    niceToMeet("🤝 반가워요"),
    cheerUp("🔥 파이팅"),
    workingHard("💪 열심히 할게요"),
    onMyWay("🏃 가고 있어요"),
    tired("😴 피곤해요"),
    needHelp("🆘 도와주세요"),
    runningLate("⏰ 늦게 도착해요"),
    goodJob("👏 고생했어요");

    private final String description;
}