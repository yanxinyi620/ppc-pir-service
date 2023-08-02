package cn.webank.wedpr.pir;

import org.springframework.stereotype.Service;

@Service
public class UserService {

    public String sayHello(String name) {
        return "Hello, " + name + "!";
    }
    
    public static void main(String[] args) {
        System.out.println("Hello, World!");
    }

}
