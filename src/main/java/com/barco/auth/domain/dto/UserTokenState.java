package com.barco.auth.domain.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.gson.Gson;

/**
 * @author Nabeel.amd
 */
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserTokenState {

    private User accessUser;
    private Notification notification;
    private String access_token;
    private Long expires_time; // in mint
    private final String token_type = "Bearer ";

    public UserTokenState() { }

    // builder
    public User getAccessUser() { return accessUser; }
    public void setAccessUser(User accessUser) { this.accessUser = accessUser; }

    public Notification getNotification() { return notification; }
    public void setNotification(Notification notification) { this.notification = notification; }

    public String getAccess_token() { return access_token; }
    public void setAccess_token(String access_token) { this.access_token = access_token; }

    public Long getExpires_time() { return expires_time; }
    public void setExpires_time(Long expires_time) { this.expires_time = expires_time; }

    public String getToken_type() { return token_type; }

    public static class User {

        private Long memberId;
        private String imageUrl;
        private String firstName;
        private String lastName;

        public User() { }

        public Long getMemberId() { return memberId; }
        public User setMemberId(Long memberId) {
            this.memberId = memberId;
            return this;
        }

        public String getImageUrl() { return imageUrl; }
        public User setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
            return this;
        }

        public String getFirstName() { return firstName; }
        public User setFirstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public String getLastName() { return lastName; }
        public User setLastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        @Override
        public String toString() { return new Gson().toJson(this); }
    }

    public static class Notification {

        private String topicId;
        private String clientPath;

        public Notification() { }

        public String getTopicId() { return topicId; }
        public Notification setTopicId(String topicId) {
            this.topicId = topicId;
            return this;
        }

        public String getClientPath() { return clientPath; }
        public Notification setClientPath(String clientPath) {
            this.clientPath = clientPath;
            return this;
        }

        @Override
        public String toString() { return new Gson().toJson(this); }
    }

    @Override
    public String toString() { return new Gson().toJson(this); }

}
