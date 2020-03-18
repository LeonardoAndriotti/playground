package com.playgroundtest.application;

import com.playgroundtest.dao.Configuration;
import com.playgroundtest.dao.ConfigurationRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;

@Service
@AllArgsConstructor
public class PlaygroundApplication {

    private ConfigurationRepository configurationRepository;

    public List<Configuration> getConfiguration(){
        return configurationRepository.findAll();
    }

    public Configuration getConfigurationById(Long id){
        return configurationRepository.findById(id).orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND));
    }

    public Configuration saveConfiguration(Configuration configuration){
        return configurationRepository.save(configuration);
    }
}
