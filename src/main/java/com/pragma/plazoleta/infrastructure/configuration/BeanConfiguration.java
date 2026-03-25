package com.pragma.plazoleta.infrastructure.configuration;

import com.pragma.plazoleta.domain.api.IEmployeeServicePort;
import com.pragma.plazoleta.domain.api.IOrderServicePort;
import com.pragma.plazoleta.domain.api.IPlateServicePort;
import com.pragma.plazoleta.domain.api.IRestaurantServicePort;
import com.pragma.plazoleta.domain.spi.*;
import com.pragma.plazoleta.domain.usecase.EmployeeUseCase;
import com.pragma.plazoleta.domain.usecase.OrderUseCase;
import com.pragma.plazoleta.domain.usecase.PlateUseCase;
import com.pragma.plazoleta.domain.usecase.RestaurantUseCase;
import com.pragma.plazoleta.infrastructure.out.http.adapter.UserHttpAdapter;
import com.pragma.plazoleta.infrastructure.out.jpa.adapter.EmployeeRestaurantJpaAdapter;
import com.pragma.plazoleta.infrastructure.out.jpa.adapter.OrderJpaAdapter;
import com.pragma.plazoleta.infrastructure.out.jpa.adapter.PlateJpaAdapter;
import com.pragma.plazoleta.infrastructure.out.jpa.adapter.RestaurantJpaAdapter;
import com.pragma.plazoleta.infrastructure.out.jpa.mapper.IEmployeeRestaurantEntityMapper;
import com.pragma.plazoleta.infrastructure.out.jpa.mapper.IOrderEntityMapper;
import com.pragma.plazoleta.infrastructure.out.jpa.mapper.IPlateEntityMapper;
import com.pragma.plazoleta.infrastructure.out.jpa.mapper.IRestaurantEntityMapper;
import com.pragma.plazoleta.infrastructure.out.jpa.repository.IEmployeeRestaurantRepository;
import com.pragma.plazoleta.infrastructure.out.jpa.repository.IOrderRepository;
import com.pragma.plazoleta.infrastructure.out.jpa.repository.IPlateRepository;
import com.pragma.plazoleta.infrastructure.out.jpa.repository.IRestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.Jaxb2RootElementHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class BeanConfiguration {

    private final IRestaurantRepository restaurantRepository;
    private final IRestaurantEntityMapper restaurantEntityMapper;
    private final IPlateRepository plateRepository;
    private final IPlateEntityMapper plateEntityMapper;
    private final IOrderRepository orderRepository;
    private final IOrderEntityMapper orderEntityMapper;
    private final IEmployeeRestaurantRepository employeeRestaurantRepository;
    private final IEmployeeRestaurantEntityMapper employeeRestaurantEntityMapper;
    

    @Value("${usuarios.service.url}")
    private String usuariosServiceUrl;

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();

        List<HttpMessageConverter<?>> converters = new ArrayList<>(restTemplate.getMessageConverters());

        converters.removeIf(c -> c instanceof Jaxb2RootElementHttpMessageConverter);

        MappingJackson2HttpMessageConverter jacksonConverter = null;
        for (HttpMessageConverter<?> c : converters) {
            if (c instanceof MappingJackson2HttpMessageConverter) {
                jacksonConverter = (MappingJackson2HttpMessageConverter) c;
                break;
            }
        }

        if (jacksonConverter != null) {
            converters.remove(jacksonConverter);
            converters.add(0, jacksonConverter);
        } else {
            converters.add(0, new MappingJackson2HttpMessageConverter());
        }

        restTemplate.setMessageConverters(converters);
        return restTemplate;
    }

    @Bean
    public IUserPersistencePort userPersistencePort() {
        return new UserHttpAdapter(restTemplate(), usuariosServiceUrl);
    }

    @Bean
    public IRestaurantPersistencePort restaurantPersistencePort() {
        return new RestaurantJpaAdapter(restaurantRepository, restaurantEntityMapper);
    }

    @Bean
    public IRestaurantServicePort restaurantServicePort() {
        return new RestaurantUseCase(restaurantPersistencePort(), userPersistencePort());
    }

    @Bean
    public IPlatePersistencePort platePersistencePort() {
        return new PlateJpaAdapter(plateRepository, plateEntityMapper);
    }

    @Bean
    public IPlateServicePort plateServicePort() {
        return new PlateUseCase(platePersistencePort(), restaurantPersistencePort());
    }

    @Bean
    public IEmployeeRestaurantPersistencePort employeeRestaurantPersistencePort() {
        return new EmployeeRestaurantJpaAdapter(employeeRestaurantRepository, employeeRestaurantEntityMapper);
    }

    @Bean
    public IEmployeeServicePort employeeServicePort() {
        return new EmployeeUseCase(userPersistencePort(), restaurantPersistencePort(), employeeRestaurantPersistencePort());
    }

    @Bean
    public IOrderPersistencePort orderPersistencePort() {
        return new OrderJpaAdapter(orderRepository, orderEntityMapper);
    }

    @Bean
    public ISmsNotificationPort smsNotificationPort() {
        return new com.pragma.plazoleta.infrastructure.out.rest.SmsRestAdapter(restTemplate());
    }

    @Bean
    public IOrderServicePort orderServicePort(ITraceabilityNotificationPort traceabilityNotificationPort, ISmsNotificationPort smsNotificationPort) {
        return new OrderUseCase(orderPersistencePort(), restaurantPersistencePort(),
            platePersistencePort(), employeeRestaurantPersistencePort(),
            smsNotificationPort, userPersistencePort(), traceabilityNotificationPort);
    }
}