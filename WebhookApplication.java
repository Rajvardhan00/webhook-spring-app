package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication
public class WebhookApplication {
    public static void main(String[] args) {
        SpringApplication.run(WebhookApplication.class, args);
    }
}

@Component
class WebhookRunner implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(WebhookRunner.class);
    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public void run(String... args) throws Exception {
        try {
            logger.info("Starting webhook flow...");
            
            // Step 1: Generate webhook
            WebhookResponse response = generateWebhook();
            logger.info("Webhook generated. URL: {}", response.webhook);
            
            // Step 2: Solve SQL problem
            String regNo = "REG12347";
            String sqlSolution = solveSqlProblem(regNo);
            logger.info("SQL solution: {}", sqlSolution);
            
            // Step 3: Submit solution
            submitSolution(response.webhook, response.accessToken, sqlSolution);
            logger.info("Solution submitted successfully!");
            
        } catch (Exception e) {
            logger.error("Error in webhook flow: ", e);
        }
    }

    private WebhookResponse generateWebhook() {
        String url = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";
        
        WebhookRequest request = new WebhookRequest();
        request.name = "John Doe";
        request.regNo = "REG12347";
        request.email = "john@example.com";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<WebhookRequest> entity = new HttpEntity<>(request, headers);
        
        return restTemplate.postForObject(url, entity, WebhookResponse.class);
    }

    private String solveSqlProblem(String regNo) {
        int lastTwoDigits = Integer.parseInt(regNo.substring(regNo.length() - 2));
        
        if (lastTwoDigits % 2 == 1) {
            // Odd - Question 1
            return """
                SELECT 
                    e.employee_id,
                    e.employee_name,
                    d.department_name,
                    e.salary
                FROM employees e
                JOIN departments d ON e.department_id = d.department_id
                WHERE e.salary > (
                    SELECT AVG(salary) 
                    FROM employees 
                    WHERE department_id = e.department_id
                )
                ORDER BY e.salary DESC;
                """;
        } else {
            // Even - Question 2
            return """
                SELECT 
                    p.product_name,
                    c.category_name,
                    SUM(s.quantity_sold) as total_sold,
                    SUM(s.quantity_sold * p.unit_price) as total_revenue
                FROM products p
                JOIN categories c ON p.category_id = c.category_id
                JOIN sales s ON p.product_id = s.product_id
                WHERE s.sale_date >= DATE_SUB(CURDATE(), INTERVAL 6 MONTH)
                GROUP BY p.product_id, p.product_name, c.category_name
                HAVING total_sold > 100
                ORDER BY total_revenue DESC;
                """;
        }
    }

    private void submitSolution(String webhookUrl, String accessToken, String sqlQuery) {
        String url = "https://bfhldevapigw.healthrx.co.in/hiring/testWebhook/JAVA";
        
        SolutionRequest request = new SolutionRequest();
        request.finalQuery = sqlQuery;
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);
        HttpEntity<SolutionRequest> entity = new HttpEntity<>(request, headers);
        
        restTemplate.postForObject(url, entity, String.class);
    }
}

// Simple DTOs
class WebhookRequest {
    public String name;
    public String regNo;
    public String email;
}

class WebhookResponse {
    public String webhook;
    public String accessToken;
}

class SolutionRequest {
    public String finalQuery;
}