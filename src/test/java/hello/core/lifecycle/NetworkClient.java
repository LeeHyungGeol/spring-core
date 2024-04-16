package hello.core.lifecycle;


import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

public class NetworkClient {
    private String url;

    public NetworkClient() {
        System.out.println("생성자 호출, url = " + url);
    }

    public void setUrl(String url) {
        this.url = url;
    }

    // 서비스 시작시 호출
    public void connect() {
        System.out.println("connect = " + url);
    }

    public void call(String message) {
        System.out.println("url = " + url + " ,message = " + message);
    }

    // 서비스 종료시 호출
    public void disconnect() {
        System.out.println("close url = " + url);
    }

    @PostConstruct
    public void init() {
        System.out.println("NetworkClient.destroy");
        disconnect();
    }

    @PreDestroy
    public void close() {
        System.out.println("NetworkClient.afterPropertiesSet");
        connect();
        call("초기화 연결 메시지");
    }
}