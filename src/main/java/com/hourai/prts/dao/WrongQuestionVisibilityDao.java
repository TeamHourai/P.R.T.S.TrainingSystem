package com.hourai.prts.dao;

import com.hourai.prts.data.DataStore;
import com.hourai.prts.entity.WrongQuestionVisibility;
import com.hourai.prts.utils.Utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * CSV-backed DAO for wrong-question visibility.
 *
 * Schema (data/wrong_visibility.csv):
 *   id,userId,questionId,hidden,updatedAt
 */
public class WrongQuestionVisibilityDao {

    private Path file() {
        return DataStore.getWrongVisibilityFile();
    }

    public synchronized List<WrongQuestionVisibility> selectAll() throws IOException {
        Path f = file();
        if (!Files.exists(f)) return new ArrayList<>();
        List<String> lines = Files.readAllLines(f, StandardCharsets.UTF_8);
        List<WrongQuestionVisibility> out = new ArrayList<>();
        for (String ln : lines) {
            if (ln == null) continue;
            String t = ln.trim();
            if (t.isEmpty()) continue;
            String[] p = t.split(",", 5);
            if (p.length < 5) continue;
            try {
                long id = Long.parseLong(p[0].trim());
                long userId = Long.parseLong(p[1].trim());
                long questionId = Long.parseLong(p[2].trim());
                boolean hidden = Boolean.parseBoolean(p[3].trim());
                String updatedAt = p[4];
                out.add(new WrongQuestionVisibility(id, userId, questionId, hidden, updatedAt));
            } catch (Exception ignored) {
            }
        }
        out.sort(Comparator.comparingLong(WrongQuestionVisibility::getId));
        return out;
    }

    public synchronized Optional<WrongQuestionVisibility> findByUserAndQuestion(long userId, long questionId) throws IOException {
        return selectAll().stream().filter(r -> r.getUserId() == userId && r.getQuestionId() == questionId).findFirst();
    }

    /**
     * Upsert a visibility record.
     */
    public synchronized WrongQuestionVisibility upsert(long userId, long questionId, boolean hidden) throws IOException {
        List<WrongQuestionVisibility> all = selectAll();
        Optional<WrongQuestionVisibility> existing = all.stream().filter(r -> r.getUserId() == userId && r.getQuestionId() == questionId).findFirst();
        String now = Utils.now();
        if (existing.isPresent()) {
            WrongQuestionVisibility r = existing.get();
            r.setHidden(hidden);
            r.setUpdatedAt(now);
        } else {
            long id = DataStore.nextId(all);
            all.add(new WrongQuestionVisibility(id, userId, questionId, hidden, now));
        }
        rewriteAll(all);
        return findByUserAndQuestion(userId, questionId).orElseThrow();
    }

    private void rewriteAll(List<WrongQuestionVisibility> all) throws IOException {
        Path f = file();
        if (f.getParent() != null && !Files.exists(f.getParent())) {
            Files.createDirectories(f.getParent());
        }
        List<String> lines = new ArrayList<>();
        for (WrongQuestionVisibility r : all) {
            String line = r.getId() + "," + r.getUserId() + "," + r.getQuestionId() + "," + r.isHidden() + "," + r.getUpdatedAt();
            lines.add(line);
        }
        Files.write(f, lines, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }
}

