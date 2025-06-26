package org.common.kafka.payloads;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = CatListResponsePayload.class, name = "CAT_LIST"),
        @JsonSubTypes.Type(value = ErrorResponsePayload.class, name = "ERROR"),
        @JsonSubTypes.Type(value = SuccessResponsePayload.class, name = "SUCCESS"),
        @JsonSubTypes.Type(value = SuccessRateResponsePayload.class, name = "SUCCESS_RATE"),
        @JsonSubTypes.Type(value = SingleCatResponsePayload.class, name = "SINGLE_CAT"),
        @JsonSubTypes.Type(value = UserResponsePayload.class, name = "USER"),
        @JsonSubTypes.Type(value = UserExistsResponsePayload.class, name = "USER_EXISTS"),
        @JsonSubTypes.Type(value = CreateCatPayload.class, name = "CREATE_CAT"),
        @JsonSubTypes.Type(value = RateCatPayload.class, name = "RATE_CAT"),
        @JsonSubTypes.Type(value = GetMyCatsPayload.class, name = "GET_MY_CATS"),
        @JsonSubTypes.Type(value = FindUserByChatIdPayload.class, name = "FIND_USER_BY_CHAT_ID"),
        @JsonSubTypes.Type(value = CreateUserPayload.class, name = "CREATE_USER"),
        @JsonSubTypes.Type(value = IsUserExistsPayload.class, name = "IS_USER_EXISTS"),
        @JsonSubTypes.Type(value = DeleteCatPayload.class, name = "DELETE_CAT"),
        @JsonSubTypes.Type(value = GetCatByIdPayload.class, name = "GET_CAT_BY_ID"),
        @JsonSubTypes.Type(value = GetRandomCatPayload.class, name = "GET_RANDOM_CAT"),
        @JsonSubTypes.Type(value = GetRandomCatResponsePayload.class, name = "GET_RANDOM_CAT_RESPONSE")
})
public interface CatResponsePayload extends Serializable {
} 