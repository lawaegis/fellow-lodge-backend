package com.fellowlodge.api.dto.publiccatalog;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Public policy detail for the guest portal. The slug is derived from the
 * policy title so it matches the portal's `slugify` links without a dedicated
 * column; the content is returned verbatim for the detail page.
 */
public record PublicPolicyResponse(
        UUID id,
        String slug,
        String title,
        String content,
        String category,
        LocalDateTime updatedAt) {
}
