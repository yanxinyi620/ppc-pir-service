package cn.webank.wedpr.http;

import org.springframework.stereotype.Service;

import cn.webank.wedpr.pir.UserService;
import cn.webank.wedpr.shared.ProductService;

@Service
public class ParentProjectService {

    public static void main(String[] args) {
        System.out.println("Hello, World!");

        UserService userService = new UserService();
        String userMessage = userService.sayHello("John");
        System.out.println(userMessage);

        ProductService productService = new ProductService();
        String productInfo = productService.getProductInfo("Laptop");
        System.out.println(productInfo);
    }
}
