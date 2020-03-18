package com.playgroundtest.api;

import com.playgroundtest.application.PlaygroundApplication;
import com.playgroundtest.dao.Configuration;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping(value = "/api")
public class PlaygroundAPI {
    
    private PlaygroundApplication playgroundApplication;

    @GetMapping(value = "/configurations")
    public List<Configuration> getConfigurations() {
        return playgroundApplication.getConfiguration();
    }

    @GetMapping(value = "/configuration/{id}")
    public Configuration getConfigurationById(@PathVariable("id") Long id) {
        return playgroundApplication.getConfigurationById(id);
    }

    @PostMapping(value = "/save")
    public Configuration saveConfiguration(@RequestBody Configuration configuration) {
        return playgroundApplication.saveConfiguration(configuration);
    }
}
