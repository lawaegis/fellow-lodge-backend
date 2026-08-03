package com.fellowlodge.api.entity;

/**
 * Contract for content entities that can be toggled on/off for the public
 * guest portal. Implemented by all admin-managed content modules.
 */
public interface Activatible {

    boolean isActive();

    void setActive(boolean active);
}
