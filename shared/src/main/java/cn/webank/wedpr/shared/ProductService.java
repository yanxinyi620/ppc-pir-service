package cn.webank.wedpr.shared;

import org.springframework.stereotype.Service;

@Service
public class ProductService {

    public String getProductInfo(String product) {
        return "Product Info: " + product;
    }

    public static void main(String[] args) {
        System.out.println("Hello, World!");
    }

}
