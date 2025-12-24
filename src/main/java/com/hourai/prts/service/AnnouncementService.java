package com.hourai.prts.service;

import com.hourai.prts.dao.AnnouncementDao;
import com.hourai.prts.entity.Announcement;
import java.sql.SQLException;

public class AnnouncementService {
    private final AnnouncementDao announcementDao = new AnnouncementDao();

    public long addAnnouncement(Announcement a) throws SQLException {
        announcementDao.insert(a);
        return a.getId() == null ? -1L : a.getId();
    }

    public java.util.List<Announcement> getAllAnnouncements() throws java.sql.SQLException {
        return announcementDao.selectAll();
    }
}
