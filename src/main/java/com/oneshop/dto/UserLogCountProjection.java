package com.oneshop.dto;

public interface UserLogCountProjection {
	Long getUserId();
    String getUsername();
    String getEmail();
    Long getLogCount();
}
