package com.smartride.resource;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartride.dto.GpsPing;
import com.smartride.entity.SosAlert;
import com.smartride.entity.User;
import com.smartride.service.SosService;

/** ALL requests about emergency SOS alerts: raise (employee / driver) and list / resolve (admin). */
@RestController
@RequestMapping("/api")
public class SosResource {

    private final SosService sosService;

    public SosResource(SosService sosService) {
        this.sosService = sosService;
    }

    @PostMapping("/common/sos")
    public SosAlert raise(@RequestAttribute("currentUser") User user, @RequestBody GpsPing ping) {
        return sosService.raise(user, ping.lat(), ping.lng());
    }

    @GetMapping("/admin/sos")
    public List<SosAlert> list() {
        return sosService.all();
    }

    @PutMapping("/admin/sos/{id}/resolve")
    public SosAlert resolve(@PathVariable("id") Long id) {
        return sosService.resolve(id);
    }
}
