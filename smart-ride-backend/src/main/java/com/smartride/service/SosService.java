package com.smartride.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.smartride.exception.ApiException;
import com.smartride.entity.SosAlert;
import com.smartride.entity.User;
import com.smartride.dao.SosAlertDao;

/** Emergency SOS events. Employees/drivers raise them, the admin sees and resolves them. */
@Service
public class SosService {

    private final SosAlertDao sosDao;

    public SosService(SosAlertDao sosDao) {
        this.sosDao = sosDao;
    }

    public SosAlert raise(User user, Double lat, Double lng) {
        SosAlert s = new SosAlert();
        s.setUser(user);
        s.setLatitude(lat);
        s.setLongitude(lng);
        return sosDao.save(s);
    }

    public List<SosAlert> all() {
        return sosDao.findAll();
    }

    public SosAlert resolve(Long id) {
        SosAlert s = sosDao.findById(id).orElseThrow(() -> ApiException.notFound("SOS alert not found"));
        s.setResolved(true);
        return sosDao.save(s);
    }
}
