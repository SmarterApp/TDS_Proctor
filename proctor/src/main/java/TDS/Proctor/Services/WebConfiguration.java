package TDS.Proctor.Services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class WebConfiguration {

    @Bean(name = "integrationRestTemplate")
    public RestTemplate getRestTemplate(@Qualifier("integrationObjectMapper") final ObjectMapper objectMapper) {

        final MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper);

        final List<HttpMessageConverter<?>> converters = new ArrayList<>();
        converters.add(converter);
        converters.add(new StringHttpMessageConverter());

        final RestTemplate template = new RestTemplate();
        template.setMessageConverters(converters);
        return template;
    }

    @Bean(name = "integrationObjectMapper")
    public ObjectMapper getIntegrationObjectMapper() {
        return new ObjectMapper()
            .registerModule(new JodaModule());
    }
}
