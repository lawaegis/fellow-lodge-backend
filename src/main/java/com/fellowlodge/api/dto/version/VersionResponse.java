package com.fellowlodge.api.dto.version;

public record VersionResponse(
        String application,
        String component,
        String version,
        String minimumDesktopVersion,
        long updateIntervalDays) {

    public static VersionResponse backend(String version, String minDesktopVersion, long updateIntervalDays) {
        return new VersionResponse("fellow-lodge", "backend", version, minDesktopVersion, updateIntervalDays);
    }
}
