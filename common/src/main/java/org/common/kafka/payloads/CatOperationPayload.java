package org.common.kafka.payloads;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.io.Serializable;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = CreateCatPayload.class, name = "CREATE_CAT"),
    @JsonSubTypes.Type(value = RateCatPayload.class, name = "RATE_CAT"),
    @JsonSubTypes.Type(value = GetMyCatsPayload.class, name = "GET_MY_CATS"),
    @JsonSubTypes.Type(value = FindUserByChatIdPayload.class, name = "FIND_USER_BY_CHAT_ID"),
    @JsonSubTypes.Type(value = CreateUserPayload.class, name = "CREATE_USER"),
    @JsonSubTypes.Type(value = IsUserExistsPayload.class, name = "IS_USER_EXISTS"),
    @JsonSubTypes.Type(value = DeleteCatPayload.class, name = "DELETE_CAT"),
    @JsonSubTypes.Type(value = GetCatByIdPayload.class, name = "GET_CAT_BY_ID"),
    @JsonSubTypes.Type(value = GetRandomCatPayload.class, name = "GET_RANDOM_CAT"),
})
public interface CatOperationPayload extends Serializable {} 