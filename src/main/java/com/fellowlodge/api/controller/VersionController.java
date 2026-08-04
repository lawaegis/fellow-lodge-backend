package com.fellowlodge.api.controller;

import com.fellowlodge.api.common.ApiResponse;
import com.fellowlodge.api.config.AppProperties;
import com.fellowlodge.api.dto.version.VersionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public version metadata consumed by the desktop client (update checking)
 * and the guest portal (version badges). No authentication required.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class VersionController {

    private static final long UPDATE_INTERVAL_DAYS = 90;

    private final AppProperties appProperties;

    @GetMapping("/version")
    public ApiResponse<VersionResponse> version() {
        return ApiResponse.ok(VersionResponse.backend(
                appProperties.getVersion(),
                appProperties.getMinDesktopVersion(),
                UPDATE_INTERVAL_DAYS));
    }
}
