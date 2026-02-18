package modak.modakmodak.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "선호 모임 방식")
public enum MeetingMethod {
    //대면, 비대면
    ONLINE, OFFLINE
}