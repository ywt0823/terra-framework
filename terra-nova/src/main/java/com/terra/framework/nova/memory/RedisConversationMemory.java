package com.terra.framework.nova.memory;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RList;
import org.redisson.api.RListAsync;
import org.redisson.api.RedissonClient;
import org.springframework.ai.chat.messages.Message;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * An implementation of {@link ConversationMemory} that stores conversations in Redis.
 *
 * @author <a href="https://github.com/kuangcp">Kuangcp</a>
 * @date 2024-06-03 16:51
 */
@Slf4j
public class RedisConversationMemory implements ConversationMemory {

    private final RedissonClient redissonClient;
    private static final String NAMESPACE = "terra-nova:chat-memory:";
    private final String id = UUID.randomUUID().toString();

    public RedisConversationMemory(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    private String getSessionKey(String sessionId) {
        return NAMESPACE + sessionId;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public Optional<List<Message>> get(String sessionId) {
        RList<Message> messages = redissonClient.getList(getSessionKey(sessionId));
        if (messages.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(messages.readAll());
    }

    @Override
    public void add(String sessionId, List<Message> messages) {
        if (messages.isEmpty()) {
            return;
        }
        try {
            // Using batch operations to replace the history atomically
            var batch = redissonClient.createBatch();
            RListAsync<Message> batchList = batch.getList(getSessionKey(sessionId));
            batchList.deleteAsync();
            batchList.addAllAsync(messages);
            batch.execute();
        } catch (Exception e) {
            log.error("add message error", e);
        }
    }

    @Override
    public void clear(String sessionId) {
        redissonClient.getList(getSessionKey(sessionId)).delete();
    }
}
 