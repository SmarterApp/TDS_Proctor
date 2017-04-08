package TDS.Proctor.Services;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import tds.shared.spring.configuration.WebConfiguration;

@Configuration
@Import(WebConfiguration.class)
public class ProctorWebConfiguration {
}
