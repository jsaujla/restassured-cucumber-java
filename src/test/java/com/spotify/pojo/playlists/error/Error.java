package com.spotify.pojo.playlists.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Error {

    @JsonProperty("status")
    private Integer status;
    @JsonProperty("message")
    private String message;

}
