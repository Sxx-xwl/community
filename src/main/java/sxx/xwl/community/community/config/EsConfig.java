//package sxx.xwl.community.community.config;
//
//import org.elasticsearch.client.RestHighLevelClient;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.data.elasticsearch.client.ClientConfiguration;
//import org.springframework.data.elasticsearch.client.RestClients;
//
///**
// * @author sxx_27
// * @create 2022-05-17 10:43
// */
//@Configuration
//public class EsConfig {
//
//    @Value("${elasticsearch.uris}")
//    private String esUrl;
//
//    @Bean
//    RestHighLevelClient client(){
//        ClientConfiguration clientConfiguration = ClientConfiguration.builder()
//                .connectedTo(esUrl)
//                .build();
//
//        return RestClients.create(clientConfiguration).rest();
//    }
//
//}
