package com.hourai.prts.service;

import com.hourai.prts.dao.AnnouncementDao;
import com.hourai.prts.entity.Announcement;
import java.sql.SQLException;

public class AnnouncementService {
    private final AnnouncementDao announcementDao = new AnnouncementDao();

    public int addAnnouncement(Announcement a) throws SQLException {
        return announcementDao.insert(a);
    }
    public java.util.List<Announcement> getAllAnnouncements() throws java.sql.SQLException {
        return announcementDao.selectAll();
    }
}
