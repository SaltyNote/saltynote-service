package com.saltynote.service.boot;

import com.saltynote.service.generator.IdGenerator;
import com.saltynote.service.repository.LoginHistoryRepository;
import com.saltynote.service.repository.NoteRepository;
import com.saltynote.service.repository.UserRepository;
import com.saltynote.service.repository.VaultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Order(1)
@RequiredArgsConstructor
@Slf4j
public class ServiceApplicationRunner implements ApplicationRunner {

    private final UserRepository userRepository;

    private final NoteRepository noteRepository;

    private final VaultRepository vaultRepository;

    private final LoginHistoryRepository loginHistoryRepository;

    private final IdGenerator snowflakeIdGenerator;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("Start fixing data....");
        fixUsers();
        Map<String, Long> userIdMap = getUserIdMap();
        fixNotes(userIdMap);
        fixVaults(userIdMap);
        fixLoginHistory(userIdMap);
        log.info("Finished fixing data....");
    }

    private void fixUsers() {
        userRepository.findAll().forEach(user -> {
            if (user.getIdx() != null && user.getIdx() > 0) {
                return;
            }
            log.info("Fixing user: {}", user.getId());
            user.setIdx(snowflakeIdGenerator.nextId());
            userRepository.save(user);
        });
    }

    private Map<String, Long> getUserIdMap() {
        Map<String, Long> result = new HashMap<>();
        userRepository.findAll().forEach(user -> result.put(user.getId(), user.getIdx()));
        log.info("Fetched {} users", result.size());
        return result;
    }

    private void fixNotes(Map<String, Long> userIdMap) {
        noteRepository.findAll().forEach(note -> {
            if (note.getIdx() != null && note.getIdx() > 0 && note.getUserIdx() != null && note.getUserIdx() > 0) {
                return;
            }
            log.info("Fixing note: {} for user {}", note.getId(), note.getUserId());
            note.setIdx(snowflakeIdGenerator.nextId());
            note.setUserIdx(userIdMap.get(note.getUserId()));
            noteRepository.save(note);
        });
    }

    private void fixVaults(Map<String, Long> userIdMap) {
        vaultRepository.findAll().forEach(vault -> {
            if (vault.getIdx() != null && vault.getIdx() > 0 && vault.getUserIdx() != null && vault.getUserIdx() > 0) {
                return;
            }
            boolean toUpdate = false;
            if (vault.getIdx() == null) {
                vault.setIdx(snowflakeIdGenerator.nextId());
                toUpdate = true;
            }
            if (vault.getUserId() != null && vault.getUserIdx() == null && userIdMap.containsKey(vault.getUserId())) {
                vault.setUserIdx(userIdMap.get(vault.getUserId()));
                toUpdate = true;
            }
            if (toUpdate) {
                log.info("Fixing vault: {} for user {}", vault.getId(), vault.getUserId());
                vaultRepository.save(vault);
            }
        });
    }

    private void fixLoginHistory(Map<String, Long> userIdMap) {
        loginHistoryRepository.findAll().forEach(history -> {
            if (history.getUserIdx() != null && history.getUserIdx() > 0) {
                return;
            }
            log.info("Fixing login history: {} for user {}", history.getId(), history.getUserId());
            history.setUserIdx(userIdMap.get(history.getUserId()));
            loginHistoryRepository.save(history);
        });
    }

}
